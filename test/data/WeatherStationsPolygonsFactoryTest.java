package data;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import frechetDistance.Polygon;

class WeatherStationsPolygonsFactoryTest {

	@Test
	void polygonDataTest() throws IOException {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.WIND_SPEED, 1d);
		features.put(WeatherFeature.PRESSURE, 1d);

		final WeatherDataPolygonsFactory factory = new WeatherStationsPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2000, 0, 3, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		List<GeographicPolygon> polygons = factory.create(startTime);

		assertEquals(3, polygons.get(0).dimension);
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

}
