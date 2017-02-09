name := "pltm-analysis"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  // other resolvers here
  // if you want to use snapshot builds (currently 0.12-SNAPSHOT), use this.
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)

libraryDependencies ++= Seq (
  // https://mvnrepository.com/artifact/org.apache.commons/commons-math3
  "org.apache.commons" % "commons-math3" % "3.6.1",
  "commons-cli" % "commons-cli" % "1.3.1",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.scalactic" %% "scalactic" % "2.2.6" % "test",
  "org.scalaz" %% "scalaz-core" % "7.2.1",
  "org.apache.commons" % "commons-csv" % "1.2",
  "commons-io" % "commons-io" % "2.4",
  "org.slf4j" % "slf4j-simple" % "1.7.21",
  "com.google.protobuf" % "protobuf-java" % "2.6.1",
  "org.rogach" %% "scallop" % "2.0.0",
  "colt" % "colt" % "1.2.0",
  "nz.ac.waikato.cms.weka" % "weka-stable" % "3.6.13"
)