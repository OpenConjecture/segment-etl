name := "segment-etl-pageviews"

version := "0.0.1"

scalaVersion := "2.10.5"

// Snowplow SDK
//resolvers += "Snowplow Analytics" at "http://maven.snplow.com/releases/"
//libraryDependencies += "com.snowplowanalytics" %% "snowplow-scala-analytics-sdk" % "0.1.0"

// Scala Dependencies
//libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.5"
//libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.10.5"

// Apache Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.6.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "1.6.1"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.6.1"

// EMR / Hadoop
libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.7.1"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.7.4"

resolvers += Resolver.mavenLocal

// Packaging merge strategies
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.last
  case PathList("javax", "activation", xs @ _*) => MergeStrategy.last
  case PathList("javax", "xml", xs @ _*) => MergeStrategy.last
  case PathList("org", "apache", xs @ _*) => MergeStrategy.last
  case PathList("com", "google", xs @ _*) => MergeStrategy.last
  case PathList("com", "esotericsoftware", xs @ _*) => MergeStrategy.last
  case PathList("com", "codahale", xs @ _*) => MergeStrategy.last
  case PathList("com", "yammer", xs @ _*) => MergeStrategy.last
  case "about.html" => MergeStrategy.rename
  case "META-INF/ECLIPSEF.RSA" => MergeStrategy.last
  case "META-INF/mailcap" => MergeStrategy.last
  case "META-INF/mimetypes.default" => MergeStrategy.last
  case "plugin.properties" => MergeStrategy.last
  case "log4j.properties" => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
