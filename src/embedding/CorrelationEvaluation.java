package embedding;

/**
 * This class contains methods for evaluating, how good well two matrices or an
 * embedding and a distance matrix correlate. The rating is done by calculating
 * the residual variance.
 *
 * <p>
 * When calculating the correlation between an embedding and a distance matrix,
 * the distance matrix does not have to be quadratic. In that case only the
 * correlation between the given distances and the corresponding distances in
 * the embedding are taken into account.
 *
 * @author Anton Ballmaier
 *
 */
public class CorrelationEvaluation {
	/**
	 * Calculates the euclidean distances between the vectors of an embedding.
	 *
	 * The number of distance calculations can be adjusted using the
	 * <code>requiredWidth</code> Parameter. Only distances from the first
	 * <code>requiredWidth</code> embedding vectors to all the others are
	 * calculated.
	 *
	 * @param embedding     the coordinates of the embedding vectors
	 * @param requiredWidth the number of points for which the distances must be
	 *                      calculated.
	 * @return the distance matrix of the embedding vectors
	 */
	public static double[][] embeddingDistances(double[][] embedding, int requiredWidth) {
		final int total = embedding[0].length;
		final int dimensions = embedding.length;
		final double[][] distances = new double[total][requiredWidth];
		double diff;
		for (int i = 0; i < requiredWidth; i++) {
			for (int j = 0; j < i; j++) {
				double distance = 0;
				for (int d = 0; d < dimensions; d++) {
					diff = embedding[d][i] - embedding[d][j];
					distance += diff * diff;
				}
				distances[i][j] = Math.sqrt(distance);
				distances[j][i] = distances[i][j];
			}
			for (int j = requiredWidth; j < total; j++) {
				double distance = 0;
				for (int d = 0; d < dimensions; d++) {
					diff = embedding[d][i] - embedding[d][j];
					distance += diff * diff;
				}
				distances[j][i] = Math.sqrt(distance);
			}
		}

		return distances;
	}

	/**
	 * Inspects the quality of an embedding from a distance matrix to the euclidean
	 * space.
	 *
	 * <p>
	 * The quality is rating is the residual variance of the embedding distances and
	 * the original distance matrix. 0 indicates a perfect embedding, 1 indicates
	 * that there is no correlation at all. In general lower values stand for better
	 * embeddings.
	 *
	 * <p>
	 * When calculating the embedding quality, the given the distance matrix does
	 * not have to be quadratic. In that case only the correlation between the given
	 * distances and the corresponding distances in the embedding are taken into
	 * account.
	 *
	 * @param distances the original distance matrix
	 * @param embedding the embedding
	 * @return the quality of the embedding
	 */
	public static double embeddingQuality(double[][] distances, double[][] embedding) {
		return residualVariance(distances, embeddingDistances(embedding, distances[0].length));

	}

	/**
	 * Calculates the residual variance between to matrices of equal dimensions.
	 * This can be used as a measure of correlation.
	 *
	 * @param a the first matrix
	 * @param b the second matrix
	 * @return their residual variance
	 */
	public static double residualVariance(double[][] a, double[][] b) {
		final int n = a.length;
		final int m = a[0].length;
		double avgA = 0, avgB = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				avgA += a[i][j];
				avgB += b[i][j];
			}
		}
		avgA /= n * m;
		avgB /= n * m;

		double tmp0 = 0, tmp1 = 0, tmp2 = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				tmp0 += (a[i][j] - avgA) * (b[i][j] - avgB);
				tmp1 += (a[i][j] - avgA) * (a[i][j] - avgA);
				tmp2 += (b[i][j] - avgB) * (b[i][j] - avgB);
			}
		}
		final double r = tmp0 / Math.sqrt(tmp1 * tmp2);
		return 1 - r * r;
	}

	// Prevent instantiation
	private CorrelationEvaluation() {
	}
}