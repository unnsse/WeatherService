package weather

import cats.effect._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.ember.server._
import org.http4s.circe.CirceEntityCodec._
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.Printer
import sttp.client3._
import sttp.client3.circe._
import sttp.model.Uri
import com.comcast.ip4s._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object WeatherServer extends IOApp {
  // Initialize logger
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  // Define case classes for the API response structure
  case class Properties(forecast: String)
  case class PointsResponse(properties: Properties)
  case class Period(shortForecast: String, temperature: Double)
  case class ForecastProperties(periods: List[Period])
  case class ForecastResponse(properties: ForecastProperties)
  case class WeatherResponse(forecast: String, temperatureType: String)

  // Classify temperature into cold, moderate, or hot
  def classifyTemperature(temp: Double): String = {
    if (temp < 60) "cold"
    else if (temp > 80) "hot"
    else "moderate"
  }

  // Function to fetch the weather based on latitude and longitude
  def getWeather(latitude: Double, longitude: Double): IO[WeatherResponse] = {
    val backend = HttpURLConnectionBackend()
    
    // Step 1: Get the forecast URL from the NWS points API
    val pointsUrl = Uri.unsafeParse(s"https://api.weather.gov/points/$latitude,$longitude")
    val pointsRequest = basicRequest
      .get(pointsUrl)
      .response(asJson[PointsResponse])

    for {
      pointsResponse <- IO.fromEither(pointsRequest.send(backend).body).onError(e => Logger[IO].error(e)("Failed to fetch points data"))
      _ <- Logger[IO].info(s"Received points response for coordinates ($latitude, $longitude)")
      forecastUrl = pointsResponse.properties.forecast
      
      // Step 2: Use the forecast URL to get weather data
      forecastUri = Uri.unsafeParse(forecastUrl)
      forecastRequest = basicRequest
        .get(forecastUri)
        .response(asJson[ForecastResponse])

      forecastResponse <- IO.fromEither(forecastRequest.send(backend).body).onError(e => Logger[IO].error(e)("Failed to fetch forecast data"))
      _ <- Logger[IO].info("Received forecast response")
      
      // Log the forecastResponse as JSON
      jsonPrinter = Printer.spaces2.copy(dropNullValues = true)
      jsonString = jsonPrinter.print(forecastResponse.asJson)
      _ <- Logger[IO].info(s"Forecast Response: $jsonString")

      todayForecast = forecastResponse.properties.periods.head.shortForecast
      temperature = forecastResponse.properties.periods.head.temperature
      weatherResponse = WeatherResponse(todayForecast, classifyTemperature(temperature))
      _ <- Logger[IO].info(s"Processed weather response: $weatherResponse")
    } yield weatherResponse
  }

  // Define the weather service route
 val weatherService = HttpRoutes.of[IO] {
    case GET -> Root / "weather" :? LatitudeQueryParamMatcher(maybeLat) +& LongitudeQueryParamMatcher(maybeLon) =>
      (maybeLat, maybeLon) match {
        case (Some(lat), Some(lon)) if isValidCoordinate(lat, lon) =>
          getWeather(lat, lon).flatMap { weather =>
            Ok(weather.asJson)
          }
        case (Some(_), Some(_)) =>
          BadRequest("Invalid coordinates. Latitude must be between -90 and 90, and longitude must be between -180 and 180.")
        case _ =>
          BadRequest("Missing or invalid latitude and/or longitude parameters.")
      }
  }

  // Matcher for latitude query param
  object LatitudeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Double]("latitude")
  
  // Matcher for longitude query param
  object LongitudeQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Double]("longitude")

  // Validate coordinate range
  def isValidCoordinate(lat: Double, lon: Double): Boolean = {
    lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180
  }

  // Build and run the server
  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info("Starting Weather Server")
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(Host.fromString("0.0.0.0").get)
        .withPort(Port.fromInt(8080).get)
        .withHttpApp(weatherService.orNotFound)
        .build
        .useForever
    } yield ExitCode.Success
  }
}