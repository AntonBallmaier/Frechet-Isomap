package embedding.shortestPaths;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

class AdjacencyListTest {

	@Test
	void adjacencyMatrixTest() {
		final AdjacencyList al = new AdjacencyList(4);
		al.addEdge(0, 1, 4);
		al.addEdge(0, 2, 5);
		final double infinity = Double.POSITIVE_INFINITY;
		final double[][] adjacencyMatrix = new double[][] { { 0, 4, 5, infinity }, { 4, 0, infinity, infinity },
				{ 5, infinity, 0, infinity }, { infinity, infinity, infinity, 0 } };

		assertArrayEquals(adjacencyMatrix, al.toAdjacencyMatrix());
	}

	@Test
	void distanceTest() {
		final AdjacencyList al = new AdjacencyList(3);
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (i == j) {
					assertEquals(0, al.distance(i, j));
				} else {
					assertEquals(Double.POSITIVE_INFINITY, al.distance(i, j));
				}
			}
		}

		al.addEdge(0, 1, 4);
		assertEquals(4, al.distance(0, 1));
		assertEquals(4, al.distance(1, 0));
	}

	@Test
	void errorTest() {
		final AdjacencyList al = new AdjacencyList(2);
		assertThrows(IllegalArgumentException.class, () -> al.addEdge(0, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> al.addEdge(0, 1, -1));
	}

	@Test
	void neighbors() {
		final AdjacencyList al = new AdjacencyList(4);
		al.addEdge(0, 1, 4);
		al.addEdge(0, 2, 5);

		assertEquals(Set.of(new Integer[] { 1, 2 }), al.neighbors(0));
		assertEquals(Set.of(new Integer[] { 0 }), al.neighbors(1));
		assertEquals(Set.of(new Integer[] { 0 }), al.neighbors(2));
		assertEquals(new HashSet<>(), al.neighbors(3));
	}

	@Test
	void removeEdgeTest() {
		final AdjacencyList al = new AdjacencyList(3);
		al.addEdge(0, 1, 4);
		al.addEdge(0, 2, 5);
		al.addEdge(0, 0, 0);
		assertEquals(4, al.distance(0, 1));
		assertEquals(5, al.distance(0, 2));

		al.removeEdge(0, 1);
		al.addEdge(0, 2, Double.POSITIVE_INFINITY);
		assertEquals(Double.POSITIVE_INFINITY, al.distance(0, 1));
		assertEquals(Double.POSITIVE_INFINITY, al.distance(0, 2));
		assertEquals(0, al.neighbors(0).size());
	}

}
