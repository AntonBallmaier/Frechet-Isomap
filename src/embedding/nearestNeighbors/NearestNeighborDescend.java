package embedding.nearestNeighbors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

import embedding.measures.Measure;
import embedding.shortestPaths.AdjacencyList;

/**
 * {@link NearestNeighborDescend} is a randomized implementation for
 * {@link KNearestNeighborGraph} that approximates the nearest neighbor graph.
 *
 * <p>
 * Calculations for previous nearest neighbor graphs are reused so that
 * calculating the neighborhood graph a second time for a different k is quicker
 * than calculating that graph from scratch.
 *
 * @author Anton Ballmaier
 *
 * @param <T> the type of objects to be compared. The used {@link Measure} must
 *            also be applicable for this type.
 */
public class NearestNeighborDescend<T> extends KNearestNeighborGraph<T> {
	/**
	 * Simple synchronized integer counter. It can be used to count in a parallel
	 * context.
	 */
	private static class SynchronizedCounter {
		/**
		 * the actual count
		 */
		private int count = 0;

		/**
		 * Adds 1 to the counter.
		 */
		public synchronized void add() {
			count++;
		}

		/**
		 * Reads and returns the current counter value. This does not happen
		 * synchronized.
		 *
		 * @return the current counter value
		 */
		public int get() {
			return count;
		}

		/**
		 * Resets the counter to 0.
		 */
		public synchronized void reset() {
			count = 0;
		}
	}

	/**
	 * Synchronized version of a {@link TreeSet} of {@link WeightedIndex} objects.
	 * It uses the weight of the weighted indices as the parameter to sort the
	 * elements by. Notice that only the following methods are synchronized:
	 * <ul>
	 * <li>{@link #add}
	 * <li>{@link #last}
	 * <li>{@link #pollLast}
	 * </ul>
	 */
	private static class SynchronizedWeightedIndexSet extends TreeSet<WeightedIndex> {
		private static final long serialVersionUID = 1L;

		/*
		 * The set of indices currently stored
		 */
		private final Set<Integer> indexSet;

		/**
		 * Lock used to prevent parallelization conflicts
		 */
		private final ReentrantReadWriteLock lock;

		/**
		 * Construct a new {@link SynchronizedWeightedIndexSet} instance.
		 * {@link WeightedIndex} objects added to this set will be sorted by their
		 * weight.
		 */
		public SynchronizedWeightedIndexSet() {
			super((a, b) -> a.weight > b.weight ? 1 : a.weight < b.weight ? -1 : a.index - b.index);
			lock = new ReentrantReadWriteLock();
			indexSet = new HashSet<>();
		}

		@Override
		public boolean add(WeightedIndex e) {
			lock.writeLock().lock();
			final boolean added = super.add(e);
			indexSet.add(e.index);
			lock.writeLock().unlock();
			return added;
		}

		/**
		 * Checks whether the given index is currently in the set.
		 *
		 * @param i the index
		 * @return <code>true</code> if the index is in the set, <code>false</code>
		 *         otherwise
		 */
		public boolean containsIndex(int i) {
			return indexSet.contains(i);
		}

		@Override
		public WeightedIndex last() {
			lock.readLock().lock();
			final WeightedIndex result = super.last();
			lock.readLock().unlock();
			return result;
		}

		@Override
		public WeightedIndex pollLast() {
			lock.writeLock().lock();
			final WeightedIndex result = super.pollLast();
			indexSet.remove(result.index);
			lock.writeLock().unlock();
			return result;
		}
	}

	/**
	 * Representation of a single element as it is needed in the NNDescend
	 * algorithm. An object is represented as its index, its weight and whether it
	 * is marked or not.
	 *
	 * @author Anton Ballmaier
	 *
	 */
	private static class WeightedIndex {
		/**
		 * index of the object as to {@linkplain KNearestNeighborGraph#nodes}.
		 */
		public int index;

		public boolean marked;
		public double weight;

		/**
		 * Constructs a new {@link WeightedIndex} instance. All parameters are simply
		 * saved to be used later.
		 *
		 * @param index  the index
		 * @param weight the weight
		 * @param marked whether <code>this</code> is marked
		 */
		public WeightedIndex(int index, double weight, boolean marked) {
			this.index = index;
			this.weight = weight;
			this.marked = marked;
		}
	}

	/**
	 * Fraction of the neighborhood size that is used to enhance the neighborhood
	 * approximation
	 */
	private static final double SAMPLE_RATE = 0.9;

	/**
	 * Maximum fraction of neighborhood changes allowed to stop the approximation
	 */
	private static final double TERMINATION_QUOTA = 0.001;

	/**
	 * neighborhoods of every single element
	 */
	private SynchronizedWeightedIndexSet[] neighborhood;

	/**
	 * Counter used to keep track of the amount of updates
	 */
	private SynchronizedCounter updates;

