package embedding.shortestPaths;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import testUtils.ExampleGraph;

class DijkstraAPSPTest {

	@Test
	void testShortestPaths() {
		final double[][] shortestPaths = new DijkstraAPSP(ExampleGraph.exampleGraphList()).shortestPaths();
		Assert.assertArrayEquals(ExampleGraph.shortestPaths(), shortestPaths);
	}

}
