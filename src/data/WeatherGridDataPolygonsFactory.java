package data;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFiles;
import ucar.nc2.Variable;

/**
 * A {@link WeatherGridDataPolygonsFactory} is used to construct
 * {@link GeographicGridPolygon}s from the grid weather dataset.
 *
 * <p>
 * The internally used <code>Integer</code> key of every position is based on
 * the grid position.
 *
 * @author Anton Ballmaier
 *
 */
public class WeatherGridDataPolygonsFactory extends WeatherDataPolygonsFactory {

	/**
	 * A filter that sorts out every other position in a checkerboard-like pattern
	 */
	public static Predicate<double[]> checkerboardFilter = metadata -> (metadata[3] + metadata[4]) % 2 == 0;

	/**
	 * The available latitudinal steps of the grid weather dataset
	 */
	public static int LATIDUTE_STEPS = 719;

	/**
	 * The available longitudinal steps of the grid weather dataset
	 */
	public static int LONGITUDE_STEPS = 937;

	/**
	 * The naming pattern all the required data files follow
	 */
	private static String FILE_NAME_FORMAT = "%s_%s.nc.%s";

	/**
	 * The directory where the fetched data is saved
	 */
	private static String LOCAL_DIRECTORY = "data/weather/project-try/";

	/**
	 * The server directory from where missing files may be fetched
	 */
	private static String SERVER_DIRECTORY = "https://opendata.dwd.de/climate_environment/CDC/grids_germany/hourly/Project_TRY/";

	/**
	 * Constructs a filter that leaves only a subgrid of the dataset. The amount of
	 * remaining positions is therefore reduced roughly quadraticly dependent on the
	 * subgrid step size
	 *
	 * @param stepSize the distance between to remaining positions in both grid
	 *                 coordinates
	 * @return the filter leaving only the subgrid of the given step size
	 */
	public static Predicate<double[]> gridFilter(int stepSize) {
		return metadata -> (metadata[3] % stepSize) + (metadata[4] % stepSize) == 0;
	}

