# Weather Service

## Overview

This is a sample HTTP Service, written in [Typelevel Scala](https://typelevel.org/), which contains an endpoint that adheres to the following requirements:

1. Accepts latitude and longitude coordinates.
2. Returns the short forecast for that area for Today (“Partly Cloudy” etc).
3. Categorizes whether the temperature is “hot”, “cold”, or “moderate”. 
4. Uses the [National Weather Service API Web Service](https://www.weather.gov/documentation/services-web-api) as a data source. 

## Technical Requirements

Local / target machine should have the following software installed:

* `jdk 21` (should work on `jdk11` or later)
* `sbt 1.10.2`

## Build / Testing Instructions

```
sbt compile
sbt test
```
### Hit Endpoint w/ External HTTP Client

To test with an external HTTP client (e.g. curl, Postman, web browser, etc):

`sbt run`

### curl

From command line:

`curl -X GET http://localhost:8080/weather?latitude=37.7749&longitude=-122.4194`

### Postman or Web Browser:

Paste the following:

http://localhost:8080/weather?latitude=37.7749&longitude=-122.4194

Successful result for those particular coordinates should yield:

`{"forecast":"Mostly Cloudy","temperatureType":"cold"}`

Note: these specific latitude (`37.7749`) & longitude (`-122.4194`) coordinates are specifically for San Francisco, California.