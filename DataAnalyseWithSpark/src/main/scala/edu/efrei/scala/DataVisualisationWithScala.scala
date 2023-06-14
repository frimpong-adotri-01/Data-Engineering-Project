import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Encoders, SparkSession, functions}
import org.apache.spark.sql.types._
import plotly._
import plotly.element._
import plotly.layout._



object DataVisualisationWithScala extends App {
  val spark = SparkSession
    .builder()
    .master("local[*]")
    .appName("SparkAwsConnection")
    .getOrCreate()

  import spark.implicits._


  spark.sparkContext.setLogLevel("ERROR") //To not show log errors


  spark.sparkContext.hadoopConfiguration.set("fs.s3a.access.key", "AKIAXYZJEEAPINFY5RLO")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.secret.key", "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")
  spark.sparkContext.hadoopConfiguration.set("fs.s3a.endpoint", "s3.us-east-2.amazonaws.com")

  val schema = new StructType()
    .add("peaceWatcherId", IntegerType, true)
    .add("currentLocation", ArrayType(DoubleType), true)
    .add("names", MapType(StringType, DoubleType), true)
    .add("keywords", ArrayType(StringType), true)
    .add("timestamp", IntegerType, true)

  val df = spark.read
    .schema(schema)
    .format("json")
    .option("header", "true") // Assuming the data has a header row
    .load("s3a://peacestate/donneePeaceWatcher.json")

  val rowCount = df.count()
  val numColumns: Int = df.columns.length
  println(s"Number of rows in the dataframe: $rowCount")
  println(s"Number of columns: $numColumns")

  println("The schema of the Dataframe : ")
  df.printSchema()

  // Analyse Part

  //Describe df
  val summary = df.describe()
  summary.show()

  //Analyse 1
  val resultAlerts = df
    .withColumn("CountAlerts", expr("size(filter(map_values(Names), x -> x < 0))"))
    .withColumn("CountNoAlerts", expr("size(filter(map_values(Names), x -> x >= 0))"))
    .withColumn("datetime", to_timestamp(from_unixtime(col("timestamp"),"MM-dd-yyyy HH:mm:ss"), "MM-dd-yyyy HH:mm:ss").as("datetime"))

  val countsAlerts = resultAlerts.agg(
    sum("CountAlerts")
      .as("SumAlerts"),
    sum("CountNoAlerts")
      .as("SumNoAlerts"))
    .first

  println("Total Number of Alerts : ", countsAlerts.getAs[Long]("SumAlerts"))
  println("Total Number of Non Alerts : ", countsAlerts.getAs[Long]("SumNoAlerts"))

  //Analyse 2
  val resultLatLon = resultAlerts.filter(resultAlerts("CountAlerts") > 0)
    .select(
      mean(col("currentLocation")(0)).as("MeanLatitude"),
      mean(col("currentLocation")(1)).as("MeanLongitude")
    ).first()

  println("Mean of Latitude : ", resultLatLon.getAs[Double]("MeanLatitude"))
  println("Mean of Longitude : ", resultLatLon.getAs[Double]("MeanLongitude"))

  //Analyse 3
  val resultPersonMostAgitated = df.select(explode(col("names")).as(Seq("name", "score")))
    .groupBy("name")
    .agg(min("score").as("minScore"))
    .orderBy(asc("minScore"))
    .limit(10) // select top 10 persons
    .collect() // The result is an array

  println("-------------------------------------------------------------------------------\n")
  println("Top 10 persons who are the most agitated:")
  resultPersonMostAgitated.foreach { person =>
    val personName = person.getAs[String]("name")
    val personScore = person.getAs[Double]("minScore")
    println(s"Name: $personName, Score: $personScore")
  }
  println("-------------------------------------------------------------------------------\n")


  //Partie Graph
  val xData = Seq("Alerts", "No-alerts")
  val yData = Seq(countsAlerts.getAs[Long]("SumAlerts"),
    countsAlerts.getAs[Long]("SumNoAlerts") )

  val data = Seq(Bar(xData, yData))
  val layout = Layout(title= "Alerts VS No-Alerts")

  Plotly.plot("NumberAlerts.html", data, layout)

  //Graph 2
  val scores = df.select(explode(map_values(col("names")))
    .as("value"))
    .select(col("value").cast(DoubleType))
    .as[Double](Encoders.scalaDouble)
    .collect()
    .toList


  val dataScores = Seq(Histogram(scores, xbins = Bins(-1, 1, 0.05)))

  val layoutDensity = Layout(title = "Density scores")

  Plotly.plot("DensityScores.html", dataScores, layoutDensity)

  //Analyse of Alertes et non-Alertes per Month
  println("Number of Alertes and non-Alertes per Month")
  val dfMonth = resultAlerts
    .withColumn("month", month(to_timestamp(col("datetime"), "MM-dd-yyyy HH:mm:ss")))
    .groupBy("month")
    .sum("CountAlerts", "CountNoAlerts").sort("month")

  dfMonth.show()

  val datemonth = dfMonth.select("month").as[String].collect().toList
  val CountAlerteBarmonth = dfMonth.select("sum(CountAlerts)").as[Long].collect().toList
  val CountNonAlerteBarmonth = dfMonth.select("sum(CountNoAlerts)").as[Long].collect().toList

