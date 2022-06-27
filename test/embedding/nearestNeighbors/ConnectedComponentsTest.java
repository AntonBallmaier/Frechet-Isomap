package embedding.nearestNeighbors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import embedding.measures.DirectMeasure;
import embedding.shortestPaths.AdjacencyList;
import testUtils.ExampleGraph;

class ConnectedComponentsTest {

	@Test
	void connectTest() {
		// Simple case: 2 components
		AdjacencyList al = ExampleGraph.exampleGraphList();
		ConnectedComponents cc = new ConnectedComponents(al);
		assertFalse(cc.isConnected());
		DirectMeasure<Integer> measure = (a, b) -> {
			return Math.abs(a - 4) + Math.abs(b - 7) + 2;
		};
		cc.connect(measure);
		assertEquals(2, al.distance(4, 7));
		assertTrue(cc.isConnected());

		// More difficult case
		al = new AdjacencyList(5);
		cc = new ConnectedComponents(al);
		measure = (a, b) -> {
			if (b < a) {
				final int tmp = a;
				a = b;
				b = tmp;
			}

			if (a == 0 && b != 4) {
				return b;
			} else if (a == 3 && b == 4) {
				return 2;
			} else {
				return 3 + Math.random();
			}
		};
		cc.connect(measure);
		cc.connect(measure);
		assertTrue(cc.isConnected());
		assertEquals(1, al.distance(0, 1));
		assertEquals(2, al.distance(0, 2));
		assertEquals(3, al.distance(0, 3));
		assertEquals(2, al.distance(3, 4));

		// Count total connections:
		int connections = 0;
		for (int i = 0; i < al.length - 1; i++) {
			for (int j = i + 1; j < al.length; j++) {
				if (Double.isFinite(al.distance(i, j))) {
					connections++;
				}
			}
		}
		assertEquals(4, connections);

	}

	@Test
	void getComponentsTest() {
		final ConnectedComponents cc = new ConnectedComponents(ExampleGraph.exampleGraphList());
		final List<List<Integer>> components = cc.getComponents();
		assertEquals(2, components.size());
		assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6), new HashSet<>(cc.mainComponent()));
		assertEquals(Set.of(0, 1, 2, 3, 4, 5, 6), new HashSet<>(components.get(0)));
		assertEquals(Set.of(7, 8), new HashSet<>(components.get(1)));
	}

}
