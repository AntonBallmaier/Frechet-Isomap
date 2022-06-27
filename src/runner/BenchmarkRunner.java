package runner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import data.GeographicPolygon;
import data.WeatherDataPolygonsFactory;
import data.WeatherFeature;
import data.WeatherGridDataPolygonsFactory;
import embedding.Embedder;
import embedding.Isomap;
import embedding.MultidimensionalScaling;
import embedding.measures.InterruptionMeasure;
import embedding.measures.Measure;
import embedding.nearestNeighbors.ConnectedComponents;
import embedding.nearestNeighbors.KNearestNeighborGraph;
import embedding.nearestNeighbors.NearestNeighborDescend;
import embedding.shortestPaths.AdjacencyList;
import embedding.shortestPaths.DijkstraAPSP;
import frechetDistance.ApproxFrechet;
import frechetDistance.DiscreteFrechet;
import frechetDistance.IntegralDiscreteFrechet;
import frechetDistance.Polygon;

/**
 * This class contains a runner used to measure the runtime of the core frechet
 * distance and {@link Isomap} implementations.
 *
 * <p>
 * Based on the machine this code is performed on it might take multiple hours
 * to finish the calculations. For comparable results the test should be run
 * multiple times and averaged. The machine should not be used in the meantime.
 *
 * <p>
 * The {@link Polygon}s used for the speed benchmarks are generated from a
 * {@link WeatherGridDataPolygonsFactory}.
 *
 * @author Anton Ballmaier
 *
 */
public class BenchmarkRunner {

	/**
	 * List of {@link Measure}s used in the benchmark.
	 */
	@SuppressWarnings("unchecked")
	final static Measure<Polygon>[] MEASURES = new Measure[] { DiscreteFrechet.getInstance(),
			IntegralDiscreteFrechet.getInstance(), new ApproxFrechet() };

	/**
	 * Length of the {@link Polygon} lists used for measure benchmarks. The number
	 * of resulting measurements is the square of this constant.
	 */
	private static final int MEASURE_POLYGONS_AMOUNT = 500;

	/**
	 * Main method making this class a runner for the benchmark it implements.
	 *
	 * <p>
	 * A {@link BenchmarkRunner}-Instance is initialized and used to perform the
	 * runtime test.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		final int embeddingPolygonLength = 30;
		final int[] embeddingPolygonAmounts = { 1000, 10000, 100000 };
		final int[] dimensions = { 1, 2, 3, 5, 10 };
		final int[] landmarkAmounts = { 100, 300, 1000 };
		final int[] neighborhoodSizes = { 3, 5, 10, 20 };
		final int[] polygonLength = { 3, 10, 30, 100 };

		final BenchmarkRunner benchmark = new BenchmarkRunner(embeddingPolygonLength, embeddingPolygonAmounts,
				dimensions, landmarkAmounts, neighborhoodSizes, polygonLength);

		benchmark.warmup();
		benchmark.isomapBenchmark();
		benchmark.measureBenchmark();
	}

	/**
	 * Lists of {@link Polygon}s used for the measure speed benchmark
	 */
	private List<List<Polygon>> distancePolygons;

	/**
	 * Numbers of dimensions the data set is embedded in during the {@link Isomap}
	 * speed benchmark
	 */
	private final int[] embeddingDimensions;

	/**
	 * Lengths of the {@link Polygon} arrays used in the {@link Isomap} speed
	 * benchmark
	 */
	private final int[] embeddingPolygonAmounts;

	/**
	 * Length of the Polygons used in the {@link Isomap} speed benchmark
	 */
	private final int embeddingPolygonLength;

	/**
	 * Array of {@link Polygon}s used for the {@link Isomap} speed benchmark
	 */
	private Polygon[][] embeddingPolygons;

	/**
	 * Numbers of landmarks used during the {@link Isomap} speed benchmark
	 */
	private final int[] landmarkAmounts;

	/**
	 * Numbers of nearest neighbors used to construct a graph from during the
	 * {@link Isomap} speed benchmark
	 */
	private final int[] neighborhoodSizes;

	/**
	 * Lengths of the {@link Polygon}s used for the measure speed benchmark
	 */
	private final int[] polygonLengths;

	/**
	 * {@link Polygon}s that are embedded as a warmup for the Java Hotspot-VM
	 */
	private Polygon[] warmupSet;

