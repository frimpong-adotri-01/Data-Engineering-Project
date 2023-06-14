name := "StreamProcessingWithSpark"
version := "0.1.0-SNAPSHOT"

scalaVersion :="2.12.8"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.3.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.2.0"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.8.0"
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0"

// spark dependancies to move after
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.2"
libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.8.1"


libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.8.1"
