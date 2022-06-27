package embedding;

import embedding.measures.Measure;

/**
 * An {@link Embedder} to calculate coordinates for an array of certain objects.
 *
 * <p>
 * Given an array of any type, an {@link Embedder} can be used to calculate
 * coordinates in n-dimensional euclidean space that preserve some distance
 * function between these objects as well as possible. For that a
 * {@link Measure} for the type of objects must be given for construction.
 * However, the distances to be preserved are not necessarily the direct
 * distances given by the {@link Measure}. The definition of the distances to be
 * preserved depends on the implementation of extending classes. It must not
 * depend solely on the {@link Measure}s distance but can also take into account
 * the elements in the given array.
 *
 * <p>
 * Once the distances between each of the given objects are determined,
 * multidimensional scaling (MDS) is used to generate coordinates that preserve
 * these distances as well as possible. To decrease computation, one can use
 * LMDS (landmark MDS). For this slightly less accurate version of MDS only
 * distances from the landmark points to the other points are required instead
 * of the distances between all pairs of objects. The number of landmark points
 * can be adjusted.
 *
 * <p>
 * An {@link Embedder} can also be used to directly test the embedding quality.
 *
 * @author Anton Ballmaier
 *
 * @see MultidimensionalScaling
 * @see CorrelationEvaluation
 * @param <T> the type of objects to be embedded. The used {@link Measure} must
 *            also be applicable for this type.
 */
public abstract class Embedder<T> {

	/**
	 * Minimal landmark count an embedding will be assigned as default, given that
	 * the number of objects to be embedded is not less.
	 */
	public static final int MIN_DEFAULT_LANDMARKS = 5;

	/**
	 * Elements to be embedded
	 */
	protected T[] elements;

	/**
	 * Used to measure distances between the {@link #elements}.
	 */
	protected Measure<T> measure;

	/**
	 * Number of landmarks to be used. Irrelevant as long as {@link #useLandmarks}
	 * is false.
	 */
	private int landmarks;

	/**
	 * LMDS needs random landmark points. Therefore the elements are saved in a
	 * shuffled order. To be able to unshuffle, a random permutation is generated
	 * and stored as array of indices.
	 */
	private final int[] shuffle;

	/**
	 * Indicates whether to use MDS or LMDS.
	 */
	private boolean useLandmarks;

	/**
	 * Constructs a new {@link Embedder} object using the given elements and
	 * {@link Measure}.
	 *
	 * <p>
	 * No landmarks will be used by default. In case this is changed using
	 * <code>useLandmarks(true)</code> the landmark count defaults to the minimum of
	 * <ul>
	 * <li>the maximum of {@link #MIN_DEFAULT_LANDMARKS} and
	 * <code>elements.length</code>
	 * <li>&Sqrt;<code>elements.length</code>
	 * </ul>
	 *
	 * @param elements the elements that are meant to be embedded
	 * @param measure  the measure used for the distance calculations
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public Embedder(T[] elements, Measure<T> measure) {
		if (elements == null || measure == null) {
			throw new IllegalArgumentException("Given arguments must not be null.");
		}

		this.elements = elements.clone();
		this.measure = measure;
		this.landmarks = Math.min(elements.length,
				Math.max(MIN_DEFAULT_LANDMARKS, (int) (2 * Math.sqrt(elements.length))));
		useLandmarks = false;

		// Generate a specific shuffle for the entire life cycle of this object
		shuffle = new int[elements.length];
		for (int i = 0; i < elements.length; i++) {
			shuffle[i] = i + (int) (Math.random() * (elements.length - i));

			final T tmp = this.elements[i];
			this.elements[i] = this.elements[shuffle[i]];
			this.elements[shuffle[i]] = tmp;
		}

	}

	/**
	 * Constructs a new {@link Embedder} object using the given elements,
	 * {@link Measure} and landmark count. Landmarks will be used by default
	 *
	 *
	 * @param elements  the elements that are meant to be embedded
	 * @param measure   the measure used for the distance calculations
	 * @param landmarks the number of landmark points
	 * @throws IllegalArgumentException if any of the arguments is <code>null</code>
	 */
	public Embedder(T[] elements, Measure<T> measure, int landmarks) {
		this(elements, measure);
		this.setLandmarkCount(landmarks);
		this.useLandmarks(true);
	}

	/**
	 * Calculates coordinates in n-dimensional euclidean space that preserve some
	 * distance function between these objects as well as possible. The definition
	 * of the distances to be preserved depends on the implementation of extending
	 * classes.
	 *
	 * <p>
	 * The embedding will be returned so that
	 * <center><code>embed(dimension)[i][j]</code></center> is the j<sup>th</sup>
	 * coordinate of the i<sup>th</sup> embedding vector.
	 *
	 * @param dimension the dimension of the euclidean space the embedding is meant
	 *                  to use. The higher, the more accurate the embedding can be.
	 * @return the coordinates of the embedding. <code>embed(dimension)[i][j]</code>
	 *         is the i<sup>th</sup> coordinate of the embedding of the
	 *         j<sup>th</sup> element.
	 */
	public double[][] embed(int dimension) {
		return unshuffleEmbedding(embedInternalOrder(dimension));
	}