	/**
	 * Constructs a new {@link BenchmarkRunner} object using the given parameters
	 * for speed tests. Runtimes of all feasible combinations of these parameters
	 * will be measured.
	 *
	 * <p>
	 * All needed {@link Polygon} datasets are initialized upon creation. Therefore
	 * instantiation might take some time.
	 *
	 * @param embeddingPolygonLength  the length of the Polygons used in the
	 *                                {@link Isomap} speed benchmark
	 * @param embeddingPolygonAmounts the lengths of the {@link Polygon} arrays used
	 *                                in the {@link Isomap} speed benchmark
	 * @param embeddingDimensions     the numbers of dimensions the data set is
	 *                                embedded in during the {@link Isomap} speed
	 *                                benchmark
	 * @param landmarkAmounts         the numbers of landmarks used during the
	 *                                {@link Isomap} speed benchmark
	 * @param neighborhoodSizes       the numbers of nearest neighbors used to
	 *                                construct a graph from during the
	 *                                {@link Isomap} speed benchmark
	 * @param polygonLengths          the lengths of the {@link Polygon}s used for
	 *                                the measure speed benchmark
	 */
	public BenchmarkRunner(int embeddingPolygonLength, int[] embeddingPolygonAmounts, int[] embeddingDimensions,
			int[] landmarkAmounts, int[] neighborhoodSizes, int[] polygonLengths) {
		this.embeddingPolygonLength = embeddingPolygonLength;
		this.embeddingPolygonAmounts = embeddingPolygonAmounts;
		this.embeddingDimensions = embeddingDimensions;
		this.landmarkAmounts = landmarkAmounts;
		this.neighborhoodSizes = neighborhoodSizes;
		this.polygonLengths = polygonLengths;
		setupPolygons();
	}

	/**
	 * Uses a variety of methods, whose runtime should be tested later.
	 *
	 * <p>
	 * The Java Hotspot-VM optimizes the code during runtime. Therefore it is
	 * important to let it do most of these optimizations before starting the speed
	 * benchmark.
	 *
	 * <p>
	 * To warm up the full variety of methods, a dataset is simply embedded using
	 * {@link Isomap} and the different {@link Measure}s.
	 */
	public void warmup() {
		Embedder<Polygon> isomap;
		for (int i = 0; i < 5; i++) {
			for (final Measure<Polygon> measure : MEASURES) {
				isomap = new Isomap<>(warmupSet, measure, 5, 100);
				isomap.embed(5);
			}

		}
	}

