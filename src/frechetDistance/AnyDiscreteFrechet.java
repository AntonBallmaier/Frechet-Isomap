package frechetDistance;

import embedding.measures.InterruptionMeasure;

/**
 * A discrete measurement of the distance (dissimilarity) between two
 * {@link Polygon}s.
 *
 * @author Anton Ballmaier
 * @see Polygon
 */
public abstract class AnyDiscreteFrechet implements InterruptionMeasure<Polygon> {
	/**
	 * Approximates the discrete fr&#xE9;chet distance variant. If the distance is
	 * greater than the given maximum, the approximation is interrupted and
	 * <code>Double.POSITIVE_INFINITY</code> is returned instead.
	 *
	 *
	 * @param p   the first polygon to be compared
	 * @param q   the second polygon to be compared
	 * @param max the maximum distance of the given polygons for which the distance
	 *            calculation should be performed
	 * @return the approximated fr&#xE9;chet distance of the given polygons
	 * @throws IllegalArgumentException if the given polygons have different
	 *                                  dimensionality
	 */
	@Override
	public double distanceCapped(Polygon p, Polygon q, double max) {
		if (p.dimension != q.dimension) {
			throw new IllegalArgumentException("Cannot compare Polygons with different dimensionality.");
		}

		if (q.length > p.length) {
			final Polygon tmp = p;
			p = q;
			q = tmp;
		}

		// Use only two alternatingly used rows to decrease memory usage
		double[] currentRow = new double[q.length];
		double[] lastRow = new double[q.length];

		// keep track of row minimum to quit one it surpasses max at which point the
		// calculation should be interrupted.
		double rowMin = Double.POSITIVE_INFINITY;

		currentRow[0] = p.cornerDistance(q, 0, 0);

		// Calculate discrete frechet distance dynamically
		for (int j = 1; j < q.length; j++) {
			currentRow[j] = accumulate(currentRow[j - 1], p.cornerDistance(q, 0, j));
			rowMin = Math.min(rowMin, currentRow[j]);
		}
		if (rowMin > max) {
			return Double.POSITIVE_INFINITY;
		}

		for (int i = 1; i < p.length; i++) {
			final double[] tmp = currentRow;
			currentRow = lastRow;
			lastRow = tmp;

			currentRow[0] = accumulate(lastRow[0], p.cornerDistance(q, i, 0));
			rowMin = currentRow[0];
			for (int j = 1; j < q.length; j++) {
				currentRow[j] = accumulate(Math.min(Math.min(lastRow[j], lastRow[j - 1]), currentRow[j - 1]),
						p.cornerDistance(q, i, j));
				rowMin = Math.min(rowMin, currentRow[j]);
			}
			if (rowMin > max) {
				return Double.POSITIVE_INFINITY;
			}
		}
		return currentRow[q.length - 1];
	}

	/**
	 * Used to accumulate the cost of reaching the pair <i>(i,j)</i> where <i>i</i>
	 * is a vertex of the polygon <i>p</i> and <i>j</i> is a vertex of the polygon
	 * <i>q</i>. The cost of this pair is <center><code>accumulate</code><i>(c(k,l),
	 * &delta;(i,j))</i>,</center> where
	 * <ul>
	 * <li><i>c(k,l)</i> is the minimal cost of a preceding pair of vertices (i.e.
	 * one of <i>(i-1,j), (i,j-1), (i-1, j-1)</i>) and
	 * <li><i>&delta;(i,j)</i> is the distance between the vertices <i>i</i> and
	 * <i>j</i>.
	 * </ul>
	 *
	 * <p>
	 * The implementation of this function must be monotone in both arguments,
	 * meaning the following must always be <code>true</code>: <center>
	 *
	 * <pre>
	 * accumulate(a,b) &geq; a
	 * accumulate(a,b) &geq; b
	 * </pre>
	 *
	 * </center>
	 *
	 **
	 * Using <code>Math.max</code> as the implementation for <code>accumulate</code>
	 * will therefore yield the typical discrete fr&#xE9;chet distance.
	 *
	 * @param a the minimal cost of any preceding pair of vertices
	 * @param b the distance of two vertices
	 * @return the accumulated cost of a pair of vertices
	 */
	abstract protected double accumulate(double a, double b);
}
