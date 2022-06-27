package data;

/**
 * A {@link GeographicGridPolygon} represents a {@link GeographicPolygon}
 * embedded in a specific grid specific coordinates.
 *
 * @author Anton Ballmaier
 *
 */
public class GeographicGridPolygon extends GeographicPolygon {
	/**
	 * coordinate of the grid position
	 */
	private final int x, y;

	/**
	 * Constructs a new {@link GeographicPolygon} object using the given data. The
	 * given <code>double[][]</code> is not copied but simply referenced in the new
	 * {@link GeographicPolygon}. Therefore the user must make sure the given array
	 * isn't altered.
	 *
	 * @param data      the vertices of the polygon. <code>data[i][j]</code> is
	 *                  interpreted as the j<sup>th</sup> coordinate of the
	 *                  i<sup>th</sup> vertex.
	 *
	 * @param longitude the longitude of the geographic position
	 * @param latitude  the latitude of the geographic position
	 * @param elevation the elevation of the geographic position
	 * @param x         the first coordinate of the grid position
	 * @param y         the second coordinate of the grid position
	 * @throws IllegalArgumentException if the given data is <code>null</code>
	 * @throws IllegalArgumentException if the given data contains less than two
	 *                                  vertices
	 * @throws IllegalArgumentException if the given data contains <code>null</code>
	 *                                  as one of it's vertices
	 * @throws IllegalArgumentException if the given data contains vertices of
	 *                                  different dimensionality
	 */
	public GeographicGridPolygon(double[][] data, double longitude, double latitude, double elevation, int x, int y) {
		super(data, longitude, latitude, elevation);
		this.x = x;
		this.y = y;
	}

	/**
	 * Returns the first coordinate of the grid position
	 *
	 * @return the first coordinate of the grid position
	 */
	public int getX() {
		return x;
	}

	/**
	 * Returns the second coordinate of the grid position
	 *
	 * @return the second coordinate of the grid position
	 */
	public int getY() {
		return y;
	}
}
