package embedding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import testUtils.TestUtils;

class MultidimensionalScalingTest {

	@Test
	void classicalMDSExactTest() {
		// Perfectly on a line:
		final double[][] distances = new double[][] { { 0, 6, 7, 9 }, { 6, 0, 1, 3 }, { 7, 1, 0, 2 }, { 9, 3, 2, 0 } };
		final double[][] embedding = MultidimensionalScaling.classical(distances, 1);
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				assertEquals(distances[i][j], Math.abs(embedding[0][i] - embedding[0][j]), 1e-4);
			}
		}
	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 3, 5, 15 })
	void classicalMDSRandomDistancesTest(int dimension) {
		final int points = dimension + 1 + (int) (5 * Math.random() * dimension);

		final double[][] coordinates = new double[points][dimension];
		for (int i = 0; i < points; i++) {
			for (int j = 0; j < dimension; j++) {
				coordinates[i][j] = Math.random();
			}
		}

		final double[][] distances = new double[points][points];
		for (int i = 0; i < points; i++) {
			for (int j = 0; j < i; j++) {
				distances[i][j] = distances[j][i] = TestUtils.euclideanDistance(coordinates[i], coordinates[j]);
			}
		}

		final double[][] embedding = MultidimensionalScaling.classical(distances, dimension);
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances.length; j++) {
				double d = 0;
				double diff;
				for (int k = 0; k < dimension; k++) {
					diff = embedding[k][i] - embedding[k][j];
					d += diff * diff;
				}
				final double embeddingDistance = Math.sqrt(d);

				assertEquals(distances[i][j], embeddingDistance, 1e-4);
			}
		}
	}

	@Test
	void illegalArgumentExceptionsTest() {
		final double[][] notRectangular = new double[][] { { 1, 2, 3 }, { 1, 2 } };
		final double[][] notDefinite = new double[][] { { 0, 1, 2 }, { 1, 5, 3 }, { 2, 3, 0 } };
		final double[][] notPositive = new double[][] { { 0, 1, 2 }, { 1, 0, -3 }, { 2, -3, 0 } };
		final double[][] notQuadratic = new double[][] { { 0, 1, 2 }, { 1, 0, 3 }, { 2, 3, 0 }, { 4, 5, 6 } };
		final double[][] wrongRectangular = new double[][] { { 0, 1, 2 }, { 1, 0, 3 } };

		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(null, 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(new double[4][], 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(new double[4][4], 5));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(notRectangular, 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(notDefinite, 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(notPositive, 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(notQuadratic, 1));
		assertThrows(IllegalArgumentException.class, () -> MultidimensionalScaling.classical(wrongRectangular, 1));

	}

	@ParameterizedTest
	@ValueSource(ints = { 1, 3, 5, 15 })
	void randomLandmarkTest(int dimensions) {
		final int dimension = 5;
		final int points = dimensions * 20;
		final int landmarks = dimension * 3;

		final Random r = new Random();
		r.setSeed(0);

		final double[][] coordinates = new double[points][dimension];
		for (int i = 0; i < points; i++) {
			for (int j = 0; j < dimension; j++) {
				coordinates[i][j] = r.nextDouble();
			}
		}

		final double[][] distances = new double[points][landmarks];
		for (int i = 0; i < points; i++) {
			for (int j = 0; j < landmarks; j++) {
				distances[i][j] = TestUtils.euclideanDistance(coordinates[i], coordinates[j]);
			}
		}

		final double[][] embedding = MultidimensionalScaling.landmark(distances, dimension);
		for (int i = 0; i < distances.length; i++) {
			for (int j = 0; j < distances[i].length; j++) {
				double d = 0;
				double diff;
				for (int k = 0; k < dimension; k++) {
					diff = embedding[k][i] - embedding[k][j];
					d += diff * diff;
				}
				final double embeddingDistance = Math.sqrt(d);

				assertEquals(distances[i][j], embeddingDistance, 1e-4);
			}
		}

	}

}
