package data;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import frechetDistance.Polygon;

class WeatherGridDataPolygonsFactoryTest {

	@Test
	void downloadedDataTest() throws IOException {
		deleteDataFiles();

		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.CLOUDS, 1d);
		features.put(WeatherFeature.DEW_POINT, 1d);
		features.put(WeatherFeature.HUMIDITY, 1d);
		features.put(WeatherFeature.PRESSURE, 1d);
		features.put(WeatherFeature.TEMPERATURE, 1d);
		features.put(WeatherFeature.WIND_SPEED, 1d);

		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2000, 0, 3, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		final List<GeographicPolygon> polygons = factory.create(startTime);

		deleteDataFiles();

		for (final double value : polygons.get(0).getVertex(0)) {
			assertTrue(!Double.isNaN(value) && Double.isFinite(value) && value > -50 && value < 2000);
		}
	}

	@Test
	void monthBorderPolygonsTest() throws IOException {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.WIND_SPEED, 1d);

		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2000, 0, 30, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		final Polygon polygon = factory.create(startTime, 100).get(0);
		for (int i = 0; i < polygon.length; i++) {
			assertFalse(Double.isNaN(polygon.getVertex(i)[1]));
		}
	}

	@Test
	void polygonDataTest() throws IOException {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.WIND_SPEED, 1d);

		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2000, 0, 3, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		List<GeographicPolygon> polygons = factory.create(startTime);

		assertEquals(2, polygons.get(0).dimension);
		assertEquals(24, polygons.get(0).length);

		// Test create using polygon length
		final Polygon polygon = factory.create(startTime, 100).get(0);
		assertEquals(100, polygon.length);

		// Test create using polygon length and step size
		final Polygon polygonSteps = factory.create(startTime, 90, 3).get(0);
		assertEquals(30, polygonSteps.length);

		for (int i = 0; i < 30; i++) {
			assertArrayEquals(polygon.getVertex(i * 3), polygonSteps.getVertex(i));
		}

		// Test create using filter
		polygons = factory.create(startTime, 24, 3, (metadata) -> metadata[2] > 1000);
		for (final GeographicPolygon p : polygons) {
			assertTrue(p.getElevation() > 1000);
		}
	}

	// Removes grid data files required for January 2000
	private void deleteDataFiles() {
		final String[] neededFilePaths = new String[] { "air_temperature_mean/TT_200001.nc",
				"air_temperature_mean/TT_200001.nc.bz2", "cloud_cover/N_200001.nc", "cloud_cover/N_200001.nc.gz",
				"dew_point/TD_200001.nc", "dew_point/TD_200001.nc.gz", "humidity/RH_200001.nc",
				"humidity/RH_200001.nc.gz", "pressure/PRED_200001.nc", "pressure/PRED_200001.nc.gz",
				"wind_speed/FF_200001.nc", "wind_speed/FF_200001.nc.gz" };
		for (final String path : neededFilePaths) {
			final File f = new File("data/weather/project-try/" + path);
			f.delete();
		}
	}
}
