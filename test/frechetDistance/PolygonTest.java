package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import testUtils.TestUtils;

class PolygonTest {

	@Test
	void constructorTest() {
		for (final int length : new int[] { 2, 3, 10 }) {
			for (final int dimension : new int[] { 1, 2, 5 }) {
				final Polygon p = new Polygon(TestUtils.randomPolygonData(dimension, length));
				assertEquals(length, p.length);
				assertEquals(dimension, p.dimension);
			}
		}
	}

	@Test
	void cornerDistanceTest() {
		final Polygon a = new Polygon(new double[][] { { 0 }, { 3 }, { 2 }, { 4 } }),
				b = new Polygon(new double[][] { { 1 }, { -10 } }),
				c = new Polygon(new double[][] { { 1, 0 }, { 2, 1 }, { 3, 1 } }),
				d = new Polygon(new double[][] { { 0, 0 }, { 2, 1 }, { 3, 0 } });

		assertThrows(IllegalArgumentException.class, () -> a.cornerDistance(c, 0, 0));
		assertEquals(14, a.cornerDistance(b, 3, 1));
		assertEquals(Math.sqrt(2), c.cornerDistance(d, 1, 2));

	}

	@Test
	void getVertexTest() {
		final double[][] polygonData = TestUtils.randomPolygonData(3, 3);
		final Polygon p = new Polygon(polygonData);
		final double[] startPoint = p.getVertex(0);
		assertTrue(Arrays.equals(polygonData[0], startPoint));
		startPoint[0] = 0;
		assertTrue(Arrays.equals(polygonData[0], p.getVertex(0)));
		assertThrows(IndexOutOfBoundsException.class, () -> p.getVertex(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> p.getVertex(Integer.MAX_VALUE));
	}

	@Test
	void illegalConstructorInputTest() {
		final double[][][] illegalInputs = new double[][][] { null, { null, TestUtils.randomPoint(2) },
				{ TestUtils.randomPoint(1), TestUtils.randomPoint(1), null, TestUtils.randomPoint(1) },
				{ TestUtils.randomPoint(2), TestUtils.randomPoint(3), TestUtils.randomPoint(2) },
				{ TestUtils.randomPoint(3) } };

		for (final double[][] input : illegalInputs) {
			assertThrows(IllegalArgumentException.class, () -> new Polygon(input));
		}
	}

	@Test
	void longestSegmentTest() {
		final double[][][] polygons = new double[][][] { { { 0 }, { 1 }, { 3 }, { 4 } },
				{ { 1, 0 }, { 2, 1 }, { 3, 1 } }, { { 1, 2, 3, 4, 5 }, { -1, 4, 1, 6, 3 } } };

		final double[] expected = new double[] { 2, Math.sqrt(2), 2 * Math.sqrt(5) };

		for (int i = 0; i < polygons.length; i++) {
			final Polygon p = new Polygon(polygons[i]);
			assertEquals(expected[i], p.longestSegment(), 0.0001);
		}
	}

}