	/**
	 * Constructs a string as a time representation. The string follows the specific
	 * format "yyyymm" which is used in the names of the data files.
	 *
	 * @param calendar a time
	 * @return the string representation of its month
	 */
	private static String idFromCalendar(Calendar calendar) {
		if (calendar.get(Calendar.YEAR) < 1995 || calendar.get(Calendar.YEAR) > 2012) {
			throw new IllegalArgumentException("No data is known for the given date.");
		}
		return String.format("%04d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1);
	}

	/**
	 * Constructs a new {@link WeatherGridDataPolygonsFactory} object using the
	 * given weighted features.
	 *
	 * <p>
	 * These define the feature space and it's scaling. A mapping of a feature f to
	 * the value x means: Include data about feature f in the Polygons and scale
	 * these data values by a factor of x.
	 *
	 * <p>
	 * A generated {@link GeographicGridPolygon} then represents the (quantized)
	 * weather course at one position over a later specified time window.
	 *
	 * @param features the {@link WeatherFeature}s to use and how to scale them
	 */
	public WeatherGridDataPolygonsFactory(Map<WeatherFeature, Double> features) {
		super(features);
	}

	/**
	 * Constructs a {@link GeographicPolygon} from the given polygon data and
	 * additional metadata containing the latitude, longitude, elevation and grid
	 * position.
	 *
	 * <p>
	 * The resulting {@link GeographicPolygon} is a {@link GeographicGridPolygon}
	 * and may be cast.
	 *
	 * @param data the polygon data
	 * @param meta the metadata, where
	 *             <ul>
	 *             <li><code>meta[0]</code> is the latitude
	 *             <li><code>meta[1]</code> is the longitude
	 *             <li><code>meta[2]</code> is the elevation
	 *             <li><code>meta[3]</code> is the first grid coordinate
	 *             <li><code>meta[4]</code> is the second grid coordinate
	 *             </ul>
	 * @return the constructed {@link GeographicPolygon}
	 */
	@Override
	protected GeographicPolygon makeWeatherPolygon(double[][] data, double[] meta) {
		final double lat = meta[0];
		final double lon = meta[1];
		final double elevation = meta[2];
		final int x = (int) meta[3];
		final int y = (int) meta[4];
		return new GeographicGridPolygon(data, lon, lat, elevation, x, y);
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
		final List<Array> dataArrays = getRawDataArrays(feature, startTime, timeSteps, stepSize);
		final List<Index> indices = dataArrays.stream().map(arr -> arr.getIndex()).collect(Collectors.toList());
		for (int x = 0; x < LATIDUTE_STEPS; x++) {
			for (int y = 0; y < LONGITUDE_STEPS; y++) {
				final double firstValue = dataArrays.get(0).getDouble(indices.get(0).set(0, y, x));
				if (firstValue == defaultValue(feature)) {
					continue;
				}
				final double[] data = new double[timeSteps];
				int alreadyFilled = 0;
				for (int i = 0; i < dataArrays.size(); i++) {
					final int currentArrayLength = indices.get(i).getShape(0);
					for (int t = 0; t < currentArrayLength; t++) {
						data[alreadyFilled + t] = dataArrays.get(i).getDouble(indices.get(i).set(t, y, x));
					}
					alreadyFilled += currentArrayLength;
				}
				featureData.put(idFromXY(x, y), data);
			}
		}

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
	 * <li><code>meta[3]</code> is the first grid coordinate
	 * <li><code>meta[4]</code> is the second grid coordinate
	 * </ul>
	 *
	 * @return the mapping from every available position key to the corresponding
	 *         metadata, where
	 *         <ul>
	 *         <li><code>meta[0]</code> is the latitude
	 *         <li><code>meta[1]</code> is the longitude
	 *         <li><code>meta[2]</code> is the elevation
	 *         <li><code>meta[3]</code> is the first grid coordinate
	 *         <li><code>meta[4]</code> is the second grid coordinate
	 *         </ul>
	 * @throws IOException if the data could not be loaded
	 */
	@Override
	protected Map<Integer, double[]> readMetaData() throws IOException {
		final Map<Integer, double[]> metaData = new HashMap<>();
		final Calendar month = Calendar.getInstance();
		month.clear();
		month.set(1995, 0, 1);
		// Use Pressure, because it has the smallest file size. Every other feature
		// works the same.
		final NetcdfFile file = getFile(WeatherFeature.PRESSURE, month);

		final Array lonArr = file.findVariable("lon").read();
		final Array latArr = file.findVariable("lat").read();

		final Index lonInd = lonArr.getIndex();
		final Index latInd = latArr.getIndex();

		for (int y = 0; y < LONGITUDE_STEPS; y++) {
			for (int x = 0; x < LATIDUTE_STEPS; x++) {
				final double elevation = ReadElevationData.getElevation(x, y);
				if (Double.isNaN(elevation)) {
					// This means the point isn't in Germany.
					continue;
				}

				final double lon = lonArr.getDouble(lonInd.set(y, x));
				final double lat = latArr.getDouble(latInd.set(y, x));
				metaData.put(idFromXY(x, y), new double[] { lat, lon, elevation, x, y });
			}
		}

		return metaData;
	}

	/**
	 * Maps a <code>WeatherFeature</code> to the corresponding attribute name in the
	 * NetCDF data files.
	 *
	 * @param feature the feature
	 * @return the attribute name
	 */
	private String attributeName(WeatherFeature feature) {
		switch (feature) {
		case CLOUDS:
			return "CF";
		case DEW_POINT:
			return "dewpoint";
		case HUMIDITY:
			return "humidity";
		case PRESSURE:
			return "SLP";
		case TEMPERATURE:
			return "temperature";
		case WIND_SPEED:
			return "FF";
		default:
			throw new IllegalArgumentException("Unexpected value: " + feature);
		}
	}

	/**
	 * Convenience method for reliably closing {@link Closeable}s like
	 * {@link Stream}s.
	 *
	 * @param closeable the object to be closed
	 */
	private void closeStream(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Maps a <code>WeatherFeature</code> to the corresponding default value in the
	 * netCDF data files. This value is used to detect which positions are
	 * unavailable.
	 *
	 * @param feature the feature
	 * @return the default value
	 */
	private double defaultValue(WeatherFeature feature) {
		switch (feature) {
		case CLOUDS:
		case HUMIDITY:
		case PRESSURE:
			return 1e30f;
		case DEW_POINT:
		case TEMPERATURE:
		case WIND_SPEED:
			return 9999f;
		default:
			throw new IllegalArgumentException("Unexpected value: " + feature);
		}
	}

	/**
	 * Maps a <code>WeatherFeature</code> to the corresponding directory name in the
	 * data folder.
	 *
	 * @param feature the feature
	 * @return the directory name
	 */
	private String directoryName(WeatherFeature feature) {
		switch (feature) {
		case CLOUDS:
			return "cloud_cover";
		case DEW_POINT:
			return "dew_point";
		case HUMIDITY:
			return "humidity";
		case PRESSURE:
			return "pressure";
		case TEMPERATURE:
			return "air_temperature_mean";
		case WIND_SPEED:
			return "wind_speed";
		default:
			throw new IllegalArgumentException("Unexpected value: " + feature);
		}
	}

	/**
	 * Downloads the a compressed NetCDF file from the database server and saves it
	 * in the local data directory.
	 *
	 *
	 * @param feature the weather feature for which the data is required
	 * @param id      the id of the time window for which the data is required
	 * @see #SERVER_DIRECTORY
	 * @see #LOCAL_DIRECTORY
	 */
	private void downloadFromDatabase(WeatherFeature feature, String id) {
		final String url = getPath(feature, id, SERVER_DIRECTORY);
		final String local = getPath(feature, id, LOCAL_DIRECTORY);
		makeDirectory(feature);
		System.out.println("Downloading " + getPath(feature, id, ""));
		final byte dataBuffer[] = new byte[1024];
		int bytesRead;

		InputStream inputStream = null;
		OutputStream outputStream = null;

		try {
			inputStream = new BufferedInputStream(new URL(url).openStream());
			outputStream = new FileOutputStream(local);
			while ((bytesRead = inputStream.read(dataBuffer, 0, 1024)) != -1) {
				outputStream.write(dataBuffer, 0, bytesRead);
			}
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		} finally {
			closeStream(inputStream);
			closeStream(outputStream);
		}
	}

	/**
	 * Maps a <code>WeatherFeature</code> and the id of a month to the corresponding
	 * file name.
	 *
	 * @param feature the feature
	 * @param id      the id of the month for that the file name should be generated
	 * @return the file name (including file ending)
	 */
	private String fileName(WeatherFeature feature, String id) {
		switch (feature) {
		case CLOUDS:
			return String.format(FILE_NAME_FORMAT, "N", id, "gz");
		case DEW_POINT:
			return String.format(FILE_NAME_FORMAT, "TD", id, "gz");
		case HUMIDITY:
			return String.format(FILE_NAME_FORMAT, "RH", id, "gz");
		case PRESSURE:
			return String.format(FILE_NAME_FORMAT, "PRED", id, "gz");
		case TEMPERATURE:
			return String.format(FILE_NAME_FORMAT, "TT", id, "bz2");
		case WIND_SPEED:
			return String.format(FILE_NAME_FORMAT, "FF", id, "gz");
		default:
			throw new IllegalArgumentException("Unexpected value: " + feature);
		}
	}

	private String getDirectory(WeatherFeature feature, String root) {
		return root + directoryName(feature) + "/";
	}

	/**
	 * Provides the corresponding NetCDF-file for a given month. If the file isn't
	 * present it will be downloaded first.
	 *
	 * @param feature the feature
	 * @param month   the month of the point in time for which the data is required
	 * @return the object representation of the requested file
	 */
	private NetcdfFile getFile(WeatherFeature feature, Calendar month) {
		final String id = idFromCalendar(month);
		final String path = getPath(feature, id, LOCAL_DIRECTORY);
		final File file = new File(path);
		if (!file.exists()) {
			downloadFromDatabase(feature, id);
		}
		NetcdfFile ncfile = null;
		try {
			ncfile = NetcdfFiles.open(path);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return ncfile;
	}

	/**
	 * Constructs the full file path for a single data file from a root directory, a
	 * feature and the time id
	 *
	 * @param feature the feature
	 * @param id      the id of the point in time for which the data is required
	 * @param root    the root directory
	 * @return the file path
	 */
	private String getPath(WeatherFeature feature, String id, String root) {
		return getDirectory(feature, root) + fileName(feature, id);
	}

	/**
	 * Returns the netCDF arrays containing the required data for a certain weather
	 * feature and time window. The returned <code>List</code> will only contain
	 * multiple entries, if the time window contains multiple months.
	 *
	 * @param feature   the feature
	 * @param startTime start time of the time window
	 * @param timeSteps the number of time steps
	 * @param stepSize  the time step size in hours
	 * @return the required netCDF data arrays
	 * @throws IOException if the data could not be loaded
	 */
	private ArrayList<Array> getRawDataArrays(WeatherFeature feature, Calendar startTime, int timeSteps, int stepSize)
			throws IOException {
		final ArrayList<Array> rawDataArrays = new ArrayList<>();
		int offsetCarryOver = 0;

		startTime = (Calendar) startTime.clone();
		final Calendar endTime = (Calendar) startTime.clone();
		endTime.add(Calendar.HOUR, timeSteps * stepSize);
		while (monthsInTotal(startTime) <= monthsInTotal(endTime)) {
			Calendar endOfMonth = null;
			if (monthsInTotal(startTime) >= monthsInTotal(endTime)) {
				endOfMonth = endTime;
			} else {
				endOfMonth = (Calendar) startTime.clone();
				endOfMonth.add(Calendar.MONTH, 1);
				endOfMonth.set(Calendar.DATE, 0);
				endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
			}
			final int from = hourInMonth(startTime) + offsetCarryOver;
			final int to = hourInMonth(endOfMonth) - 1;
			final Variable variable = getFile(feature, startTime).findVariable(attributeName(feature));
			try {
				rawDataArrays.add(variable.read(String.format("%d:%d:%d, :, :", from, to, stepSize)));
			} catch (final InvalidRangeException e) {
				throw new RuntimeException(e);
			}
			offsetCarryOver = (stepSize - (to - from + 1) % stepSize) % stepSize;
			startTime.add(Calendar.MONTH, 1);
			startTime.set(Calendar.DATE, 1);
			startTime.set(Calendar.HOUR_OF_DAY, 0);
			if (startTime.after(endTime)) {
				break;
			}
		}
		return rawDataArrays;
	}

	/**
	 * Returns the number of a specific hour counting from the start of that month.
	 *
	 * @param calendar the point in time
	 * @return the number of the hour counting from the start of that month
	 */
	private int hourInMonth(Calendar calendar) {
		return calendar.get(Calendar.HOUR_OF_DAY) + (calendar.get(Calendar.DATE) - 1) * 24;
	}

	/**
	 * Constructs the unique <code>Integer</code> key from the grid coordinates
	 *
	 * @param x the first grid coordinate
	 * @param y the first grid coordinate
	 * @return the <code>Integer</code> key
	 */
	private int idFromXY(int x, int y) {
		return LONGITUDE_STEPS * y + x;
	}

	/**
	 * Makes sure the data directory for a specific weather feature exists by
	 * creating it if not present.
	 *
	 * @param feature the feature
	 */
	private void makeDirectory(WeatherFeature feature) {
		final String path = getDirectory(feature, LOCAL_DIRECTORY);
		final File directory = new File(path);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}

	/**
	 * Returns the total number of months that went by up to a specific point in
	 * time, not restarting the count in every year.
	 *
	 * @param calendar a point in time
	 * @return the number of months that are over
	 */
	private int monthsInTotal(Calendar calendar) {
		return calendar.get(Calendar.MONTH) + calendar.get(Calendar.YEAR) * 12;
	}

}
