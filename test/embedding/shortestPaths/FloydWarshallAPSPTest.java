package embedding.shortestPaths;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import testUtils.ExampleGraph;

class FloydWarshallAPSPTest {

	@Test
	void testShortestPaths() {
		final double[][] shortestPaths = new FloydWarshallAPSP(ExampleGraph.exampleGraphList()).shortestPaths();
		Assert.assertArrayEquals(ExampleGraph.shortestPaths(), shortestPaths);
	}
}
