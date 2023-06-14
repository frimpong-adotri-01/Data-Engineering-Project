name := "ProjetPeaceWatcher"
version := "0.1.0-SNAPSHOT"

scalaVersion :="2.12.8"

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "2.3.1"
libraryDependencies += "it.bitbl" %% "scala-faker" % "0.4"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
libraryDependencies += "org.twitter4j" % "twitter4j-core" % "4.0.5"
libraryDependencies += "com.google.code.gson" % "gson" % "2.7"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8"
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.2.0"
libraryDependencies += "it.bitbl" %% "scala-faker" % "0.4"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"
libraryDependencies += "io.circe" %% "circe-core" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-generic" % "0.14.1"
libraryDependencies += "io.circe" %% "circe-parser" % "0.14.1"
libraryDependencies += "org.apache.kafka" % "kafka-streams" % "2.8.0"
libraryDependencies += "org.apache.kafka" %% "kafka-streams-scala" % "2.8.0"

// spark dependancies to move after
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.1.2"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.1.2" % Test