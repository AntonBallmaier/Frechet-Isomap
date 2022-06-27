package runner;

import data.GeographicPolygon;

/**
 * {@link ColorByElevation} is used to color {@link GeographicPolygon}s by their
 * elevation.
 *
 * <p>
 * The color is determined using a list of specified pairs of color and
 * elevation. The exact color is obtained by interpolating between the
 * colorStops of the two closest specified colorStops.
 *
 * @author Anton Ballmaier
 *
 */
public class ColorByElevation extends WeatherPolygonPainter {
	/**
	 * The defined color stops. These are used at the corresponding
	 * {@link #ELEVATION_STOPS}
	 */
	private static final double[][] COLOR_STOPS = new double[][] { { .333, 1, 0.2 }, { .333, 1, 0.25 }, { .25, 1, 0.4 },
			{ .167, 1, 0.5 }, { .083, 1, .45 }, { 0, 1, 0.35 }, { 0, 1, 0 } };

	/**
	 * The defined elevation stops. These are used at the corresponding
	 * {@link #COLOR_STOPS}
	 */
	private static final int[] ELEVATION_STOPS = new int[] { 0, 50, 200, 400, 700, 1200, 3000 };

	/**
	 * Assigns a color to a given {@link GeographicPolygon} based on the elevation
	 * of its geographic position.
	 *
	 * <p>
	 * The mapping is chosen in a way so low regions are green higher ones range
	 * from yellow over red to black.
	 *
	 * @param poly the {@link GeographicPolygon} to color
	 * @return the color of the given {@link GeographicPolygon}
	 */
	@Override
	public int[] colorOf(GeographicPolygon poly) {
		final double elevation = Math.max(0, poly.getElevation());
		final double[] hsl = new double[3];

		for (int i = 0; i < ELEVATION_STOPS.length; i++) {
			if (elevation < ELEVATION_STOPS[i]) {
				final int upperBound = ELEVATION_STOPS[i];
				final int lowerBound = ELEVATION_STOPS[i - 1];
				final double t = (elevation - lowerBound) / (upperBound - lowerBound);
				for (int j = 0; j < 3; j++) {
					hsl[j] = t * COLOR_STOPS[i][j] + (1f - t) * COLOR_STOPS[i - 1][j];
				}
				break;
			}
		}
		return hslToRgb((float) hsl[0], (float) hsl[1], (float) hsl[2]);
	}
}
