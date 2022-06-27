package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FrechetDeciderTabularTest {

	private Polygon[] createPolygons() {
		final double[][][] polygonData = new double[][][] { { { 1 }, { 2 }, { 4 }, { 5 } },
				{ { 1 }, { 2 }, { 4 }, { 5 } }, { { 2 }, { 5 }, { 1 }, { 5 } }, { { 1 }, { 4 } } };
		final Polygon[] polygons = new Polygon[polygonData.length];
		for (int i = 0; i < polygonData.length; i++) {
			polygons[i] = new Polygon(polygonData[i]);
		}
		return polygons;
	}

	@Test
	void decideDistanceTest() {
		final Polygon[] polygons = createPolygons();

		FrechetDeciderTabular decider = new FrechetDeciderTabular(polygons[0], polygons[1]);
		assertTrue(decider.decideDistance(0));
		assertTrue(decider.decideDistance(1));

		decider = new FrechetDeciderTabular(polygons[0], polygons[2]);
		assertFalse(decider.decideDistance(0.5));
		assertFalse(decider.decideDistance(1.9));
		assertTrue(decider.decideDistance(2));
		assertTrue(decider.decideDistance(3));

		decider = new FrechetDeciderTabular(polygons[0], polygons[3]);
		assertFalse(decider.decideDistance(0.5));

	}

}
