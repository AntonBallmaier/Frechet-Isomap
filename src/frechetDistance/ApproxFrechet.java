package frechetDistance;

import embedding.measures.InterruptionMeasure;
import embedding.measures.Measure;;

/**
 * The fr&#xE9;chet distance between two {@link Polygon}s.
 *
 * <p>
 * This class provides an implementation of the {@link Measure} interface for
 * {@link Polygon}s. It can be used to approximate the fr&#xE9;chet distance up
 * to the desired degree of accuracy.
 *
 * @author Anton Ballmaier
 * @see Polygon
 */
public class ApproxFrechet implements InterruptionMeasure<Polygon> {

	/**
	 * The default precision value
	 */
	public static final double DEFAULT_PRECISION = 1e-4;

	/**
	 * The precision of the approximation
	 */
	private double precision;

	/**
	 * Whether to use the conventional tabular fr&#xE9;chet decider.
	 */
	private final boolean tabularDecider;

	/**
	 * Constructs a new default {@link ApproxFrechet} object.
	 * <p>
	 * As the {@linkplain FrechetDecider} it will use
	 * {@linkplain FrechetDeciderPath}, the default precision is
	 * {@linkplain #DEFAULT_PRECISION}.
	 */
	public ApproxFrechet() {
		this(false);
	}

	/**
	 * Constructs a new {@link ApproxFrechet} object. It can be specified if the
	 * typical tabular decider for the fr&#xE9;chet distance should be used.
	 * <p>
	 * The default precision is {@linkplain #DEFAULT_PRECISION}.
	 *
	 * @param tabularDecider whether to use the typical tabular decider for the
	 *                       fr&#xE9;chet distance
	 */
	public ApproxFrechet(boolean tabularDecider) {
		this(DEFAULT_PRECISION, tabularDecider);
	}

	/**
	 * Constructs a new {@link ApproxFrechet} object approximating the fr&#xE9;chet
	 * distance to the given precision.
	 *
	 * <p>
	 * As the {@linkplain FrechetDecider} it will use
	 * {@linkplain FrechetDeciderPath}.
	 *
	 * @param precision the desired precision of distance calculations
	 * @throws IllegalArgumentException if the precision is not positive
	 */
	public ApproxFrechet(double precision) {
		this(precision, false);
	}

	/**
	 * Constructs a new {@link ApproxFrechet} object approximating the fr&#xE9;chet
	 * distance to the given precision. It can be specified if the typical tabular
	 * decider for the fr&#xE9;chet distance should be used.
	 *
	 * @param precision      the desired precision of distance calculations
	 * @param tabularDecider whether to use the typical tabular decider for the
	 *                       fr&#xE9;chet distance
	 * @throws IllegalArgumentException if the precision is not positive
	 */
	public ApproxFrechet(double precision, boolean tabularDecider) {
		this.setPrecision(precision);
		this.tabularDecider = tabularDecider;
	}

	/**
	 * Approximates the fr&#xE9;chet distance to the precision of this instance. If
	 * the distance is greater than the given maximum, the approximation is
	 * interrupted and <code>Double.POSITIVE_INFINITY</code> is returned instead.
	 *
	 * The returned value will differ from the real fr&#xE9;chet distance at most by
	 * this instances {@link #precision} value.
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
		final double[] bounds = getBounds(p, q);

		if (max < bounds[0]) {
			// Even lower bound is larger than expected maximum
			return Double.POSITIVE_INFINITY;
		}

		final FrechetDecider decider = getDecider(p, q);
		if (max < bounds[1]) {
			if (decider.decideDistance(max)) {
				bounds[1] = max;
			} else {
				// Fr&#xE9;chet distance is larger than expected maximum
				return Double.POSITIVE_INFINITY;
			}
		}

		return approximate(bounds[0], bounds[1], decider);
	}

	/**
	 * Returns the current precision value.
	 *
	 * @return the current precision
	 */
	public double getPrecision() {
		return precision;
	}

	/**
	 * Sets the precision to the given value. The precision must be &gt;0.
	 *
	 * @param precision the desired precision of distance calculations
	 * @throws IllegalArgumentException if the precision is not positive
	 */
	public void setPrecision(double precision) {
		if (precision <= 0) {
			throw new IllegalArgumentException("Pricision must be positive.");
		}
		this.precision = precision;
	}

	/**
	 * Binary approximation of the fr&#xE9;chet distance using a given fr&#xE9;chet
	 * decider.
	 *
	 * @param min     a known lower bound of the fr&#xE9;chet distance
	 * @param max     a known upper bound of the fr&#xE9;chet distance
	 * @param decider the decider used for approximation
	 * @return the approximated fr&#xE9;chet distance
	 */
	private double approximate(double min, double max, FrechetDecider decider) {
		double distance = (min + max) / 2;
		while ((max - min) / 2 > precision) {
			if (decider.decideDistance(distance)) {
				max = distance;
			} else {
				min = distance;
			}
			distance = (min + max) / 2;
		}
		return distance;
	}

	/**
	 * Calculated upper and lower bounds for the fr&#xE9;chet distance.
	 *
	 * <p>
	 * These bounds are constructed using the longest segment in any one of the two
	 * polygons and their discrete fr&#xE9;chet distance.
	 *
	 * @param p the first polygon to be compared
	 * @param q the second polygon to be compared
	 * @return the lower and upper bound of the fr&#xE9;chet distance given as
	 *         length 2 array, where the 0<sup>th</sup> element is the lower and the
	 *         1<sup>st</sup> element is the upper bound
	 */
	private double[] getBounds(Polygon p, Polygon q) {
		final double longestSegment = Math.max(p.longestSegment(), q.longestSegment());
		final double discreteDistance = DiscreteFrechet.getInstance().distance(p, q);
		final double[] bounds = new double[2];
		bounds[0] = Math.max(0, discreteDistance - longestSegment / 2);
		bounds[1] = discreteDistance;
		return bounds;
	}

	/**
	 * Returns an instance of the {@link FrechetDecider} used for this
	 * {@link ApproxFrechet}.
	 *
	 * @param p the first polygon to be compared
	 * @param q the second polygon to be compared
	 * @return the required instance, a {@link FrechetDeciderTabular} if
	 *         {@link #tabularDecider} is set to <code>true</code>, a
	 *         {@link FrechetDeciderPath} otherwise
	 */
	private FrechetDecider getDecider(Polygon p, Polygon q) {
		if (tabularDecider) {
			return new FrechetDeciderTabular(p, q);
		} else {
			return new FrechetDeciderPath(p, q);
		}
	}
}