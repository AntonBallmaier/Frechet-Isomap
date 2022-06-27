package runner;

import data.GeographicPolygon;

/**
 * {@link ColorByLongitude} is used to color {@link GeographicPolygon}s by their
 * longitude.
 *
 * <p>
 * The longitude is linearly mapped to a hue value, the resulting color being
 * fully saturated. The linear mapping is chosen in a way so northern Germany is
 * red and southern Germany purple.
 *
 * @author Anton Ballmaier
 *
 */
public class ColorByLongitude extends WeatherPolygonPainter {

	/**
	 * Assigns a color to a given {@link GeographicPolygon} based on the longitude
	 * of its geographic position.
	 *
	 * <p>
	 * The mapping is chosen in a way so western Germany is red and eastern Germany
	 * purple.
	 *
	 * @param poly the {@link GeographicPolygon} to color
	 * @return the color of the given {@link GeographicPolygon}
	 */
	@Override
	public int[] colorOf(GeographicPolygon poly) {
		float hue = (float) (32.69161 * poly.getLongitude() - 191.79983);
		hue /= 360f;
		return hslToRgb(hue, 1f, 0.5f);
	}

}
