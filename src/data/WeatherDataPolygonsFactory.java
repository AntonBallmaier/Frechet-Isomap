package data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A {@link WeatherDataPolygonsFactory} is used to construct
 * {@link GeographicPolygon}s from weather data.
 *
 * <p>
 * Every one of those factories has a set of weighted {@link WeatherFeature}s
 * that define the feature space and it's scaling. Additionally there is the
 * time dimension with a scaling of 1 = 1 hour.
 *
 * <p>
 * A generated {@link GeographicPolygon} then represents the (quantized) weather
 * course at one position over a specific time window.
 *
 * <p>
 * Implementations may differ in the source data and therefore the amounts of
 * available locations.
 *
 * @author Anton Ballmaier
 *
 */
public abstract class WeatherDataPolygonsFactory {
	/**
	 * Converts a <code>List</code> of {@link GeographicPolygon}s to an array.
	 *
	 * @param polygons the {@link GeographicPolygon}s
	 * @return an array of the given {@link GeographicPolygon}s
	 */
	public static GeographicPolygon[] toArray(List<GeographicPolygon> polygons) {
		return polygons.toArray(new GeographicPolygon[polygons.size()]);
	}

	/**
	 * The {@link WeatherFeature}s and their scaling. This map also defines the
	 * order of the dimension as it's iteration order.
	 */
	private final LinkedHashMap<WeatherFeature, Double> features;

	/**
	 * Metadata for all the available positions. This contains information about
	 * longitude, latitude and elevation, but might contain some more important
	 * values. The <code>Integer</code> keys used may be arbitrary but fixed.
	 */
	private Map<Integer, double[]> metadata;

	/**
	 * Constructs a new {@link WeatherDataPolygonsFactory} object using the given
	 * weighted features.
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
	public WeatherDataPolygonsFactory(Map<WeatherFeature, Double> features) {
		this.features = new LinkedHashMap<>(features);
	}

	/**
	 * Constructs a {@link GeographicPolygon} dataset from weather data.
	 *
	 * <p>
	 * For every available position one of those polygons is created. Every point on
	 * its course corresponds to one point in time. The time in hours after the
	 * starting time is given by the first coordinate. Every other coordinate
	 * represents a weather feature at that position in that point in time.
	 *
	 * <p>
	 * The duration of the time window is one day, the time step size 1 hour.
	 *
	 * @param startTime the first point of time to include in the course of the
	 *                  polygon
	 * @return the generated dataset
	 * @throws IOException if the data could not be loaded
	 */
	public List<GeographicPolygon> create(Calendar startTime) throws IOException {
		return create(startTime, 24);
	}

	/**
	 * Constructs a {@link GeographicPolygon} dataset from weather data.
	 *
	 * <p>
	 * For every available position one of those polygons is created. Every point on
	 * its course corresponds to one point in time. The time in hours after the
	 * starting time is given by the first coordinate. Every other coordinate
	 * represents a weather feature at that position in that point in time.
	 *
	 * <p>
	 * The time step size 1 hour.
	 *
	 * @param startTime the first point of time to include in the course of the
	 *                  polygon
	 * @param duration  the duration of the time window
	 * @return the generated dataset
	 * @throws IOException if the data could not be loaded
	 */
	public List<GeographicPolygon> create(Calendar startTime, int duration) throws IOException {
		return create(startTime, duration, 1);
	}

	/**
	 * Constructs a {@link GeographicPolygon} dataset from weather data.
	 *
	 * <p>
	 * For every available position one of those polygons is created. Every point on
	 * its course corresponds to one point in time. The time in hours after the
	 * starting time is given by the first coordinate. Every other coordinate
	 * represents a weather feature at that position in that point in time.
	 *
	 * @param startTime the first point of time to include in the course of the
	 *                  polygon
	 * @param duration  the duration of the time window
	 * @param stepSize  the time step size in hours. Must be at least 1
	 * @return the generated dataset
	 * @throws IOException if the data could not be loaded
	 */
	public List<GeographicPolygon> create(Calendar startTime, int duration, int stepSize) throws IOException {
		return create(startTime, duration, stepSize, (x) -> true);
	}

