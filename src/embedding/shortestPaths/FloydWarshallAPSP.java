package embedding.shortestPaths;

/**
 * An object able to measure the shortest paths along a weighted graph. To
 * perform these calculations the floyd-warshall algorithm is used.
 *
 * <p>
 * This algorithm has a low runtime constant but not so fast asymptotic runtime.
 * If the graph for which the all pair shortest paths problem should be solved
 * has many vertices, consider using {@linkplain DijkstraAPSP} instead.
 *
 * @author Anton Ballmaier
 *
 */
public class FloydWarshallAPSP extends AllPairShortestPaths {
	/**
	 * Constructs a new {@link FloydWarshallAPSP} object from the given
	 * {@link AdjacencyList}. This can solve its all pair shortest paths problem.
	 *
	 * @param adjacency the adjacency list representing a graph
	 */
	public FloydWarshallAPSP(AdjacencyList adjacency) {
		super(adjacency);
	}

	@Override
	public double[][] shortestPaths() {
		double repath;
		final int n = adjacency.length;
		final double[][] distances = adjacency.toAdjacencyMatrix();

		// floyd warshall
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					repath = distances[i][k] + distances[k][j];
					if (distances[i][j] > repath) {
						distances[i][j] = repath;
					}
				}
			}
		}

		return distances;
	}
}