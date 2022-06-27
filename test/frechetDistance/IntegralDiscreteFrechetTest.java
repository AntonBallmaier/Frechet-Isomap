package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IntegralDiscreteFrechetTest {

	@Test
	void distanceTest() {
		final Polygon[] polygons = createPolygons();
		final IntegralDiscreteFrechet idf = IntegralDiscreteFrechet.getInstance();

		assertEquals(1, idf.distance(polygons[0], polygons[1]));
		assertEquals(2, idf.distance(polygons[0], polygons[2]));
		assertEquals(6, idf.distance(polygons[0], polygons[3]));
	}

	@Test
	void singletonTest() {
		assertEquals(IntegralDiscreteFrechet.getInstance(), IntegralDiscreteFrechet.getInstance());
	}

	private Polygon[] createPolygons() {
		final double[][][] polygonData = new double[][][] { { { 1 }, { 2 }, { 3 }, { 4 }, { 5 } },
				{ { 1 }, { 2 }, { 4 }, { 5 } }, { { 1 }, { 1.5 }, { 2.5 }, { 3.5 }, { 4.5 }, { 5 } },
				{ { 1 }, { 5 }, { 1 }, { 5 } } };
		final Polygon[] polygons = new Polygon[polygonData.length];
		for (int i = 0; i < polygonData.length; i++) {
			polygons[i] = new Polygon(polygonData[i]);
		}
		return polygons;
	}

//	@Test
//	void measureTest() {
//		final Polygon[] polygons = createPolygons();
//		final DirectMeasure<Polygon> measure = IntegralDiscreteFrechet.measure;
//
//		assertEquals(1, measure.distance(polygons[0], polygons[1]));
//		assertEquals(2, measure.distance(polygons[0], polygons[2]));
//		assertEquals(6, measure.distance(polygons[0], polygons[3]));
//	}

}
