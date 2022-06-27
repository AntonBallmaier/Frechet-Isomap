package embedding.shortestPaths;

/**
 * An object able to measure the shortest paths along a weighted graph.
 *
 * <p>
 * This graph is given as an {@link AdjacencyList}.
 *
 * @author Anton Ballmaier
 *
 */
public abstract class AllPairShortestPaths {
	/**
	 * The adjacency list for which shortest paths should be calculated
	 */
	protected final AdjacencyList adjacency;

	/**
	 * Constructs a new {@link AllPairShortestPaths} object from the given
	 * {@link AdjacencyList}. This can solve its all pair shortest paths problem.
	 *
	 * @param adjacency the adjacency list representing a graph
	 */
	public AllPairShortestPaths(AdjacencyList adjacency) {
		this.adjacency = adjacency;
	}

	/**
	 * Calculates the length of the shortest paths from every vertex in the graph to
	 * every other.
	 *
	 * <p>
	 * Since the {@linkplain AdjacencyList} represents an undirected graph, the
	 * resulting matrix will always be symmetrical. If no path is possible their
	 * path distance is <code>Double.POSITIVE_INFINITY</code>.
	 *
	 * @return the shortest path distance for every pair of vertices
	 */
	public abstract double[][] shortestPaths();
}
