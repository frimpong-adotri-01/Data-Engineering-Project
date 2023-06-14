package edu.efrei.m1bdml




import org.apache.spark.sql.{SaveMode, SparkSession, functions}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types.{ArrayType, DataTypes, DoubleType, IntegerType, MapType, StringType, StructField, StructType}
import org.apache.spark.streaming.{Minutes, StreamingContext}




object SparkStreaming extends App{

  // Creation of the Spark Session
  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("SparkKafkaStreaming")
    .config("spark.streaming.stopGracefullyOnShutdown", "true")
    .config("spark.sql.shuffle.partitions", 100)
    .getOrCreate()

  val streamingContext = new StreamingContext(spark.sparkContext, Minutes(3))
  spark.sparkContext.setLogLevel("ERROR") //To not show log errors


  spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "AKIAXYZJEEAPINFY5RLO")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key","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")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3.us-east-2.amazonaws.com")

  //call
  import spark.implicits._

  val df = (spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:29092")
    .option("subscribe", "PeaceWatcher_topic")
    .option("startingOffsets", "earliest")
    .load())

  println("Streaming DataFrame : " + df.isStreaming)

  val resultDF = df
    .withColumn("result", col("value") )


  df.printSchema()
  val stringDF = df.selectExpr("CAST(value AS STRING)").as[String]

  val schema = new StructType()
    .add("peaceWatcherId", IntegerType, true)
    .add("currentLocation", ArrayType(DoubleType), true)
    .add("names", MapType(StringType,DoubleType), true)
    .add("keywords", ArrayType(StringType), true)
    .add("timestamp", IntegerType, true)


  val PeaceWatcherDF = stringDF.withColumn("value", from_json(col("value"),schema))
    .select(col("value.*"))

  val query = PeaceWatcherDF.writeStream
    .format("json")
    .outputMode("append")
    .option("path", "s3a://peacestate/donneePeaceWatcher.json")
    .option("checkpointLocation", "./checkpoints")
    .trigger(Trigger.ProcessingTime("180 seconds"))

  query.start()
    .awaitTermination()

  spark.stop()
}