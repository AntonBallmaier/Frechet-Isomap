package runner;

import data.GeographicPolygon;

/**
 * {@link ColorByLatitude} is used to color {@link GeographicPolygon}s by their
 * latitude.
 *
 * <p>
 * The latitude is linearly mapped to a hue value, the resulting color being
 * fully saturated. The linear mapping is chosen in a way so northern Germany is
 * red and southern Germany purple.
 *
 * @author Anton Ballmaier
 *
 */
public class ColorByLatitude extends WeatherPolygonPainter {
	@Override
	public int[] colorOf(GeographicPolygon poly) {
		float hue = (float) (-45 * poly.getLatitude() + 2445);
		hue /= 360f;
		return hslToRgb(hue, 1f, 0.5f);
	}
}