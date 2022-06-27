package frechetDistance;

/**
 * Multidimensional polygon implementation.
 *
 * <p>
 * This implementation is based on a <code>double[][]</code> containing the
 * coordinates of the polygons vertices.
 *
 * @author Anton Ballmaier
 *
 */
public class Polygon {
	/**
	 * The dimensionality of each vertex of the polygon.
	 */
	public final int dimension;

	/**
	 * The number of vertices of the polygon.
	 */
	public final int length;

	private final double[][] data;

	/**
	 * Constructs a new {@link Polygon} object using the given data. The given
	 * <code>double[][]</code> is not copied but simply referenced in the new
	 * {@link Polygon}. Therefore the user must make sure the given array isn't
	 * altered.
	 *
	 * @param data the vertices of the polygon. <code>data[i][j]</code> is
	 *             interpreted as the j<sup>th</sup> coordinate of the
	 *             i<sup>th</sup> vertex.
	 * @throws IllegalArgumentException if the given data is <code>null</code>
	 * @throws IllegalArgumentException if the given data contains less than two
	 *                                  vertices
	 * @throws IllegalArgumentException if the given data contains <code>null</code>
	 *                                  as one of it's vertices
	 * @throws IllegalArgumentException if the given data contains vertices of
	 *                                  different dimensionality
	 */
	public Polygon(double[][] data) {
		if (data == null) {
			throw new IllegalArgumentException("Polygon data cannot be null.");
		}
		if (data.length < 2) {
			throw new IllegalArgumentException("Polygon must have at least two points.");
		}
		for (final double[] point : data) {
			if (point == null) {
				throw new IllegalArgumentException("The Polygon cannot contain null as one of its points.");
			}
		}
		dimension = data[0].length;
		for (final double[] point : data) {
			if (point.length != dimension) {
				throw new IllegalArgumentException("The Polygon cannot contain points of different dimension.");
			}
		}
		this.length = data.length;
		this.data = data;
	}

	/**
	 * Calculates the distance between specified vertices of <code>this</code> and
	 * another {@link Polygon}. It must be ensured that the given indices are valid
	 * for the respective {@link Polygon}.
	 *
	 * @param p        the other polygon
	 * @param ownIndex the index of the vertex of <code>this</code>
	 * @param pIndex   the index of the vertex of p
	 * @return the euclidean distance between the specified vertices
	 * @throws IllegalArgumentException if the given polygon has a different
	 *                                  dimensionality than <code>this</code>
	 */
	public double cornerDistance(Polygon p, int ownIndex, int pIndex) {
		if (p.dimension != dimension) {
			throw new IllegalArgumentException("Cannot compare Polygons of different dimension.");
		}
		return euclideanDistance(data[ownIndex], p.data[pIndex]);
	}

	/**
	 * Returns the selected vertex. Note that changing the returned array will
	 * result in changing the {@link Polygon}s data!
	 *
	 * @param i the index of the selected vertex.
	 * @return the copy of the selected vertex.
	 * @throws IndexOutOfBoundsException if the given index isn't viable
	 */
	public double[] getVertex(int i) {
		return data[i].clone();
	}

	/**
	 * Calculates the length of the longest segment (a segment being the line
	 * between two consecutive vertices)
	 *
	 * @return the length of the longest segment
	 */
	public double longestSegment() {
		double longestSegment = 0;
		for (int i = 1; i < length; i++) {
			longestSegment = Math.max(longestSegment, euclideanDistance(data[i - 1], data[i]));
		}
		return longestSegment;
	}

	/**
	 * Calculates the euclidean distance between to multidimensional points a and b.
	 *
	 * @param a the first point
	 * @param b the second point
	 * @return the euclidean distance of a and b
	 */
	private double euclideanDistance(double[] a, double[] b) {
		double d = 0;
		double diff;
		for (int i = 0; i < a.length; i++) {
			diff = a[i] - b[i];
			d += diff * diff;
		}
		return Math.sqrt(d);
	}
}