	/**
	 * Constructs a new {@link NearestNeighborDescend} object from the given objects
	 * and measure. This can construct a k nearest neighbor graph.
	 *
	 * @param k       neighborhood size
	 * @param nodes   objects to be compared
	 * @param measure measure to compare the given objects
	 */
	public NearestNeighborDescend(int k, T[] nodes, Measure<T> measure) {
		super(k, nodes, measure);
	}

	/**
	 * Constructs a nearest neighbor graph and returns it as {@link AdjacencyList}.
	 * <p>
	 * The constructed neighborhood graph is only approximated and might not be
	 * completely accurate. Especially if the resulting graph is not connected
	 * errors may occur. Therefore consider constructing a neighborhood graph using
	 * a larger neighborhood size first. This will make the following construction
	 * of graphs for smaller neighborhoods more accurate, since the results from
	 * previous calculations are reused.
	 *
	 * @return the nearest neighbor graph
	 */
	@SuppressWarnings("unchecked")
	@Override
	public AdjacencyList nnGraph() {
		// use prior results if nn graph was calculated before
		if (neighborhood != null && neighborhood[0].size() >= k) {
			return adjacencyFromNeighborhood();
		}

		final int n = nodes.length;
		final int sampleSize = (int) Math.max(1, Math.ceil(k * SAMPLE_RATE));

		// The following 4 sets will always contain only samples of the entire estimate
		final Set<Integer>[] nextNNEstimate = new Set[n];
		final Set<Integer>[] currentNNEstimate = new Set[n];
		final Set<Integer>[] nextNNEstimateReversed = new Set[n];
		final Set<Integer>[] currentNNEstimateReversed = new Set[n];
		updates = new SynchronizedCounter();

		for (int i = 0; i < n; i++) {
			nextNNEstimate[i] = new LinkedHashSet<>();
			nextNNEstimateReversed[i] = new LinkedHashSet<>();
			currentNNEstimate[i] = new LinkedHashSet<>();
			currentNNEstimateReversed[i] = new LinkedHashSet<>();
		}

		initalizeNeighborhood();

		// Iteratively enhance nearest neighbor estimations
		do {
			// Rest update counter
			updates.reset();

			// Sample estimates in parallel
			IntStream.range(0, n).parallel().forEach(v -> {
				for (final WeightedIndex neighbor : neighborhood[v]) {
					// Take marked vertices for next estimate, others for current
					if (neighbor.marked) {
						nextNNEstimate[v].add(neighbor.index);
					} else {
						currentNNEstimate[v].add(neighbor.index);
					}
				}

				// Cap next sample size
				nextNNEstimate[v] = new LinkedHashSet<>(randomSample(nextNNEstimate[v], sampleSize));

				// Remove mark from next sampled items
				for (final WeightedIndex neighbor : neighborhood[v]) {
					if (nextNNEstimate[v].contains(neighbor.index)) {
						neighbor.marked = false;
					}
				}
			});

			// calculate reversed versions of estimates
			for (int i = 0; i < n; i++) {
				currentNNEstimateReversed[i].clear();
				nextNNEstimateReversed[i].clear();
			}
			for (int i = 0; i < n; i++) {
				for (final int v : currentNNEstimate[i]) {
					currentNNEstimateReversed[v].add(i);
				}
				for (final int v : nextNNEstimate[i]) {
					nextNNEstimateReversed[v].add(i);
				}
			}

			// Update NN heaps based on the sampled items in parallel
			// Note that the update counter and the kNN heaps are synchronized.
			IntStream.range(0, n).parallel().forEach(v -> {
				// Sample in some of the reversed items of the samples
				currentNNEstimate[v].addAll(randomSample(currentNNEstimateReversed[v], sampleSize));
				nextNNEstimate[v].addAll(randomSample(nextNNEstimateReversed[v], sampleSize));

				// Try to update nearest neighbor heaps for all pairs u1, u2 with:
				// u1 in nextNNEstimate[v] and...
				for (final int u1 : nextNNEstimate[v]) {
					// ...u2 in nextNNEstimate[v] and u1 < u2
					for (final int u2 : nextNNEstimate[v]) {
						if (u2 >= u1) {
							break;
						}
						tryNearestNeighborUpdates(u1, u2);
					}

					// ...u2 in currentNNEstimate[v] and u1 != u2
					for (final int u2 : currentNNEstimate[v]) {
						if (u2 == u1) {
							continue;
						}
						tryNearestNeighborUpdates(u1, u2);
					}
				}
			});

			// Repeat until few enough updates have been made
		} while (updates.get() > TERMINATION_QUOTA * n * k);

		return adjacencyFromNeighborhood();
	}

	/**
	 * Constructs an {@link AdjacencyList} from the internal graph representation.
	 *
	 * @return the {@link AdjacencyList} representing the generated neighborhood
	 *         graph
	 */
	private AdjacencyList adjacencyFromNeighborhood() {
		final AdjacencyList adjacency = new AdjacencyList(nodes.length);

		for (int i = 0; i < nodes.length; i++) {
			int j = 0;
			for (final WeightedIndex v : neighborhood[i]) {
				if (j >= k) {
					break;
				}
				adjacency.addEdge(i, v.index, v.weight);
				j++;
			}
		}

		return adjacency;
	}