  //Plotly Graph for the proportion of Alerts and Non Alerts reports per Month
  val tracemonth1 = Bar(datemonth,
    CountAlerteBarmonth,
    name = "Alerts"
  )

  val tracemonth2 = Bar(
    datemonth,
    CountNonAlerteBarmonth,
    name = "Non Alerts"
  )

  val datamonth2 = Seq(tracemonth1, tracemonth2)

  val layoutmonth2 = Layout(
    barmode = BarMode.Group,
    title = "Alerts VS Non-Alerts in terms of day of month",
    xaxis = Axis(title = "month"),
    yaxis = Axis(title = "Number of Alerts")
  )

  Plotly.plot("AlertVsNonALtersPerMonth.html", datamonth2, layoutmonth2)

  //Analyse of Alertes et non-Alertes per Day
  println("Number of Alertes and non-Alertes per Day")
  val dfDay = resultAlerts
    .withColumn("dayofmonth", dayofmonth(to_timestamp(col("datetime"), "MM-dd-yyyy HH:mm:ss")))
    .groupBy("dayofmonth")
    .sum("CountAlerts", "CountNoAlerts").sort("dayofmonth")

  dfDay.show()

  //Plotly Graph for the proportion of Alerts and Non Alerts reports

  val date = dfDay.select("dayofmonth").as[String].collect().toList
  val CountAlerteBar = dfDay.select("sum(CountAlerts)").as[Long].collect().toList
  val CountNonAlerteBar = dfDay.select("sum(CountNoAlerts)").as[Long].collect().toList


  val trace1 = Bar(date,
    CountAlerteBar ,
    name = "Alerts"
  )

  val trace2 = Bar(
    date,
    CountNonAlerteBar,
    name = "Non Alters"
  )

  val data2 = Seq(trace1, trace2)

  val layout2 = Layout(
    barmode = BarMode.Group,
    title = "Number in terms of day of month",
    xaxis = Axis(title = "DayOfMonth"),
    yaxis = Axis(title = "Number of Alerts")
  )

  Plotly.plot("AlertVsNonALtersPerDayOfMonth.html", data2, layout2)


  //Analyse of Alertes et non-Alertes per hour
  println("Number of Alertes and non-Alertes per Hour")
  val dfHour = resultAlerts
    .withColumn("hour", hour(to_timestamp(col("datetime"), "MM-dd-yyyy HH:mm:ss")))
    .groupBy("hour")
    .sum("CountAlerts", "CountNoAlerts").sort("hour")

  dfHour.show()

  //Plotly Graph for the proportion of Alerts and Non Alerts reports per Hour
  val datehour = dfHour.select("hour").as[String].collect().toList
  val CountAlerteBarhour = dfHour.select("sum(CountAlerts)").as[Long].collect().toList
  val CountNonAlerteBarhour = dfHour.select("sum(CountNoAlerts)").as[Long].collect().toList


  val tracehour1 = Bar(datehour,
    CountAlerteBarhour,
    name = "Alerts"
  )

  val tracehour2 = Bar(
    datehour,
    CountNonAlerteBarhour,
    name = "Non Alerts"
  )

  val datahour2 = Seq(tracehour1, tracehour2)

  val layouthour2 = Layout(
    barmode = BarMode.Group,
    title = "Alerts VS Non-alerts in term of hour",
    xaxis = Axis(title = "Hour"),
    yaxis = Axis(title = "Number of Alerts")
  )

  Plotly.plot("AlertVsNonALtersPerHour.html", datahour2, layouthour2)

  //Word count of all reports
  println("Word count in all reports")
  val words = resultAlerts
    .select(explode(col("keywords")).as("word"))

  val wordcount = words.groupBy("word")
    .count()
    .sort(col("count").desc)

  wordcount.show(50)

  //Graphe for word count in reports
  val word = wordcount.select("word").as[String].collect().toList
  val countOcc = wordcount.select("count").as[Long].collect().toList
  val tracew = Scatter(
    word,
    countOcc
  )


  val dataw = Seq(tracew)
  val layoutw = Layout(title = "Occurence of word", xaxis = Axis(title = "word"), yaxis = Axis(title = "occurence"))

  Plotly.plot("wordcount.html", dataw, layoutw)

  //Word count of report containing Alertes
  println("Word count of report containing Alertes")
  val wordsAlerte = resultAlerts.filter(resultAlerts("CountAlerts") > 0).select(explode(col("keywords")).as("word"))

  val wordcountAlert = wordsAlerte.groupBy("word")
    .count()
    .sort(col("count").desc)

  wordcountAlert.show(50)

  //Graphe for word count in Alert
  val worda = wordcountAlert.select("word").as[String].collect().toList
  val countOcca = wordcountAlert.select("count").as[Long].collect().toList
  val tracewa = Scatter(
    worda,
    countOcca
  )


  val datawa = Seq(tracewa)
  val layoutwa = Layout(title = "Occurence of word in alert", xaxis = Axis(title = "word"), yaxis = Axis(title = "occurence"))

  Plotly.plot("wordcountAlert.html", datawa, layoutwa)
}
