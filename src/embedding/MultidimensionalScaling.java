package embedding;

import java.util.Arrays;

import mdsj.MDSJ;
import smile.mds.MDS;

/**
 * This class contains methods for constructing embeddings from distance
 * matrices using classical and landmark MDS.
 *
 * <p>
 * Performing the classical MDS is based on the implementation in {@link MDSJ}.
 *
 * @author Anton Ballmaier
 *
 */
public class MultidimensionalScaling {

	/**
	 * Performs classical MDS using a quadratic distance matrix. This matrix must be
	 * positive definite, meaning it must contain only positive entries and
	 * <code>distances[i][i]</code> must always be 0.
	 *
	 * @param distances  the quadratic distance matrix
	 * @param dimensions the number of dimensions of the embedding
	 * @return the embedding in euclidean space with the given dimensions.
	 *         <code>classical(...)[i][j]</code> is the i<sup>th</sup> component of
	 *         the j<sup>th</sup> embedding vector.
	 * @throws IllegalArgumentException if the given matrix or any of its rows is
	 *                                  <code>null</code>
	 * @throws IllegalArgumentException if the matrix is not quadratic
	 * @throws IllegalArgumentException if the matrix is not positive definite
	 * @throws IllegalArgumentException if the number of embedding dimensions is
	 *                                  greater than the size of the distance matrix
	 *
	 */
	public static double[][] classical(double[][] distances, int dimensions) {
		checkInputClassical(distances, dimensions);
		final MDS mds = MDS.of(distances, dimensions);

		// Transpose to get correct output format
		final double transpose[][] = new double[dimensions][distances.length];
		for (int i = 0; i < dimensions; i++) {
			for (int j = 0; j < distances.length; j++) {
				transpose[i][j] = mds.coordinates[j][i];
			}
		}

		return transpose;
	}

	/**
	 * Performs landmark MDS using a distance matrix giving distances from everyp
	 * oint to every landmark. This matrix must be positive definite, meaning it
	 * must contain only positive entries and <code>distances[i][i]</code> must
	 * always be 0. The number of landmarks is given by
	 * <code>distances[i].length</code>.
	 *
	 * @param distances  the distance matrix
	 * @param dimensions the number of dimensions of the embedding
	 * @return the embedding in euclidean space with the given dimensions.
	 *         <code>landmark(...)[i][j]</code> is the i<sup>th</sup> component of
	 *         the j<sup>th</sup> embedding vector.
	 * @throws IllegalArgumentException if the given matrix or any of its rows is
	 *                                  <code>null</code>
	 * @throws IllegalArgumentException if the matrix is not rectangular
	 * @throws IllegalArgumentException if the matrix is not positive definite
	 * @throws IllegalArgumentException if the number of embedding dimensions is
	 *                                  greater than the size of the distance matrix
	 *
	 */
	public static double[][] landmark(double[][] distances, int dimensions) {
		checkInputLandmark(distances, dimensions);
		// Assume landmarks are random.
		final int landmarks = distances[0].length;
		final int total = distances.length;

		final double[][] landmarkDistances = Arrays.copyOf(distances, landmarks);
		final double[][] landmarkEmbedding = classical(landmarkDistances, dimensions);

		final double[][] squareDistances = new double[total][landmarks];
		final double[][] embedding = new double[dimensions][total];

		// Square distances
		for (int i = 0; i < total; i++) {
			for (int j = 0; j < landmarks; j++) {
				squareDistances[i][j] = distances[i][j] * distances[i][j];
			}
		}

		for (int i = 0; i < landmarks; i++) {
			// Calculate the mean of the i-th squared landmark distances
			double mean = 0;
			for (int j = 0; j < landmarks; j++) {
				mean += squareDistances[j][i];
			}
			mean /= landmarks;

			// subtract mean from distance vectors
			for (int j = landmarks; j < distances.length; j++) {
				squareDistances[j][i] -= mean;
			}
		}

		for (int i = 0; i < dimensions; i++) {
			// Calculate i-th eigenvalue
			double eigenvalue = 0;
			for (int j = 0; j < landmarks; j++) {
				eigenvalue += landmarkEmbedding[i][j] * landmarkEmbedding[i][j];
			}
			// If the eigenvalue is really small, the transformation isn't numerically
			// stable but would theoretically have a minor influence on the result, since
			// such a small eigenvalue is meaning the dimension isn't really needed for a
			// good embedding. Setting it to Infinity in this case is the same as just not
			// transforming this dimension.
			if (eigenvalue < 0.01) {
				eigenvalue = Double.POSITIVE_INFINITY;
			}
			for (int j = 0; j < landmarks; j++) {
				// Calculate pseudo-inverse transpose per entry and use that for embedding
				// (implicit matrix multiplication)
				final double transformation = landmarkEmbedding[i][j] / eigenvalue;
				for (int k = landmarks; k < total; k++) {
					embedding[i][k] -= 0.5 * transformation * squareDistances[k][j];
				}

				// Copy landmark embedding
				embedding[i][j] = landmarkEmbedding[i][j];
			}
		}

		return embedding;
	}