	/**
	 * Constructs a {@link GeographicPolygon} dataset from weather data.
	 *
	 * <p>
	 * For every available position one of those polygons is created, if it matches
	 * the filter. Every point on its course corresponds to one point in time. The
	 * time in hours after the starting time is given by the first coordinate. Every
	 * other coordinate represents a weather feature at that position in that point
	 * in time.
	 *
	 * <p>
	 * Filtering the position for which polygons are generated works by providing a
	 * <code>Predicate</code>. This predicate can use the positions metadata (see
	 * {@link #readMetaData()}) to decide, whether or not to use that position.
	 *
	 * @param startTime the first point of time to include in the course of the
	 *                  polygon
	 * @param duration  the duration of the time window
	 * @param stepSize  the time step size in hours. Must be at least 1
	 * @param filter    the filter that decides which polygons to generate
	 * @return the generated dataset
	 * @throws IOException if the data could not be loaded
	 */
	public List<GeographicPolygon> create(Calendar startTime, int duration, int stepSize, Predicate<double[]> filter)
			throws IOException {
		final int timeSteps = (int) Math.ceil((double) duration / stepSize);
		if (metadata == null) {
			metadata = readMetaData();
		}

		final Map<Integer, double[][]> featureData = generateFeatureData(startTime, timeSteps, stepSize);
		final List<Integer> ids = new ArrayList<>(featureData.keySet());

		final List<GeographicPolygon> polygons = new ArrayList<>();
		for (final Integer id : ids) {
			final double[] meta = metadata.get(id);
			if (meta == null) {
				continue;
			}
			if (filter.test(meta)) {
				polygons.add(makeWeatherPolygon(featureData.get(id), meta));
			}
		}

		return polygons;
	}

	/**
	 * Constructs a {@link GeographicPolygon} from the given polygon data and
	 * additional metadata that might be required for the specific type of
	 * {@link GeographicPolygon}.
	 *
	 * @param data the polygon data
	 * @param meta the metadata
	 * @return the constructed {@link GeographicPolygon}
	 */
	protected abstract GeographicPolygon makeWeatherPolygon(double[][] data, double[] meta);

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
	protected abstract LinkedHashMap<Integer, double[]> readFeatureData(WeatherFeature feature, Calendar startTime,
			int timeSteps, int stepSize) throws IOException;

	/**
	 * Constructs the metadata of all available positions and maps them to the
	 * positions <code>Integer</code> key.
	 *
	 * @return the mapping from every available position key to the corresponding
	 *         metadata
	 * @throws IOException if the data could not be loaded
	 */
	protected abstract Map<Integer, double[]> readMetaData() throws IOException;

	/**
	 * Combines the data from multiple weather polygon features with the time
	 * values. The resulting data is the multidimensional course of the
	 * {@link GeographicPolygon}s.
	 *
	 * @param valueMaps the mappings from every available position key to the
	 *                  corresponding polygon data
	 * @param scalars   the scalars for each feature. Must be ordered like
	 *                  <code>valueMaps</code>
	 * @param timeSteps the number of time steps
	 * @param stepSize  the time step size in hours
	 * @return the mapping from every available position key to the corresponding
	 *         polygon data
	 */
	private Map<Integer, double[][]> combineFeatureData(List<Map<Integer, double[]>> valueMaps, List<Double> scalars,
			int timeSteps, int stepSize) {
		final Map<Integer, double[][]> combinedDataMap = new LinkedHashMap<>();
		final Set<Integer> ids = valueMaps.get(0).keySet();
		for (final int id : ids) {
			final double[][] combinedData = new double[timeSteps][valueMaps.size() + 1];
			boolean use = true;
			// Write feature values
			for (int i = 0; i < valueMaps.size(); i++) {
				final Map<Integer, double[]> valueMap = valueMaps.get(i);
				final double scalar = scalars.get(i);
				if (!valueMap.containsKey(id)) {
					use = false;
					break;
				}
				final double[] values = valueMap.get(id);
				for (int t = 0; t < timeSteps; t++) {
					combinedData[t][i + 1] = values[t] * scalar;
				}
			}
			if (!use) {
				continue;
			}

			// Write time values
			for (int t = 0; t < timeSteps; t++) {
				combinedData[t][0] = t * stepSize;
			}
			combinedDataMap.put(id, combinedData);
		}
		return combinedDataMap;
	}

	/**
	 * Constructs the course of all {@link GeographicPolygon}s for a given time
	 * window.
	 *
	 * @param startTime the start time of the time window
	 * @param timeSteps the number of time steps
	 * @param stepSize  the time step size in hours
	 * @return the mapping from every available position key to the corresponding
	 *         polygon data
	 * @throws IOException if the data could not be loaded
	 */
	private Map<Integer, double[][]> generateFeatureData(Calendar startTime, int timeSteps, int stepSize)
			throws IOException {
		final List<Map<Integer, double[]>> valueMaps = new LinkedList<>();
		final List<Double> scalars = new LinkedList<>();
		// parallel:
		features.entrySet().parallelStream().forEach(entry -> {
			try {
				valueMaps.add(readFeatureData(entry.getKey(), startTime, timeSteps, stepSize));
				scalars.add(entry.getValue());
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		});

		return combineFeatureData(valueMaps, scalars, timeSteps, stepSize);
	}
}
