package testUtils;

import frechetDistance.Polygon;

public class TestUtils {
	/**
	 * Calculates the euclidean distance between to multidimensional points a and b.
	 *
	 * @param a the first point
	 * @param b the second point
	 * @return the euclidean distance of a and b
	 */
	public static double euclideanDistance(double[] a, double[] b) {
		double d = 0;
		double diff;
		for (int i = 0; i < a.length; i++) {
			diff = a[i] - b[i];
			d += diff * diff;
		}
		return Math.sqrt(d);
	}

	public static double[] randomPoint(int dimension) {
		final double[] point = new double[dimension];
		for (int i = 0; i < dimension; i++) {
			point[i] = (Math.random() - 0.5) * 100;
		}
		return point;
	}

	public static Polygon randomPolygon(int dimension, int length) {
		final double[][] polygonData = new double[length][];
		for (int i = 0; i < length; i++) {
			polygonData[i] = randomPoint(dimension);
		}
		return new Polygon(polygonData);
	}

	public static double[][] randomPolygonData(int dimension, int length) {
		final double[][] polygonData = new double[length][];
		for (int i = 0; i < length; i++) {
			polygonData[i] = randomPoint(dimension);
		}
		return polygonData;
	}
}
