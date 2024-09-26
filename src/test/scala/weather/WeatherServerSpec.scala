package weather

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.http4s._
import org.http4s.implicits._
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import io.circe.parser._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class WeatherServerSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers {

  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  "WeatherServer" - {
    "classifyTemperature" - {
      "should classify temperatures correctly" in {
        WeatherServer.classifyTemperature(50) shouldBe "cold"
        WeatherServer.classifyTemperature(70) shouldBe "moderate"
        WeatherServer.classifyTemperature(90) shouldBe "hot"
      }
    }

    "getWeather" - {
      "should return weather response for valid coordinates" in {
        val result = WeatherServer.getWeather(37.7749, -122.4194)
        result.asserting { response =>
          response.forecast should not be empty
          Set("cold", "moderate", "hot") should contain(response.temperatureType)
        }
      }
    }

    "weatherService" - {
      "should handle valid requests" in {
        val request = Request[IO](Method.GET, uri"/weather?latitude=37.7749&longitude=-122.4194")
        val response = WeatherServer.weatherService.orNotFound.run(request)
        
        response.flatMap { res =>
          res.status shouldBe Status.Ok
          res.as[String].map { body =>
            val json = parse(body).getOrElse(fail("Failed to parse response body"))
            json.hcursor.get[String]("forecast").toOption should not be empty
            Set("cold", "moderate", "hot") should contain(json.hcursor.get[String]("temperatureType").getOrElse(""))
          }
        }
      }

      "should return 400 Bad Request for invalid coordinates" in {
        val request = Request[IO](Method.GET, uri"/weather?latitude=100&longitude=-200")
        val response = WeatherServer.weatherService.orNotFound.run(request)
        
        response.flatMap { res =>
          res.status shouldBe Status.BadRequest
          res.as[String].map { body =>
            body should include("Invalid coordinates")
          }
        }
      }

      "should return 400 Bad Request for missing coordinates" in {
        val request = Request[IO](Method.GET, uri"/weather")
        val response = WeatherServer.weatherService.orNotFound.run(request)
        
        response.flatMap { res =>
          res.status shouldBe Status.BadRequest
          res.as[String].map { body =>
            body should include("Missing or invalid latitude and/or longitude parameters")
          }
        }
      }
    }
  }
}