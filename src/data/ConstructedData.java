package data;

import frechetDistance.Polygon;

/**
 * This class provides a selection methods to generate datasets that can be used
 * to test embedding methods.
 *
 * @author Anton Ballmaier
 *
 */
public class ConstructedData {
	/**
	 * Generates a so called swiss roll dataset. This dataset consists of points
	 * lying on a spiraling 2d surface in 3d space. The points within this dataset
	 * are generated randomly with a uniform distribution.
	 *
	 * @param n the number of points to generate
	 * @return the swiss roll dataset
	 */
	public static double[][] randomSwissRoll(int n) {
		final double[][] points = new double[n][3];
		for (int i = 0; i < n; i++) {
			final double phi = 1f + Math.random() * 4f;
			points[i][0] = phi * Math.cos(phi);
			points[i][1] = phi * Math.sin(phi);
			points[i][2] = Math.random() * 20f;
		}
		return points;
	}

	/**
	 * Generates a {@link Polygon} dataset where each polygon is a random walk
	 * starting at the origin. Their length ranges from 3 to 15, the dimension of
	 * the polygon space is 4. The amount of {@link Polygon}s can be specified.
	 *
	 * @param amount the number of {@link Polygon}s to generate
	 * @return the generated {@link Polygon}s
	 */
	public static Polygon[] randomWalks(int amount) {
		return randomWalks(amount, 3, 15, 4);
	}

	/**
	 * Generates a {@link Polygon} dataset where each polygon is a random walk
	 * starting at the origin. The length, dimension of the polygon space and amount
	 * of {@link Polygon}s can be specified.
	 *
	 * @param amount     the number of {@link Polygon}s to generate
	 * @param minLength  the minimum length
	 * @param maxLength  the maximum length
	 * @param dimensions the dimension of the polygon space
	 * @return the generated {@link Polygon}s
	 */
	public static Polygon[] randomWalks(int amount, int minLength, int maxLength, int dimensions) {
		final Polygon[] polygons = new Polygon[amount];
		for (int i = 0; i < amount; i++) {
			final int length = minLength + (int) Math.floor(Math.random() * (maxLength - minLength));
			final double[][] data = new double[length][];
			final double[] point = new double[dimensions];
			for (int l = 0; l < length; l++) {
				for (int d = 0; d < dimensions; d++) {
					point[d] += Math.random();
				}
				data[l] = point.clone();
			}
			polygons[i] = new Polygon(data);
		}
		return polygons;
	}

	/**
	 * Generates a dataset of {@link Polygon}s. They lie on a 2d manifold in the 2d
	 * polygon space and have length 5. The {@link Polygon}s within this dataset are
	 * distributed evenly.
	 *
	 * <p>
	 * Every {@link Polygon} is generated from two parameters <code>a</code> and
	 * <code>b</code>. All parameter values from a certain interval, separated by a
	 * certain step size are chosen.
	 *
	 * @param range defines the interval from which the parameter values are
	 *              selected. The interval used is [<code>-range</code>,
	 *              <code>range</code>].
	 * @param step  the difference between two used parameter values
	 * @return the generated dataset
	 */
	public static Polygon[] shiftedSpikes(double range, double step) {

		// Count number steps:
		int amount = 0;
		for (double x = -range; x <= range; x += step) {
			amount++;
		}

		// Polygons for every 2d configuration:
		amount *= amount;
		final Polygon[] polys = new Polygon[amount];

		int i = 0;
		for (double a = -range; a <= range; a += step) {
			for (double b = -range; b <= range; b += step) {
				final double[][] data = new double[][] { { 0.0, 0.0 }, { 0.4, a / 2 }, { b + 0.5, a }, { 0.6, a / 2 },
						{ 1.0, 0.0 } };
				polys[i++] = new Polygon(data);
			}
		}
		return polys;
	}

	// Prevent instantiation
	private ConstructedData() {
	}
}
