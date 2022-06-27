package data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link WeatherStationsPolygonsFactory} is used to construct
 * {@link GeographicPolygon}s from the data of weather stations.
 *
 * <p>
 * The internally used <code>Integer</code> key of every position is based on
 * the official weather station id.
 *
 * @author Anton Ballmaier
 *
 */
public class WeatherStationsPolygonsFactory extends WeatherDataPolygonsFactory {

	/**
	 * The directory where the required data is saved
	 */
	private static String LOCAL_DIRECTORY = "data/weather/stations/";

	/**
	 * Constructs a new {@link WeatherStationsPolygonsFactory} object using the
	 * given weighted features.
	 *
	 * <p>
	 * These define the feature space and it's scaling. A mapping of a feature f to
	 * the value x means: Include data about feature f in the Polygons and scale
	 * these data values by a factor of x.
	 *
	 * <p>
	 * A generated {@link GeographicPolygon} then represents the (quantized) weather
	 * course at one position over a later specified time window.
	 *
	 * @param features the {@link WeatherFeature}s to use and how to scale them
	 */
	public WeatherStationsPolygonsFactory(Map<WeatherFeature, Double> features) {
		super(features);
	}

	/**
	 * Constructs a {@link GeographicPolygon} from the given polygon data and
	 * additional metadata containing the latitude, longitude and elevation.
	 *
	 * @param data the polygon data
	 * @param meta the metadata, where
	 *             <ul>
	 *             <li><code>meta[0]</code> is the latitude
	 *             <li><code>meta[1]</code> is the longitude
	 *             <li><code>meta[2]</code> is the elevation
	 *             </ul>
	 * @return the constructed {@link GeographicPolygon}
	 */
	@Override
	protected GeographicPolygon makeWeatherPolygon(double[][] data, double[] meta) {
		final double lat = meta[0];
		final double lon = meta[1];
		final double elevation = meta[2];
		return new GeographicPolygon(data, lat, lon, elevation);
	}

	/**
	 * Constructs the course of all {@link GeographicPolygon}s with regard to a
	 * single feature.
	 *
	 * @param feature   the feature for which the data should be constructed
	 * @param startTime the start time of the time window
	 * @param timeSteps the number of time steps
	 * @param stepSize  the time step size in hours
	 * @return the mapping from every available position key to the a single
	 *         dimension of the corresponding polygon data
	 * @throws IOException if the data could not be loaded
	 */
	@Override
	protected LinkedHashMap<Integer, double[]> readFeatureData(WeatherFeature feature, Calendar startTime,
			int timeSteps, int stepSize) throws IOException {
		final LinkedHashMap<Integer, double[]> featureData = new LinkedHashMap<>();
		final BufferedReader csvReader = new BufferedReader(
				new FileReader(LOCAL_DIRECTORY + fileName(feature) + ".csv"));

		final Calendar endTime = (Calendar) startTime.clone();
		endTime.add(Calendar.HOUR, timeSteps * stepSize);

		String row;
		int currentId = -1;
		boolean currentStationDone = false;
		double[] data = null;
		int currentIndex = -1;
		Calendar nextTime = null;
		while ((row = csvReader.readLine()) != null) {
			final String[] fields = row.split(",");
			final int id = Integer.parseInt(fields[0]);
			if (currentId != id) {
				currentId = id;
				currentStationDone = false;
				currentIndex = -1;
				data = new double[timeSteps];
				nextTime = (Calendar) startTime.clone();
			}
			if (!currentStationDone) {
				final Calendar currentTime = timeFromString(fields[1]);
				if (currentTime.compareTo(endTime) >= 0) {
					currentStationDone = true;
				}
				if (currentTime.compareTo(nextTime) == 0) {
					data[++currentIndex] = Double.parseDouble(fields[2]);
					nextTime.add(Calendar.HOUR, stepSize);
					if (!nextTime.before(endTime)) {
						currentStationDone = true;
						featureData.put(id, data);
					}
				}
			}
		}
		csvReader.close();

		return featureData;
	}

	/**
	 * Constructs the metadata of all available positions and maps them to the
	 * positions <code>Integer</code> key.
	 *
	 * <p>
	 * The metadata contains is structured as follows:
	 * <ul>
	 * <li><code>meta[0]</code> is the latitude
	 * <li><code>meta[1]</code> is the longitude
	 * <li><code>meta[2]</code> is the elevation
	 * </ul>
	 *
	 * @return the mapping from every available position key to the corresponding
	 *         metadata, where
	 *         <ul>
	 *         <li><code>meta[0]</code> is the latitude
	 *         <li><code>meta[1]</code> is the longitude
	 *         <li><code>meta[2]</code> is the elevation
	 *         </ul>
	 * @throws IOException if the data could not be loaded
	 */
	@Override
	protected Map<Integer, double[]> readMetaData() throws IOException {
		final Map<Integer, double[]> metaData = new HashMap<>();
		final BufferedReader csvReader = new BufferedReader(new FileReader(LOCAL_DIRECTORY + "stations.csv"));
		String row;
		while ((row = csvReader.readLine()) != null) {
			final String[] data = row.split(",");
			metaData.put(Integer.parseInt(data[0]), new double[] { Double.parseDouble(data[1]),
					Double.parseDouble(data[2]), Double.parseDouble(data[3]) });
		}
		csvReader.close();
		return metaData;
	}

	/**
	 * Maps a {@link WeatherFeature} to the corresponding file name in the data
	 * directory.
	 *
	 * @param feature the feature
	 * @return the file name (without file ending)
	 */
	private String fileName(WeatherFeature feature) {
		switch (feature) {
		case HUMIDITY:
			return "humidity";
		case PRESSURE:
			return "pressure";
		case TEMPERATURE:
			return "temperature";
		case WIND_SPEED:
			return "wind_speed";
		default:
			throw new IllegalArgumentException("Unexpected value: " + feature);
		}
	}

	/**
	 * Constructs a point in time from a specific textual time format.
	 *
	 * @param string representation of a time using the format "yyyymmddhh"
	 * @return the point in time
	 */
	private Calendar timeFromString(String string) {
		final int year = Integer.parseInt(string.substring(0, 4)), month = Integer.parseInt(string.substring(4, 6)) - 1,
				day = Integer.parseInt(string.substring(6, 8)), hour = Integer.parseInt(string.substring(8, 10));

		final Calendar calendar = new GregorianCalendar(year, month, day, hour, 0, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar;
	}
}
