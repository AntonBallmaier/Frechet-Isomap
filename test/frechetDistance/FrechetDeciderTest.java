package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class FrechetDeciderTest {

	@Test
	void endpointTest() {
		// Use FrechetDeciderTabular implementation to test

		final Polygon a = new Polygon(new double[][] { { 0 }, { 1 } });
		final Polygon b = new Polygon(new double[][] { { 0 }, { -4 }, { 6 }, { 2 } });
		final Polygon c = new Polygon(new double[][] { { 1 }, { 4 }, { 1 } });

		final FrechetDecider ab = new FrechetDeciderTabular(a, b);
		assertTrue(ab.endpointsToFar(0.999));
		assertFalse(ab.endpointsToFar(1));
		assertFalse(ab.endpointsToFar(1.001));

		final FrechetDecider bc = new FrechetDeciderTabular(b, c);
		assertFalse(bc.endpointsToFar(2));
		assertTrue(bc.endpointsToFar(0.5));

	}

	@Test
	void freeIntervalTest() {
		final Polygon oneDimensional = new Polygon(new double[][] { { 0 }, { 1 } });
		final Polygon a = new Polygon(new double[][] { { 0, 0 }, { 1, 1 } });
		final Polygon b = new Polygon(new double[][] { { 0, 1 }, { 1, 0 } });
		final Polygon c = new Polygon(new double[][] { { 0, 1 }, { 1, 1 } });
		final Polygon d = new Polygon(new double[][] { { 1, 0 }, { 0, 0.5 } });
		final Polygon e = new Polygon(new double[][] { { 5, 2 }, { -4, -4 } });
		final Polygon f = new Polygon(new double[][] { { 1, 0.5 }, { 2, 0.5 } });
		final Polygon g = new Polygon(new double[][] { { 2, 0.5 }, { 1, 0.5 } });

		assertThrows(IllegalArgumentException.class, () -> FrechetDecider.freeInterval(oneDimensional, 0, a, 0, 1));
		assertArrayEquals(new double[] { 0.2, 0.8 }, FrechetDecider.freeInterval(a, 0, b, 0, 0.82462), 0.0001);
		assertArrayEquals(new double[] { 0, 0.5 }, FrechetDecider.freeInterval(a, 0, c, 0, 1.11803), 0.0001);
		assertArrayEquals(new double[] { 0.5, 1 }, FrechetDecider.freeInterval(a, 0, d, 0, 0.55902), 0.0001);
		assertArrayEquals(new double[] { 0, 1 }, FrechetDecider.freeInterval(a, 0, e, 0, 6), 0.0001);
		assertNull(FrechetDecider.freeInterval(a, 0, b, 0, 0.5));
		assertNull(FrechetDecider.freeInterval(a, 0, f, 0, 1));
		assertNull(FrechetDecider.freeInterval(a, 0, g, 0, 1));

	}

}