	/**
	 * Appends the required polygon datasets to the given {@link List}.
	 *
	 * <p>
	 * Since {@link Polygon}s generated from a
	 * {@link WeatherGridDataPolygonsFactory} are used for the speed benchmarks, a
	 * Map of {@link WeatherFeature}s must be given defining those {@link Polygon}s.
	 *
	 * <p>
	 * All these lists have a predefiend length of {@link #MEASURE_POLYGONS_AMOUNT}
	 *
	 * @param polygonLists the {@link List} to add the generated {@link Polygon}
	 *                     lists to
	 * @param features     the features and weights used to generate
	 *                     {@link Polygon}s. The size of this parameter determines
	 *                     the dimension of the {@link Polygon}s.
	 */
	@SuppressWarnings("unchecked")
	private void addPolygonListsFromFeatures(List<List<Polygon>> polygonLists, Map<WeatherFeature, Double> features) {
		final Calendar startTime = new GregorianCalendar(2012, 7, 15, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);
		for (final int l : polygonLengths) {
			List<GeographicPolygon> polygonList = null;
			try {
				polygonList = factory.create(startTime, l, 1, WeatherGridDataPolygonsFactory.gridFilter(10));
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
			Collections.shuffle(polygonList);
			polygonLists.add((List<Polygon>) (List<?>) polygonList.subList(0, MEASURE_POLYGONS_AMOUNT));
		}
	}

	/**
	 * Measures and prints runtimes of all the required components for calculating
	 * an embedding using {@link Isomap}. All feasible combinations of the
	 * benchmarks parameters.
	 *
	 * <p>
	 * Using these abbreviations
	 * <ul>
	 * <li>n = embeddingPolygonAmounts
	 * <li>k = neighborhoodSizes
	 * <li>&delta; = measures
	 * <li>l = landmarkAmounts
	 * <li>d = embeddingDimensions
	 * </ul>
	 * the tested combinations are
	 * <ul>
	 * <li>n&times;k&times;&delta; for neighborhood graphs
	 * <li>n&times;k&times;&delta; for connecting graph components
	 * <li>n&times;k&times;l for calculating shortest paths
	 * <li>n&times;l&times;d for embedding using LMDS
	 * </ul>
	 */
	private void isomapBenchmark() {
		long start;
		for (final Polygon[] testSet : embeddingPolygons) {
			System.out.println(String.format("\n\nn=%d", testSet.length));
			for (final int k : neighborhoodSizes) {
				System.out.println(String.format("\nk=%d", k));
				AdjacencyList nnGraph = null;
				for (final Measure<Polygon> measure : MEASURES) {
					System.out.println(String.format("Next Mesure: %s", measure.getClass().getSimpleName()));
					final KNearestNeighborGraph<Polygon> nnd = new NearestNeighborDescend<>(k, testSet, measure);
					start = System.nanoTime();
					nnGraph = nnd.nnGraph();
					System.out.println(String.format("NNDescend: %.6f", (System.nanoTime() - start) * 1e-9f));
					final InterruptionMeasure<Integer> indexMeasure = (a, b, max) -> measure.distanceCapped(testSet[a],
							testSet[b], max);

					start = System.nanoTime();
					new ConnectedComponents(nnGraph).connect(indexMeasure);
					System.out.println(String.format("Connect: %.6f", (System.nanoTime() - start) * 1e-9f));
				}
				for (final int l : landmarkAmounts) {
					System.out.println(String.format("\nl=%d", l));
					start = System.nanoTime();
					final double[][] distances = new DijkstraAPSP(nnGraph).shortestPaths(l);
					System.out.println(String.format("Paths: %.6f", (System.nanoTime() - start) * 1e-9f));
					if (k == neighborhoodSizes[0]) {
						for (final int d : embeddingDimensions) {
							start = System.nanoTime();
							MultidimensionalScaling.landmark(distances, d);
							System.out.println(
									String.format("LMDS (d=%d): %.6f", d, (System.nanoTime() - start) * 1e-9f));
						}
					}
				}
			}
		}
	}

	/**
	 * Measures and prints runtimes of average measure durations. All combinations
	 * of some polygon dimensions, polygon lengths and measures are tested.
	 *
	 * <p>
	 * The printed time values are given in &mu;s per measurement, averaged over
	 * many measurements.
	 */
	private void measureBenchmark() {
		final int measurementsPerTest = MEASURE_POLYGONS_AMOUNT * MEASURE_POLYGONS_AMOUNT;
		final double timeFactor = 1e-3 / measurementsPerTest;

		long start;
		for (final List<Polygon> testSet : distancePolygons) {
			System.out.println(String.format("|p|=%d, d=%d", testSet.get(0).length, testSet.get(0).dimension));
			for (final Measure<Polygon> measure : MEASURES) {
				start = System.nanoTime();
				for (final Polygon p : testSet) {
					for (final Polygon q : testSet) {
						measure.distance(p, q);
					}
				}
				System.out.println(String.format("%.6f", (System.nanoTime() - start) * timeFactor));
			}
		}
	}

	/**
	 * Generates all the {@link Polygon}s required for performing the benchmarks and
	 * stores them.
	 */
	private void setupPolygons() {
		embeddingPolygons = setupPolygonsForIsomap();
		distancePolygons = setupPolygonsForMeasures();
		warmupSet = embeddingPolygons[0];
	}

	/**
	 * Generates the {@link Polygon}s required in the {@link Isomap} speed
	 * benchmark.
	 *
	 * <p>
	 * The generated {@link Polygon}s are 3-dimensional and have the defined length.
	 * They are taken from the grid weather data set using a
	 * {@link WeatherGridDataPolygonsFactory}.
	 *
	 * @return the {@link Polygon} arrays used in the {@link Isomap} speed benchmark
	 */
	private Polygon[][] setupPolygonsForIsomap() {
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();
		features.put(WeatherFeature.TEMPERATURE, 1d);
		features.put(WeatherFeature.WIND_SPEED, 2d);

		final WeatherDataPolygonsFactory factory = new WeatherGridDataPolygonsFactory(features);

		final Calendar startTime = new GregorianCalendar(2012, 7, 15, 0, 0, 0);
		startTime.set(Calendar.MILLISECOND, 0);

		List<GeographicPolygon> polygonList = null;
		try {
			polygonList = factory.create(startTime, embeddingPolygonLength, 1);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		Collections.shuffle(polygonList);
		final Polygon[] polygons = WeatherDataPolygonsFactory.toArray(polygonList);

		final Polygon[][] testSets = new Polygon[embeddingPolygonAmounts.length][];
		for (int i = 0; i < embeddingPolygonAmounts.length; i++) {
			testSets[i] = Arrays.copyOfRange(polygons, 0, embeddingPolygonAmounts[i]);
		}

		return testSets;
	}

	/**
	 * Generates the {@link Polygon}s required in the {@link Measure} speed
	 * benchmark.
	 *
	 * <p>
	 * The generated {@link Polygon}s have all combinations of the dimensions
	 * (2,3,5) and the defined lengths. They are taken from the grid weather data
	 * set using a {@link WeatherGridDataPolygonsFactory}.
	 *
	 * @return the {@link Polygon} lists used in the {@link Measure} speed benchmark
	 */
	private List<List<Polygon>> setupPolygonsForMeasures() {
		final List<List<Polygon>> polygonLists = new LinkedList<>();
		final Map<WeatherFeature, Double> features = new LinkedHashMap<>();

		features.put(WeatherFeature.TEMPERATURE, 1d);
		addPolygonListsFromFeatures(polygonLists, features);

		features.put(WeatherFeature.WIND_SPEED, 2d);
		addPolygonListsFromFeatures(polygonLists, features);

		features.put(WeatherFeature.PRESSURE, 2d);
		features.put(WeatherFeature.HUMIDITY, 10d);
		addPolygonListsFromFeatures(polygonLists, features);

		return polygonLists;
	}
}
