package analytics

// Spark Imports
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.SparkContext._

// Imports
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.functions.from_utc_timestamp
import org.apache.spark.sql.SchemaRDD
import scala.util.Try
import org.joda.time.{DateTime, Period}

object ETL extends App {
  // Spark App Config
  val conf = new SparkConf().setAppName("Segment | ETL | Pageviews")
  val sc = new SparkContext(conf)

  // Setup Spark SQL context
  val sqlContext = new org.apache.spark.sql.SQLContext(sc)
  import sqlContext._
  import sqlContext.implicits._

  // S3 Hadoop credentials
  sc.hadoopConfiguration.set("fs.s3n.awsAccessKeyId", "...")
  sc.hadoopConfiguration.set("fs.s3n.awsSecretAccessKey", "...")

  /*
   *  Schema: Pageview
   *  ========================
   *  Pageviews track a user's movement from one URL (or intra-page interactivity).
   */
  import org.apache.spark.sql.types.StructType

  // Property schema
  val properties_schema = (new StructType).add("path", "string")
                                         .add("referrer", "string")
                                         .add("search", "string")
                                         .add("title", "string")
                                         .add("url", "string")

  // Create overall pageview schema
  val pageview_schema = (new StructType).add("userId", "string")
                                        .add("anonymousId", "string")
                                        .add("timestamp", "string")
                                        .add("type", "string")
                                        .add("messageId", "string")
                                        .add("properties", properties_schema)

  def loadData(start_date: String, end_date: String): org.apache.spark.sql.DataFrame = {
    // Grab and parse raw snowplow data
    val logs = sqlContext.read.schema(pageview_schema).json("../logs/segment-logs/<segment_s3_folder>/*/*")
    val pageviews = logs.filter("type = 'page'").filter("timestamp between '" + start_date + "' and '" + end_date + "'")

    // Return raw pageviews
    return pageviews
  }

  /*
        Pageview Extraction (Parrallel Implementation)
        -----------
   */

  // Pageview class
  case class Pageview(userid: String,
                      sessionid: String,
                      urlpath: String,
                      date: String,
                      time: String)

  // Grab all relevant values and group by session
  def parsePageviews(pageviews: org.apache.spark.sql.DataFrame): org.apache.spark.sql.DataFrame = {
    return pageviews.rdd.map(r => {
      // Get properties
      val properties = r(5).toString().replace("[","").replace("]","").split(",")
      val userId = if(r(0) != null) r(0).toString else ""

      // Datetime
      val date = r(2).toString().substring(0, 10)
      val time = r(2).toString().substring(11)

      // Marshal in pageview class
      Pageview(userId, r(1).toString(), properties(0), date, time)
    }).toDF()
  }

  // Master load function
  def loadParseSave(start_date: String, stop_date: String, register_table: Boolean) = {
    // Load data
    val pageviews = loadData(start_date, stop_date)

    // Get sql compliant date
    val sql_event_date = start_date.replace("-", "_")

    // Save to disk
    //parsePageviews(pageviews).write.parquet("s3n://dw-data/etl/pageviews/" + "/")
    parsePageviews(pageviews).coalesce(100).write.parquet("../etl/pageviews/" + sql_event_date + "/")

    if(register_table) {
      // Analyze in Spark SQL
      val parsedPageviews = parsePageviews(pageviews)
      parsedPageviews.registerTempTable("pageviews")
    }
  }

  // Helper - creates and returns a date range list
  def dateRange(from: DateTime, to: DateTime, step: Period): Iterator[DateTime] = Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to))

  // Run master load function over specified date range
  def loadDateRange(date_start: String, date_end: String) = {
    // Get date range
    val date_range = dateRange(new DateTime(date_start), new DateTime(date_end), new Period().withDays(1))

    // Iterate over date range and call master loadParseSave
    date_range.toList.foreach(dt => {
      // Get start date
      val start_date = dt.toString.substring(0, 10)

      // Get end date
      val end_date = dt.plusDays(1).toString.substring(0, 10)

      // Call load-parse function
      loadParseSave(start_date, end_date, false)
    })
  }

  /*
   *  Choose Run Path
   *  ---------------
   *  1) Multi-date Run -- 2 Arguments
   *    [a] start date
   *    [b] end date
   *  2) Single-date Run -- 1 Arguments
   *    [a] date
   */
  if(args.length == 2) {
    // Call multi-date range function
    loadDateRange(args(0), args(1))
  } else if(args.length == 1) {
    // Call single date range
    loadParseSave(args(0), false)
  }
}
