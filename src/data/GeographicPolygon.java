package data;

import frechetDistance.Polygon;

/**
 * A {@link GeographicPolygon} represents a {@link Polygon} at a specific
 * geographic location.
 *
 * @author Anton Ballmaier
 *
 */
public class GeographicPolygon extends Polygon {
	/**
	 * The elevation of the geographic position in MSL (height above mean sea level)
	 */
	private final double elevation;

	/**
	 * The latitude of the geographic position
	 */
	private final double latitude;

	/**
	 * The longitude of the geographic position
	 */
	private final double longitude;

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
	 * @throws IllegalArgumentException if the given data is <code>null</code>
	 * @throws IllegalArgumentException if the given data contains less than two
	 *                                  vertices
	 * @throws IllegalArgumentException if the given data contains <code>null</code>
	 *                                  as one of it's vertices
	 * @throws IllegalArgumentException if the given data contains vertices of
	 *                                  different dimensionality
	 */
	public GeographicPolygon(double[][] data, double longitude, double latitude, double elevation) {
		super(data);
		this.longitude = longitude;
		this.latitude = latitude;
		this.elevation = elevation;
	}

	/**
	 * Returns the elevation of the geographic position
	 *
	 * @return the elevation of the geographic position
	 */
	public double getElevation() {
		return elevation;
	}

	/**
	 * Returns the latitude of the geographic position
	 *
	 * @return the latitude of the geographic position
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Returns the longitude of the geographic position
	 *
	 * @return the longitude of the geographic position
	 */
	public double getLongitude() {
		return longitude;
	}
}
