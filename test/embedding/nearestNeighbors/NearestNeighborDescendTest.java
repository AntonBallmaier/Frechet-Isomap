package embedding.nearestNeighbors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import embedding.measures.DirectMeasure;
import embedding.shortestPaths.AdjacencyList;

class NearestNeighborDescendTest {

	@Test
	void errorDistanceTest() {
		final int nodes = 10000, dimensions = 3, k = 5;
		final Double[][] points = new Double[nodes][dimensions];
		for (int i = 0; i < nodes; i++) {
			for (int j = 0; j < dimensions; j++) {
				points[i][j] = Math.random();
			}
		}
		final DirectMeasure<Double[]> euclideanMeasure = (a, b) -> {
			double sum = 0;
			for (int i = 0; i < dimensions; i++) {
				final double diff = (a[i] - b[i]);
				sum += diff * diff;
			}
			return Math.sqrt(sum);
		};

		final KNearestNeighborGraph<Double[]> bf = new NearestNeighborBruteForce<>(k, points, euclideanMeasure);
		final AdjacencyList exact = bf.nnGraph();

		final KNearestNeighborGraph<Double[]> descend = new NearestNeighborDescend<>(k, points, euclideanMeasure);
		final AdjacencyList approx = descend.nnGraph();

		double exactTotalDist = 0, exactEdges = 0;
		double approxTotalDist = 0, approxEdges = 0;
		for (int i = 0; i < nodes; i++) {
			for (final int j : exact.neighbors(i)) {
				exactTotalDist += exact.distance(i, j);
				exactEdges++;
			}
			for (final int j : approx.neighbors(i)) {
				approxTotalDist += approx.distance(i, j);
				approxEdges++;
			}
		}
		assertEquals(1, (approxTotalDist / approxEdges) / (exactTotalDist / exactEdges), 0.01);
	}

}