	/**
	 * Initializes the neighborhood of each node with random other nodes.
	 */
	private void initalizeNeighborhood() {
		final int n = nodes.length;
		final int randomSamples = k;

		neighborhood = new SynchronizedWeightedIndexSet[n];
		for (int i = 0; i < n; i++) {
			neighborhood[i] = new SynchronizedWeightedIndexSet();
		}

		final int[] vertices = new int[n];
		for (int i = 0; i < n; i++) {
			vertices[i] = i;
		}

		// Initial random sampling:
		for (int i = 0; i < n; i++) {
			// adjusted Yates-Fischer shuffle of first k elements
			int sampleUntil = randomSamples;

			for (int j = 0; j < sampleUntil; j++) {
				final int r = (int) (j + Math.random() * (n - j));
				final int sample = vertices[r];
				vertices[r] = vertices[j];
				vertices[j] = sample;

				if (neighborhood[i].containsIndex(sample)) {
					sampleUntil++;
				} else {
					final double distance = measure.distance(nodes[i], nodes[sample]);
					neighborhood[i].add(new WeightedIndex(sample, distance, true));
				}
			}
		}
	}

	/**
	 * Returns a number of random samples from a given <code>Collection</code>.
	 * Given elements cannot be picked twice.
	 *
	 * @param <S>     the type of elements in the set
	 * @param set     the elements to pick from
	 * @param samples the number of samples to pick.
	 * @return a random sample from the given <code>Collection</code>. If
	 *         <code>samples&gt;=set.size()</code> the given <code>Collection</code>
	 *         is simply returned.
	 */
	private <S> Collection<S> randomSample(Collection<S> set, int samples) {
		if (samples >= set.size()) {
			return set;
		}
		final List<S> selection = new ArrayList<>(set);

		// Yates-Fischer shuffle
		for (int i = 0; i < samples; i++) {
			final int r = (int) (i + Math.random() * (set.size() - i));
			final S sample = selection.get(r);
			selection.set(r, selection.get(i));
			selection.set(i, sample);
		}
		while (selection.size() > samples) {
			selection.remove(selection.size() - 1);
		}

		return selection;
	}

	/**
	 * Tries to perform an update on the neighborhood for two items (defined by
	 * their indices u1 and u2) that take part in a local join.
	 *
	 * <p>
	 * More specifically this method does the following:
	 * <ul>
	 * <li>Test if u1 is in u2's kNN and the other way around.
	 * <li>Update the nearest neighbor heap if one of the u1, u2 has a place in the
	 * others k-neighborhood.
	 * <li>Add to the update counter for the changes in the kNNG.
	 * </ul>
	 *
	 * @param u1 the index of the first element for a potential update
	 * @param u2 the index of the second element for a potential update
	 */
	private void tryNearestNeighborUpdates(int u1, int u2) {
		final boolean c12 = !neighborhood[u1].containsIndex(u2);
		final boolean c21 = !neighborhood[u2].containsIndex(u1);
		if (!c12 && !c21) {
			return;
		}

		WeightedIndex worstNeighbor1 = null, worstNeighbor2 = null;
		double maxWeight = 0;

		if (c12) {
			worstNeighbor1 = neighborhood[u1].last();
			maxWeight = worstNeighbor1.weight;
		}
		if (c21) {
			worstNeighbor2 = neighborhood[u2].last();
			maxWeight = Math.max(worstNeighbor2.weight, maxWeight);
		}

		final double distance = measure.distanceCapped(nodes[u1], nodes[u2], maxWeight);
		if (distance == Double.POSITIVE_INFINITY) {
			return;
		}

		if (c12) {
			updateNearestNeighbors(u1, u2, distance, worstNeighbor1);
		}
		if (c21) {
			updateNearestNeighbors(u2, u1, distance, worstNeighbor2);
		}
	}

	/**
	 * Checks whether a new neighbor with a given distance should be in the nearest
	 * neighbors and (if applicable) replaces the worst (meaning farthest) neighbor
	 * in that heap.
	 *
	 * @param node          the index of the node whose neighborhood might be
	 *                      updated
	 * @param newNeighbor   the index of the node that might be added as a new
	 *                      neighbor
	 * @param distance      the distance of the two nodes in question
	 * @param worstNeighbor the current farthest nearest neighbor
	 */
	private void updateNearestNeighbors(int node, int newNeighbor, double distance, WeightedIndex worstNeighbor) {
		if (distance < worstNeighbor.weight) {
			neighborhood[node].pollLast();
			neighborhood[node].add(new WeightedIndex(newNeighbor, distance, true));
			updates.add();
		}
	}
}
