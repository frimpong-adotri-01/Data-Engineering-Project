package edu.efrei.m1bdml

import scala.collection.mutable.ArrayBuffer

final case class Report(
                         peaceWatcherId: Int,
                         currentLocation: Array[Double],
                         names: Map[String, Double],
                         keywords: Array[String],
                         timestamp: Int
                       )

