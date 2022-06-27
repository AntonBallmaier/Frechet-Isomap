package data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import frechetDistance.Polygon;

class ConstructedDataTest {

	@Test
	void randomWalksTest() {
		final Polygon[] polygons = ConstructedData.randomWalks(50);
		for (final Polygon polygon : polygons) {
			for (int d = 0; d < polygon.dimension; d++) {
				double last = 0;
				for (int i = 0; i < polygon.length; i++) {
					final double current = polygon.getVertex(i)[d];
					assertTrue(last + 1 > current);
					last = current;
				}
			}
		}
	}

	@Test
	void shiftedSpikesTest() {
		assertEquals(400, ConstructedData.shiftedSpikes(0.99, 0.1).length);
		assertEquals(6400, ConstructedData.shiftedSpikes(1.99, 0.05).length);

		final Polygon[] polygons = ConstructedData.shiftedSpikes(1, 0.1);
		assertEquals(2, polygons[0].dimension);
		assertEquals(5, polygons[0].length);
	}
}
