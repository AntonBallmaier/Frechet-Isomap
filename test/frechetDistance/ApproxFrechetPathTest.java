package frechetDistance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import data.ConstructedData;

class ApproxFrechetPathTest {

	@Test
	void comparisonTest() throws IOException {
		// Test by comparing to conventional (tabular) fr&#xE9;chet decider
		final Polygon[] polygons = ConstructedData.randomWalks(100);
		double precision = 1e-5;

		final ApproxFrechet path = new ApproxFrechet(precision), tabular = new ApproxFrechet(precision, true);
		precision *= 2; // allow error in both function calls in opposite directions
		for (final Polygon p : polygons) {
			for (final Polygon q : polygons) {
				if (p == q) {
					assertEquals(path.distance(p, q), 0, precision);
				} else {
					final double target = tabular.distance(p, q);
					assertEquals(target, path.distance(p, q), precision);
					assertEquals(target, path.distanceCapped(p, q, target + precision), precision);
					assertEquals(Double.POSITIVE_INFINITY, path.distanceCapped(p, q, target - precision));
				}
			}
		}
	}

	@Test
	void defaultPrecisionTest() {
		final ApproxFrechet af = new ApproxFrechet();
		af.setPrecision(1);
		assertEquals(1, af.getPrecision());
		assertThrows(IllegalArgumentException.class, () -> af.setPrecision(0));
		assertThrows(IllegalArgumentException.class, () -> af.setPrecision(-1));
	}
}
