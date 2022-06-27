package data;

/**
 * A {@link WeatherFeature} defines a feature of weather data.
 *
 * @author Anton Ballmaier
 *
 */
public enum WeatherFeature {
	/**
	 * Relative cloudiness
	 */
	CLOUDS,

	/**
	 * Dew point in &deg;C. This is the temperature to which air must be cooled to
	 * become saturated with water vapor.
	 */
	DEW_POINT,

	/**
	 * Relative humidity
	 */
	HUMIDITY,

	/**
	 * Air pressure in hPa
	 */
	PRESSURE,

	/**
	 * Air temperature 2m above ground in &deg;C
	 */
	TEMPERATURE,

	/**
	 * Wind speed 2m above ground in m/s
	 */
	WIND_SPEED;
}