	/**
	 * Tests if the given parameters are valid for classical MDS and throws an
	 * <code>IllegalArgumentException</code> otherwise.
	 *
	 * @param distances  the distance matrix
	 * @param dimensions the number of dimensions of the embedding
	 * @throws IllegalArgumentException if the given matrix or any of its rows is
	 *                                  <code>null</code>
	 * @throws IllegalArgumentException if the matrix is not quadratic
	 * @throws IllegalArgumentException if the matrix is not positive definite
	 * @throws IllegalArgumentException if the number of embedding dimensions is
	 *                                  greater than the size of the distance matrix
	 */
	private static void checkInputClassical(double[][] distances, int dimensions) throws IllegalArgumentException {
		checkInputLandmark(distances, dimensions);
		if (distances[0].length != distances.length) {
			throw new IllegalArgumentException("Distance matrix must be quadratic.");
		}
	}

	/**
	 * Tests if the given parameters are valid for landmark MDS and throws an
	 * <code>IllegalArgumentException</code> otherwise.
	 *
	 * @param distances  the distance matrix
	 * @param dimensions the number of dimensions of the embedding
	 * @throws IllegalArgumentException if the given matrix or any of its rows is
	 *                                  <code>null</code>
	 * @throws IllegalArgumentException if the matrix is not rectangular
	 * @throws IllegalArgumentException if the matrix is not positive definite
	 * @throws IllegalArgumentException if the number of embedding dimensions is
	 *                                  greater than the size of the distance matrix
	 */
	private static void checkInputLandmark(double[][] distances, int dimensions) {
		if (distances == null) {
			throw new IllegalArgumentException("Distance matrix cannot be null.");
		}
		if (dimensions > distances.length) {
			throw new IllegalArgumentException(
					"Embedding dimension cannot be greater than the size of the distance matrix.");
		}
		for (final double[] element : distances) {
			if (element == null) {
				throw new IllegalArgumentException("Distance matrix cannot be null.");
			} else if (element.length != distances[0].length) {
				throw new IllegalArgumentException("Distance matrix must be rectangular.");
			}
		}
		if (distances.length < distances[0].length) {
			throw new IllegalArgumentException("Cannot have a matrix that is wider than high.");
		}
		for (int i = 0; i < distances[0].length; i++) {
			if (distances[i][i] != 0) {
				throw new IllegalArgumentException("Distance from an element to it self must be 0.");
			}
			for (final double[] element : distances) {
				if (element[i] < 0) {
					throw new IllegalArgumentException("Distance matrix entries must be positive.");
				}
			}
		}
	}

	// Prevent instantiation
	private MultidimensionalScaling() {
	}
}
