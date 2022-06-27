package runner;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import data.GeographicGridPolygon;
import data.WeatherDataPolygonsFactory;
import data.WeatherFeature;
import data.WeatherGridDataPolygonsFactory;

/**
 * This class contains a runner used to generate complete coverage maps using
 * three different coloring schemes.
 *
 * <p>
 * The {@link WeatherPolygonPainter}s used are {@link ColorByLatitude},
 * {@link ColorByLongitude} and {@link ColorByElevation}.
 *
 * @author Anton Ballmaier
 *
 */
public class GenerateCoverageMaps {
	/**
	 * The file path that generated images are saved to
	 */
	private static final String FILE_PATH_FORMAT = "images/coverageMaps/%s";

	/**
	 * Main method making this class a runner for generating coverage map images.
	 *
	 * @param args unused
	 * @throws IOException if saving the images results in an error
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.WIND_SPEED, 1d);
		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2012, 7, 30, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);
		final List<GeographicGridPolygon> polygons = (List<GeographicGridPolygon>) (List<?>) factory.create(startTime,
				2, 1);

		final WeatherPolygonPainter[] painters = new WeatherPolygonPainter[] { new ColorByLatitude(),
				new ColorByLongitude(), new ColorByElevation() };

		for (final WeatherPolygonPainter painter : painters) {
			new CoverageMap(polygons, painter)
					.draw(String.format(FILE_PATH_FORMAT, painter.getClass().getSimpleName()));
		}
		System.out.println(String.format("%d coverage maps have been saved to %s", painters.length,
				String.format(FILE_PATH_FORMAT, "")));
	}

	// Prevent instantiation
	private GenerateCoverageMaps() {
	}
}
