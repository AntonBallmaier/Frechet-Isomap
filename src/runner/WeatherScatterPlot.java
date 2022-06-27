package runner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LightPoint;

import data.GeographicPolygon;
import data.WeatherDataPolygonsFactory;
import data.WeatherFeature;
import embedding.Embedder;
import frechetDistance.Polygon;

/**
 * A {@link ScatterPlot} displaying a set of points in 3d space. The 3d model
 * can than be viewed from different angles to allow for a good visualization of
 * the structure of the plotted data.
 *
 * <p>
 * The points to be plotted are the result of embedding
 * {@link GeographicPolygon}s. The color of resulting points is generated using
 * different {@link WeatherPolygonPainter}s. This class also allows to
 * periodically toggle the coloring scheme.
 *
 * <p>
 * Every implementation of this class must implement a method
 * {@link #getDataFactory(Map)} providing the {@link GeographicPolygon} dataset
 * as well as {@link #getEmbedder(Polygon[])} to construct an {@link Embedder}
 * used to map the {@link Polygon}s to points.
 *
 * @author Anton Ballmaier
 *
 */
public abstract class WeatherScatterPlot extends ScatterPlot {
	/**
	 * The points displayed in the scatter plot
	 */
	List<LightPoint> points;

	/**
	 * The dataset to be embedded
	 */
	private GeographicPolygon[] dataset;

	/**
	 * The filter to decide whether to keep a {@link GeographicPolygon} or not
	 */
	private Predicate<double[]> filter;

	/**
	 * Constructs a new {@link WeatherScatterPlot} object using the given point
	 * size.
	 *
	 * @param pointSize the size of plotted points in pixels. The points will be
	 *                  drawn as squares with this value as their side length.
	 */
	protected WeatherScatterPlot(float pointSize) {
		super(pointSize);
	}

	/**
	 * Start to cycle through different coloring schemes. This method will not
	 * terminate! The duration every coloring scheme can be chosen.
	 *
	 * @param delay the duration every coloring scheme is used before the next
	 *              switch
	 */
	public void animateColor(int delay) {
		final WeatherPolygonPainter[] painters = new WeatherPolygonPainter[] { new ColorByLongitude(),
				new ColorByLatitude(), new ColorByElevation() };

		while (true) {
			for (final WeatherPolygonPainter painter : painters) {
				for (int i = 0; i < dataset.length; i++) {
					final LightPoint p = points.get(i);
					final int[] rgb = painter.colorOf(dataset[i]);
					p.rgb = new Color(rgb[0], rgb[1], rgb[2]);
				}
				System.out.println("Current Painter: " + painter.getClass().getSimpleName());

				try {
					Thread.sleep(delay * 1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Sets the data filter. To disable the filter call this method with
	 * <code>null</code> as parameter. The filter is used to decide whether to keep
	 * a {@link GeographicPolygon} in the dataset or not
	 *
	 * @param filter the filter to decide whether to keep a
	 *               {@link GeographicPolygon} or not
	 */
	public void setDataFilter(Predicate<double[]> filter) {
		this.filter = filter;
	}

	/**
	 * Generates and saves the dataset to be embedded using the
	 * {@link WeatherDataPolygonsFactory} provided by any implementation of this
	 * class. If a {@link #filter} is defined, it will be used to potentially thin
	 * out the data set.
	 */
	protected void generateDataset() {
		final WeatherDataPolygonsFactory factory = getDataFactory(getDefaultFeatureMap());
		try {
			final List<GeographicPolygon> polygonList;

			if (filter == null) {
				polygonList = factory.create(getDefaultStartTime(), 24, 3);
			} else {
				polygonList = factory.create(getDefaultStartTime(), 24, 3, filter);
			}
			dataset = WeatherDataPolygonsFactory.toArray(polygonList);

		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Constructs a factory for {@link GeographicPolygon}s using the given weighted
	 * {@link WeatherFeature}s.
	 *
	 * @param features the weighted {@link WeatherFeature}s present in the generated
	 *                 {@link GeographicPolygon}s.
	 * @return the {@link WeatherDataPolygonsFactory} used to generate the dataset
	 *         to be embedded
	 */
	protected abstract WeatherDataPolygonsFactory getDataFactory(Map<WeatherFeature, Double> features);

	/**
	 * Generates and returns the weighted features used for the {@link Polygon}
	 * construction.
	 *
	 * @return the default map of weighted {@link WeatherFeature}s
	 */
	protected Map<WeatherFeature, Double> getDefaultFeatureMap() {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.TEMPERATURE, 1d);
		features.put(WeatherFeature.WIND_SPEED, 2d);
		features.put(WeatherFeature.PRESSURE, 2d);
		features.put(WeatherFeature.HUMIDITY, 10d);
		return features;
	}

	/**
	 * Generates and returns the start time of the time window used for the
	 * {@link Polygon} construction.
	 *
	 * @return the default start time
	 */
	protected Calendar getDefaultStartTime() {
		final Calendar startTime = new GregorianCalendar(2012, 7, 30, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);
		return startTime;
	}

	/**
	 * Returns an {@link Embedder} used to map the {@link Polygon}s to points. These
	 * points can than be plotted in 3d.
	 *
	 * @param polygons the {@link Polygon}s to construct an {@link Embedder} for
	 * @return the constructed {@link Embedder}
	 */
	protected abstract Embedder<Polygon> getEmbedder(Polygon[] polygons);

	/**
	 * Returns the set of <code>LightPoints</code> to be plotted. Every
	 * <code>LightPoint</code> corresponds to a {@link GeographicPolygon} embedded
	 * in 3d space. The color of these points is not dependent on their embedding
	 * position but on meta data of the corrsponding {@link Polygon}.
	 *
	 * @return the list of colored points to be plotted
	 */
	@Override
	protected List<LightPoint> getPoints() {
		generateDataset();
		final Embedder<Polygon> embedder = getEmbedder(dataset);
		final double[][] embedding = embedder.embed(3);
		final Color[] colors = getColors(dataset);

		final int size = embedding[0].length;
		points = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			points.add(new LightPoint(
					new Coord3d((float) embedding[0][i], (float) embedding[1][i], (float) embedding[2][i]), colors[i]));
		}

		return points;
	}

	/**
	 * Returns the colors of each {@link GeographicPolygon} in the dataset. These
	 * colors are used to color the points resulting from embedding these.
	 *
	 * @param polygons the {@link Polygon}s
	 * @return the colors corresponding to the given {@link Polygon}s
	 */
	private Color[] getColors(GeographicPolygon[] polygons) {
		final WeatherPolygonPainter painter = new ColorByLatitude();
		final Color[] colors = new Color[polygons.length];
		for (int i = 0; i < polygons.length; i++) {
			final int[] rgb = painter.colorOf(polygons[i]);
			colors[i] = new Color(rgb[0], rgb[1], rgb[2]);
		}
		return colors;
	}
}
