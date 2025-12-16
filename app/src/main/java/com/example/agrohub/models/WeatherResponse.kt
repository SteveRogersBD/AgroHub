package com.example.agrohub.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class WeatherResponse(
    val location: Location? = null,
    val current: Current? = null,
    val forecast: Forecast? = null,
    val alerts: Alerts? = null
) : Parcelable

@Parcelize
data class AirQuality(
    val co: Double? = null,
    val no2: Double? = null,
    val o3: Double? = null,
    val so2: Double? = null,
    @SerializedName("pm2_5") val pm25: Double? = null,
    val pm10: Double? = null,
    @SerializedName("us-epa-index") val usEpaIndex: Int? = null,
    @SerializedName("gb-defra-index") val gbDefraIndex: Int? = null
) : Parcelable

@Parcelize
data class Alerts(
    val alert: @RawValue List<Map<String, Any>>? = null
) : Parcelable

@Parcelize
data class Astro(
    val sunrise: String? = null,
    val sunset: String? = null,
    val moonrise: String? = null,
    val moonset: String? = null,
    @SerializedName("moon_phase") val moonPhase: String? = null,
    @SerializedName("moon_illumination") val moonIllumination: Int? = null,
    @SerializedName("is_moon_up") val isMoonUp: Int? = null,
    @SerializedName("is_sun_up") val isSunUp: Int? = null
) : Parcelable

@Parcelize
data class Condition(
    val text: String? = null,
    val icon: String? = null,
    val code: Int? = null
) : Parcelable

@Parcelize
data class Current(
    @SerializedName("temp_c") val tempC: Double? = null,
    @SerializedName("is_day") val isDay: Int? = null,
    val condition: Condition? = null,
    @SerializedName("wind_kph") val windKph: Double? = null,
    @SerializedName("wind_degree") val windDegree: Int? = null,
    @SerializedName("pressure_mb") val pressureMb: Double? = null,
    @SerializedName("precip_mm") val precipMm: Double? = null,
    val humidity: Int? = null,
    val cloud: Int? = null,
    @SerializedName("feelslike_c") val feelslikeC: Double? = null,
    @SerializedName("windchill_c") val windchillC: Double? = null,
    @SerializedName("heatindex_c") val heatindexC: Double? = null,
    @SerializedName("dewpoint_c") val dewpointC: Double? = null,
    @SerializedName("vis_km") val visKm: Double? = null,
    val uv: Double? = null,
    @SerializedName("gust_kph") val gustKph: Double? = null,
    @SerializedName("air_quality") val airQuality: AirQuality? = null
) : Parcelable

@Parcelize
data class Day(
    @SerializedName("maxtemp_c") val maxTempC: Double? = null,
    @SerializedName("mintemp_c") val minTempC: Double? = null,
    @SerializedName("avgtemp_c") val avgTempC: Double? = null,
    @SerializedName("maxwind_kph") val maxWindKph: Double? = null,
    @SerializedName("totalprecip_mm") val totalPrecipMm: Double? = null,
    @SerializedName("totalsnow_cm") val totalSnowCm: Double? = null,
    @SerializedName("avgvis_km") val avgVisKm: Double? = null,
    @SerializedName("avghumidity") val avgHumidity: Int? = null,
    @SerializedName("daily_will_it_rain") val dailyWillItRain: Int? = null,
    @SerializedName("daily_chance_of_rain") val dailyChanceOfRain: Int? = null,
    @SerializedName("daily_will_it_snow") val dailyWillItSnow: Int? = null,
    @SerializedName("daily_chance_of_snow") val dailyChanceOfSnow: Int? = null,
    val condition: Condition? = null,
    val uv: Double? = null
) : Parcelable

@Parcelize
data class Forecast(
    val forecastday: List<ForecastDay>? = null
) : Parcelable

@Parcelize
data class ForecastDay(
    val date: String? = null,
    val day: Day? = null,
    val astro: Astro? = null,
    val hour: List<Hour>? = null
) : Parcelable

@Parcelize
data class Hour(
    @SerializedName("time_epoch") val timeEpoch: Int? = null,
    val time: String? = null,
    @SerializedName("temp_c") val tempC: Double? = null,
    @SerializedName("temp_f") val tempF: Double? = null,
    @SerializedName("is_day") val isDay: Int? = null,
    val condition: Condition? = null,
    @SerializedName("wind_mph") val windMph: Double? = null,
    @SerializedName("wind_kph") val windKph: Double? = null,
    @SerializedName("wind_degree") val windDegree: Int? = null,
    @SerializedName("wind_dir") val windDir: String? = null,
    @SerializedName("pressure_mb") val pressureMb: Double? = null,
    @SerializedName("pressure_in") val pressureIn: Double? = null,
    @SerializedName("precip_mm") val precipMm: Double? = null,
    @SerializedName("precip_in") val precipIn: Double? = null,
    @SerializedName("snow_cm") val snowCm: Double? = null,
    val humidity: Int? = null,
    val cloud: Int? = null,
    @SerializedName("feelslike_c") val feelslikeC: Double? = null,
    @SerializedName("feelslike_f") val feelslikeF: Double? = null,
    @SerializedName("windchill_c") val windchillC: Double? = null,
    @SerializedName("windchill_f") val windchillF: Double? = null,
    @SerializedName("heatindex_c") val heatindexC: Double? = null,
    @SerializedName("heatindex_f") val heatindexF: Double? = null,
    @SerializedName("dewpoint_c") val dewpointC: Double? = null,
    @SerializedName("dewpoint_f") val dewpointF: Double? = null,
    @SerializedName("will_it_rain") val willItRain: Int? = null,
    @SerializedName("chance_of_rain") val chanceOfRain: Int? = null,
    @SerializedName("will_it_snow") val willItSnow: Int? = null,
    @SerializedName("chance_of_snow") val chanceOfSnow: Int? = null,
    @SerializedName("vis_km") val visKm: Double? = null,
    @SerializedName("vis_miles") val visMiles: Double? = null,
    @SerializedName("gust_mph") val gustMph: Double? = null,
    @SerializedName("gust_kph") val gustKph: Double? = null,
    val uv: Double? = null
) : Parcelable

@Parcelize
data class Location(
    val name: String? = null,
    val region: String? = null,
    val country: String? = null,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerializedName("tz_id") val tzId: String? = null,
    @SerializedName("localtime_epoch") val localtimeEpoch: Int? = null,
    val localtime: String? = null
) : Parcelable
