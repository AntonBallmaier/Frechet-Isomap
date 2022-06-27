package embedding.shortestPaths;

import java.util.stream.IntStream;

import embedding.shortestPaths.FibonacciHeap.Entry;

/**
 * An object able to measure the shortest paths along a weighted graph. To
 * perform these calculations dijkstras algorithm is used for every vertex in
 * parallel.
 *
 * <p>
 * This algorithm has a high runtime constant but fast asymptotic runtime. If
 * the graph for which the all pair shortest paths problem should be solved has
 * only a few vertices, consider using {@linkplain FloydWarshallAPSP} instead.
 *
 * @author Anton Ballmaier
 *
 */
public class DijkstraAPSP extends AllPairShortestPaths {
	/**
	 * Constructs a new {@link DijkstraAPSP} object from the given
	 * {@link AdjacencyList}. This can solve its all pair shortest paths problem.
	 *
	 * @param adjacency the adjacency list representing a graph
	 */
	public DijkstraAPSP(AdjacencyList adjacency) {
		super(adjacency);
	}

	@Override
	public double[][] shortestPaths() {
		return shortestPaths(adjacency.length);
	}

	/**
	 * Calculates the length of the shortest paths to every vertex in the graph from
	 * a given number of starting vertices.
	 * <p>
	 * As starting points the first vertices of the {@link AdjacencyList} will be
	 * used.
	 *
	 * <p>
	 * Since the {@linkplain AdjacencyList} represents an undirected graph, the
	 * resulting matrix will always be symmetrical for all entries that have a
	 * transpose. If no path is possible their path distance is
	 * <code>Double.POSITIVE_INFINITY</code>.
	 *
	 * @param startingPoints the number of vertices used as starting points to solve
	 *                       the shortest path problem
	 * @return the shortest path distance for every pair of starting point and
	 *         vertex. <code>shortestPaths()[i][j]</code> is length of the shortest
	 *         path from the j<sup>th</sup> starting point to the i<sup>th</sup>
	 *         vertex.
	 */
	public double[][] shortestPaths(int startingPoints) {
		final int n = adjacency.length;
		final double[][] distances = new double[n][startingPoints];

		// Run Dijkstra algorithm for every single vertex as start point in parallel
		IntStream.range(0, startingPoints).parallel().forEach(start -> {
			for (int i = 0; i < n; i++) {
				distances[i][start] = Double.POSITIVE_INFINITY;
			}
			distances[start][start] = 0;

			// Fibonacci Heap to store all vertices that are not yet finalized.
			final FibonacciHeap<Integer> unsettled = new FibonacciHeap<>();

			final boolean[] settled = new boolean[n];

			// Add all vertices.
			@SuppressWarnings("unchecked")
			final Entry<Integer>[] heapEntries = new Entry[n];
			for (int i = 0; i < n; i++) {
				heapEntries[i] = unsettled.enqueue(i, distances[i][start]);
			}

			while (!unsettled.isEmpty()) {
				final Entry<Integer> currentEntry = unsettled.dequeueMin();
				final int current = currentEntry.getValue();
				settled[current] = true;
				for (final int neighbour : adjacency.neighbors(current)) {
					if (!settled[neighbour]) {
						final double alternative = distances[current][start] + adjacency.distance(current, neighbour);
						if (distances[neighbour][start] > alternative) {
							unsettled.decreaseKey(heapEntries[neighbour], alternative);
							distances[neighbour][start] = alternative;
						}
					}
				}
			}
		});
		return distances;
	}
}
