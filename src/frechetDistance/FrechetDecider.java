package frechetDistance;

/**
 * A decider for the fr&#xE9;chet decision problem.
 *
 * <p>
 * The decision problem is the question whether the fr&#xE9;chet distance of two
 * polygons is less or equal a given distance.
 *
 * @author Anton Ballmaier
 *
 */
abstract class FrechetDecider {
	/**
	 * Calculates the section of a line segment which lies within a given distance
	 * around a given point.
	 * <p>
	 * The returned array contains two values, measured in local coordinates of the
	 * line segment (0 corresponding to the start of the line segment, 1 to it's
	 * end). If no point of the line segment lies within the given distance
	 * <code>null</code> is returned.
	 *
	 * @param p            the polygon that has the center point as vertex
	 * @param centerIndex  the index of the center point on p
	 * @param q            the polygon that contains the line segment
	 * @param segmentIndex index of the starting point of the line segment
	 * @param distance     maximal distance of all point in the returned interval
	 *                     around center
	 * @return interval of the section of the line segment which lies within
	 *         distance around center or <code>null</code>
	 * @throws IllegalArgumentException  if the given polygons have different
	 *                                   dimensionality
	 * @throws IndexOutOfBoundsException if either of the indices for the center
	 *                                   point or line segment are invalid.
	 */
	public static double[] freeInterval(Polygon p, int centerIndex, Polygon q, int segmentIndex, double distance) {
		if (p.dimension != q.dimension) {
			throw new IllegalArgumentException("Cannot compare Polygons of different dimension.");
		}

		// Extract points to separate variables to make calculations clearer
		final double[] a = q.getVertex(segmentIndex), b = q.getVertex(segmentIndex + 1), c = p.getVertex(centerIndex);

		// Result of analytically solving
		// distance = ||center - (p[i]+t*(p[i+1] - p[i]))|| for t
		// Since the resulting formula is build up from 3 recurring parts these are
		// calculated first.
		double tmp0 = 0, tmp1 = 0, tmp2 = 0;
		for (int i = 0; i < a.length; i++) {
			tmp0 += b[i] * c[i] + a[i] * a[i] - a[i] * c[i] - a[i] * b[i];
			tmp1 += a[i] * a[i] + b[i] * b[i] - 2 * a[i] * b[i];
			tmp2 += c[i] * c[i] + a[i] * a[i] - 2 * a[i] * c[i];
		}

		tmp0 *= 2;
		tmp1 *= 2;
		tmp2 = tmp0 * tmp0 - 2 * tmp1 * (tmp2 - distance * distance);

		if (tmp2 < 0) {
			// No intersection at all
			return null;
		}
		tmp2 = Math.sqrt(tmp2);

		double t0 = (tmp0 - tmp2) / tmp1;
		double t1 = (tmp0 + tmp2) / tmp1;

		// t0 and t1 can contain values outside [0,1]

		if (t0 > 1 || t1 < 0) {
			// Only intersections with the line outside the actual line segment
			return null;
		}

		// Adjust if intersection is larger than the line segment
		if (t0 < 0) {
			t0 = 0;
		}
		if (t1 > 1) {
			t1 = 1;
		}
		return new double[] { t0, t1 };
	}

	protected final Polygon p, q;

	/**
	 * Constructs a new {@link FrechetDecider} object for the two given
	 * {@link Polygon}s. This can solve their decision problem for any distance.
	 *
	 * @param p the first polygon
	 * @param q the second polygon
	 * @throws IllegalArgumentException if the given polygons have different
	 *                                  dimensionality
	 */
	public FrechetDecider(Polygon p, Polygon q) {
		if (p.dimension != q.dimension) {
			throw new IllegalArgumentException("Cannot compare Polygons of different dimension.");
		}
		this.p = p;
		this.q = q;
	}

	/**
	 * Decides whether the fr&#xE9;chet distance is less or equal the given
	 * distance.
	 *
	 * @param distance the distance to check the fr&#xE9;chet distance against
	 * @return <code>true</code> if fr&#xE9;chet distance of p and q is less or
	 *         equal than distance, <code>false</code> otherwise.
	 */
	abstract public boolean decideDistance(double distance);

	/**
	 * Check if the corresponding endpoints of the two polygons are within reach
	 * given a distance of each other.
	 *
	 * @param distance the maximal distance of the endpoints
	 * @return <code>true</code> if both starting and endpoints are not further than
	 *         <code>distance</code> apart, <code>false</code> otherwise
	 */
	public boolean endpointsToFar(double distance) {
		return p.cornerDistance(q, 0, 0) > distance || p.cornerDistance(q, p.length - 1, q.length - 1) > distance;
	}
}
