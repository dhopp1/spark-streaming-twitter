scalaVersion := "2.11.12"
name := "spark-streaming-twitter"
organization := "dhopp1"
version := "1.0"
logLevel in run := Level.Error
onLoadMessage := ""

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming-twitter" % "1.6.1",
  "org.twitter4j" % "twitter4j-core" % "3.0.6",
  "org.apache.spark" %% "spark-streaming" % "1.6.1",
  "org.apache.spark" %% "spark-core" % "1.6.1"
)

assemblyMergeStrategy in assembly := {
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard
 case x => MergeStrategy.first
}