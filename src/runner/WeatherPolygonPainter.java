package runner;

import data.GeographicPolygon;

/**
 * A {@link WeatherPolygonPainter} is used to color {@link GeographicPolygon}s
 * using a certain coloring scheme.
 *
 * @author Anton Ballmaier
 *
 */
public abstract class WeatherPolygonPainter {
	/**
	 * Color conversion from HSL to RGB colors.
	 *
	 * <p>
	 * Hue, saturation and lighness are expected to be from the interval [0, 1].
	 *
	 * @param h the hue from the interval [0,1]
	 * @param s the saturation from the interval [0,1]
	 * @param l the lightness from the interval [0,1]
	 * @return the RGB representation of the given hsl color, where r, g, b are from
	 *         the interval [0,255]
	 */
	protected static int[] hslToRgb(float h, float s, float l) {
		float r, g, b;

		if (s == 0f) {
			r = g = b = l; // achromatic
		} else {
			final float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
			final float p = 2 * l - q;
			r = hueToRgb(p, q, h + 1f / 3f);
			g = hueToRgb(p, q, h);
			b = hueToRgb(p, q, h - 1f / 3f);
		}
		return new int[] { to255(r), to255(g), to255(b) };
	}

	/**
	 * Helper method used to convert hue values to RGB color space
	 *
	 * @param p the intermediate result p used to compute the rgb value
	 * @param q the intermediate result q used to compute the rgb value
	 * @param t the required hue shift dependent on the color to calculate
	 * @return the (floating point) r, g or b value resulting from the given
	 *         parameters
	 */
	private static float hueToRgb(float p, float q, float t) {
		if (t < 0f) {
			t += 1f;
		}
		if (t > 1f) {
			t -= 1f;
		}
		if (t < 1f / 6f) {
			return p + (q - p) * 6f * t;
		}
		if (t < 0.5f) {
			return q;
		}
		if (t < 2f / 3f) {
			return p + (q - p) * (2f / 3f - t) * 6f;
		}
		return p;
	}

	/**
	 * Helper method used to map the interval [0,1] to a [0,255] <code>int</code>
	 * range. This is used to quantize colors.
	 *
	 * @param v the value
	 * @return the mapped value
	 */
	private static int to255(float v) {
		return (int) Math.min(255, 256 * v);
	}

	/**
	 * Assigns a color to a given {@link GeographicPolygon}.
	 *
	 * @param poly the {@link GeographicPolygon} to color
	 * @return the color of the given {@link GeographicPolygon}
	 */
	public abstract int[] colorOf(GeographicPolygon poly);
}
