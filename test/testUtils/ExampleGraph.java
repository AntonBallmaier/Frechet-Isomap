package testUtils;

import embedding.shortestPaths.AdjacencyList;

public class ExampleGraph {

	public static AdjacencyList exampleGraphList() {
		final AdjacencyList al = new AdjacencyList(9);
		al.addEdge(0, 1, 14);
		al.addEdge(0, 3, 22);
		al.addEdge(0, 4, 4);
		al.addEdge(1, 2, 16);
		al.addEdge(1, 6, 3);
		al.addEdge(2, 3, 12);
		al.addEdge(3, 4, 12);
		al.addEdge(4, 5, 10);

		al.addEdge(7, 8, 5);

		return al;
	}

	public static double[][] shortestPaths() {
		final double infinity = Double.POSITIVE_INFINITY;
		return new double[][] { { 0, 14.0, 28.0, 16.0, 4.0, 14.0, 17.0, infinity, infinity },
				{ 14.0, 0, 16.0, 28.0, 18.0, 28.0, 3.0, infinity, infinity },
				{ 28.0, 16.0, 0, 12.0, 24.0, 34.0, 19.0, infinity, infinity },
				{ 16.0, 28.0, 12.0, 0, 12.0, 22.0, 31.0, infinity, infinity },
				{ 4.0, 18.0, 24.0, 12.0, 0, 10.0, 21.0, infinity, infinity },
				{ 14.0, 28.0, 34.0, 22.0, 10.0, 0, 31.0, infinity, infinity },
				{ 17.0, 3.0, 19.0, 31.0, 21.0, 31.0, 0, infinity, infinity },
				{ infinity, infinity, infinity, infinity, infinity, infinity, infinity, 0, 5.0 },
				{ infinity, infinity, infinity, infinity, infinity, infinity, infinity, 5.0, 0 } };
	}
}
