package embedding.nearestNeighbors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import embedding.measures.Measure;
import embedding.shortestPaths.AdjacencyList;

/**
 * A {@link ConnectedComponents} instance is used to work with the connected
 * components of a graph given as {@link AdjacencyList}. The graph can be tested
 * for connectedness and be joined if unconnected.
 *
 * @author Anton Ballmaier
 *
 */
public class ConnectedComponents {

	/**
	 * Representation of a single edge in the graph of graph components.
	 *
	 * @author Anton Ballmaier
	 *
	 */
	private class KruskalEdge implements Comparable<KruskalEdge> {
		/**
		 * Distance of the shortest distance from one vertex in component i and one
		 * vertex in component j
		 */
		public double distance;

		/**
		 * First component
		 */
		public int i;

		/**
		 * Vertex in component i that is a part of the shortest join
		 */
		public int indexI;

		/**
		 * Vertex in component j that is a part of the shortest join
		 */
		public int indexJ;

		/**
		 * Second component
		 */
		public int j;

		/**
		 * Constructs a new {@link KruskalEdge} instance. All parameters are simply
		 * saved to be used later.
		 *
		 * @param i        the first component
		 * @param j        the second component
		 * @param distance the distance of the shortest distance from one vertex in
		 *                 component i and one vertex in component j
		 * @param indexI   the vertex in component i that is a part of the shortest join
		 * @param indexJ   the vertex in component j that is a part of the shortest join
		 */
		public KruskalEdge(int i, int j, double distance, int indexI, int indexJ) {
			this.i = i;
			this.j = j;
			this.distance = distance;
			this.indexI = indexI;
			this.indexJ = indexJ;
		}

		@Override
		public int compareTo(KruskalEdge o) {
			if (this.distance < o.distance) {
				return -1;
			} else if (this.distance == o.distance) {
				return 0;
			} else {
				return 1;
			}
		}

	}

	/**
	 * The a adjacency list representing the graph this instance deals with.
	 */
	private final AdjacencyList adjacencyList;

	/**
	 * List of all connected components within the graph. The components are sorted
	 * by size, the first component being the largest component.
	 */
	private List<List<Integer>> components;

	/**
	 * Constructs a new {@link ConnectedComponents} object from an
	 * {@link AdjacencyList}. The components will be identified upon instantiation.
	 *
	 * @param adjacencyList the adjacency list representing the graph this instance
	 *                      deals with
	 */
	public ConnectedComponents(AdjacencyList adjacencyList) {
		this.adjacencyList = adjacencyList;
		findComponents();
	}

	/**
	 * Connects the components of the handled graph to a single component.
	 *
	 * <p>
	 * The connection is constructed as a minimal spanning tree of the component
	 * graph. The added edges in the original graph will each be the shortest
	 * connections possible. Afterwards the graph has only one connected component.
	 *
	 * @param measure the measure used to determine distances between vertices in
	 *                the graph
	 */
	public void connect(Measure<Integer> measure) {
		if (!isConnected()) {
			final LinkedList<KruskalEdge> edges = findShortestComponentConnections(measure);
			connectComponentsMST(edges);

			// Update components list:
			components.clear();
			final ArrayList<Integer> singleComponent = new ArrayList<>(adjacencyList.length);
			for (int i = 0; i < adjacencyList.length; i++) {
				singleComponent.add(i);
			}
			components.add(singleComponent);
		}
	}

	/**
	 * Returns the connected components of the graph.
	 *
	 * @return the connected components of the graph
	 */
	public List<List<Integer>> getComponents() {
		return components;
	}

	/**
	 * Tests whether the graph is connected.
	 *
	 * <p>
	 * This doesn't reflect changes made externally to the {@link AdjacencyList}
	 * used to construct this instance.
	 *
	 * @return <code>true</code> if the graph is connected, <code>false</code>
	 *         otherwise.
	 */
	public boolean isConnected() {
		final boolean result = components.size() == 1;
		return result;
	}

	/**
	 * Returns the vertices of the largest component of the graph.
	 *
	 * @return the vertices of the largest component of the graph
	 */
	public List<Integer> mainComponent() {
		return components.get(0);
	}

	/**
	 * Connects the graph components as sparingly as possible by constructing the
	 * minimal spanning tree from the graph components.
	 *
	 * <p>
	 * This implementation uses kruskals algorithm. Since a graph usually doesn't
	 * have a lot of components, this implementation isn't optimized for asymptotic
	 * runtime.
	 *
	 * @param edges the possible edges between components
	 */
	private void connectComponentsMST(final LinkedList<KruskalEdge> edges) {
		final int[] componentNumbers = new int[components.size()];
		for (int i = 1; i < componentNumbers.length; i++) {
			componentNumbers[i] = i;
		}
		edges.sort(Comparator.naturalOrder());
		while (!edges.isEmpty()) {
			final KruskalEdge next = edges.pollFirst();
			final int oldComponentNumber = componentNumbers[next.j];
			final int newComponentNumber = componentNumbers[next.i];
			if (oldComponentNumber != newComponentNumber) {
				adjacencyList.addEdge(next.indexI, next.indexJ, next.distance);
				for (int i = 0; i < componentNumbers.length; i++) {
					if (componentNumbers[i] == oldComponentNumber) {
						componentNumbers[i] = newComponentNumber;
					}
				}
			}
		}
	}

	/**
	 * Identifies the connected components in the graph.
	 *
	 * <p>
	 * The components are identified by running a depth first search for each vertex
	 * that has not already been assigned to a component. The components are saved
	 * for this instance.
	 */
	private void findComponents() {
		Stack<Integer> stack;
		final int[] numbering = new int[adjacencyList.length];
		int componentNumber = 0;
		components = new ArrayList<>();

		for (int i = 0; i < adjacencyList.length; i++) {
			if (numbering[i] == 0) {
				final List<Integer> currentComponent = new ArrayList<>();
				componentNumber++;
				stack = new Stack<>();
				stack.push(i);
				numbering[i] = componentNumber;
				while (!stack.isEmpty()) {
					final int current = stack.pop();
					currentComponent.add(current);
					for (final int neighbor : adjacencyList.neighbors(current)) {
						if (numbering[neighbor] == 0) {
							stack.push(neighbor);
							numbering[neighbor] = componentNumber;
						}
					}
				}
				components.add(currentComponent);

			}
		}
		components.sort((a, b) -> Integer.compare(b.size(), a.size())); // Is order correct?
	}

	/**
	 * Calculates the closest pairs and their distance for every pair of connected
	 * components in the graph.
	 *
	 * @param measure the measure used to determine distances between vertices in
	 *                the graph
	 * @return the possible edges between components
	 */
	private LinkedList<KruskalEdge> findShortestComponentConnections(Measure<Integer> measure) {
		final LinkedList<KruskalEdge> edges = new LinkedList<>();
		for (int i = 0; i < components.size() - 1; i++) {
			for (int j = i + 1; j < components.size(); j++) {
				double minDistance = Double.POSITIVE_INFINITY;
				int[] bestPair = new int[2];
				for (final int k : components.get(i)) {
					for (final int l : components.get(j)) {
						final double distance = measure.distanceCapped(k, l, minDistance);
						if (distance < minDistance) {
							minDistance = distance;
							bestPair = new int[] { k, l };
						}
					}
				}
				edges.add(new KruskalEdge(i, j, minDistance, bestPair[0], bestPair[1]));
			}
		}
		return edges;
	}
}