# Segment | ETL | Pageviews
Project for parsing exported ETL data into general use pageviews.

This JAR, once run, puts data into the data/etl/pageviews/{date}/ directory in S3.

## Quick Setup

 1. Install [SBT](http://www.scala-sbt.org/)
 1. Clone this project
 1. Run `sbt` (This will also download all dependencies)
 1. In the sbt console you can:
  * `run` - runs the greeter.Hello application
  * `test` - runs the GreetingsSpec tests
  * `console` - run the REPL with your project environment loaded
  * `compile` - compiles. :)
  * `clean` - cleans the generated file from compiling
