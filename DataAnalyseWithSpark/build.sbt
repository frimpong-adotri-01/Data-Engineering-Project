name := "DataAnalyseWithSpark"
version := "0.1.0-SNAPSHOT"

scalaVersion :="2.12.8"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.3.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8"



libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.8.1"


libraryDependencies += "org.apache.hadoop" % "hadoop-aws" % "2.8.1"

libraryDependencies += "org.plotly-scala" %% "plotly-render" % "0.8.2"