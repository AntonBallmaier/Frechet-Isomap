package embedding.nearestNeighbors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import embedding.measures.DirectMeasure;
import embedding.shortestPaths.AdjacencyList;

class NearestNeighborBruteForceTest {

	@Test
	void linearTest() {
		final DirectMeasure<Integer> measure = (a, b) -> Math.abs(a - b);
		final NearestNeighborBruteForce<Integer> nn = new NearestNeighborBruteForce<>(2,
				new Integer[] { 0, 1, 2, 3, 4, 5 }, measure);
		final AdjacencyList nnGraph = nn.nnGraph();
		for (int i = 0; i <= 5; i++) {
			for (int j = 0; j <= 5; j++) {
				if (i == j) {
					continue;
				}
				if (Math.abs(i - j) == 1) {
					assertEquals(1, nnGraph.distance(i, j));
				} else if (Math.abs(i - j) == 2 && (i == 0 || j == 0 || i == 5 || j == 5)) {
					assertEquals(2, nnGraph.distance(i, j));
				} else {
					assertEquals(Double.POSITIVE_INFINITY, nnGraph.distance(i, j));
				}
			}
		}
	}

}