	/**
	 * Calculates the quality of the embedding in n-dimensional eucliden space.
	 *
	 * <p>
	 * The residual variance of the following two matrices is used as the measure
	 * for the quality:
	 * <ul>
	 * <li>the distances returned by {@link #requiredDistances(int)}
	 * <li>the euclidean distances within the embedding
	 * </ul>
	 *
	 * @param dimension the dimension of the eucliden space the embedding is meant
	 *                  to use. Generally, the higher the dimension, the better the
	 *                  quality
	 * @return the quality of the embedding as a value between 0 (perfect embedding)
	 *         and 1 (no correlation between embedding and input distances)
	 * @see CorrelationEvaluation
	 */
	public double embeddingQuality(int dimension) {
		final double[][] embedding = embedInternalOrder(dimension);
		final double correlation = CorrelationEvaluation.embeddingQuality(requiredDistances(getStartingPoints()),
				embedding);
		return correlation;
	}

	/**
	 * Returns the number of landmarks. If landmarks are not used at the moment, the
	 * saved count will be returned anyways.
	 *
	 * @return the number of landmarks used if useLandmarks was set to
	 *         <code>true</code>
	 */
	public int getLandmarkCount() {
		return landmarks;
	}

	/**
	 * Used to set the number of landmarks used. If landmarks are not used at the
	 * moment, the landmark count is saved anyway and would be used after calling
	 * <code>useLandmarks(false)</code>.
	 *
	 * @param landmarks the number of landmarks to use if useLandmarks was set to
	 *                  <code>true</code>
	 * @throws IllegalArgumentException if the given landmark count is greater than
	 *                                  the number of elements or less than 0.
	 */
	public void setLandmarkCount(int landmarks) {
		if (landmarks > this.elements.length) {
			throw new IllegalArgumentException(
					String.format("Cannot have %d landmarks using only %d elements", landmarks, elements.length));
		}
		if (landmarks < 2) {
			throw new IllegalArgumentException("Landmark count must be at least 2.");
		}

		this.landmarks = landmarks;
	}

	/**
	 * Enables or disables using landmarks. If enabling landmark usage, the last set
	 * landmark count is used. For default landmark count see
	 * {@link #Embedder(Object[], Measure)}.
	 *
	 * @param useLandmarks whether to use landmarks
	 */
	public void useLandmarks(boolean useLandmarks) {
		this.useLandmarks = useLandmarks;
	}

	/**
	 * Defines the distance between the starting points and all other elements that
	 * the embedding is supposed to preserve.
	 *
	 * <p>
	 * The first entries of <code>elements</code> must be used as starting points.
	 * The returned distances must be positive. They also must be symmetric, meaning
	 * <center><code>requiredDistances(startingPoints)[i][j]</code>==
	 * <code>requiredDistances(startingPoints)[j][i]</code></center> provided that
	 * <code>i</code>,<code>j</code>&lt;<code>startingPoints</code>.
	 *
	 * @param startingPoints the number of starting points
	 * @return the distances from starting points to all other entries.
	 *         <code>requiredDistances(startingPoints)[i][j]</code> is the distance
	 *         from the j<sup>th</sup> starting point to the i<sup>th</sup> element.
	 */
	protected abstract double[][] requiredDistances(int startingPoints);

	/**
	 * Used to calculate the actual embedding used in {@link #embed(int)}. However,
	 * the returned embedding is sorted in the shuffled order.
	 *
	 * @param dimensions the dimension of the euclidean space the embedding is meant
	 *                   to use. The higher, the more accurate the embedding can be.
	 * @return the coordinates of the embedding. <code>embed(dimension)[i][j]</code>
	 *         is the i<sup>th</sup> coordinate of the embedding of the
	 *         j<sup>th</sup> element.
	 * @throws IllegalArgumentException if <code>dimensions</code> &leq; 0
	 */
	private double[][] embedInternalOrder(int dimensions) {
		if (dimensions <= 0) {
			throw new IllegalArgumentException("Dimension must be at least 1");
		}

		final double[][] distances = requiredDistances(getStartingPoints());
		double[][] embedding;
		if (useLandmarks) {
			embedding = MultidimensionalScaling.landmark(distances, dimensions);
		} else {
			embedding = MultidimensionalScaling.classical(distances, dimensions);
		}
		return embedding;
	}

	/**
	 * Returns the number of points that should be used as starting points (as in
	 * {@link #requiredDistances(int)}.
	 *
	 * <p>
	 * This is simply the number of landmarks if those are used, the number of
	 * elements otherwise.
	 *
	 * @return the number of points that should be used as starting points
	 */
	private int getStartingPoints() {
		int startingPoints = elements.length;
		if (useLandmarks) {
			startingPoints = landmarks;
		}
		return startingPoints;
	}

	/**
	 * Reverses the internally used index shuffling for an embedding. The given
	 * array will be changed.
	 *
	 * @param embedding the embedding that should be unshuffled
	 * @return the unshuffled embedding
	 */
	private double[][] unshuffleEmbedding(double[][] embedding) {

		for (int i = elements.length - 1; i >= 0; i--) {
			for (int d = 0; d < embedding.length; d++) {
				final double tmp = embedding[d][i];
				embedding[d][i] = embedding[d][shuffle[i]];
				embedding[d][shuffle[i]] = tmp;
			}
		}
		return embedding;
	}
}