package edu.efrei.m1bdml
import edu.efrei.m1bdml.RandomData.RandomLocation
import faker._
import play.api.libs.json._

import scala.collection.mutable.ArrayBuffer
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder}
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.KStream


object RandomData extends App {

  object RandomId {
    def random: Int = {
      // Returns a pseudorandom, uniformly distributed int value
      // between 0 (inclusive) and the specified value (exclusive)
      scala.util.Random.nextInt(10)
    }
  }

  object RandomLocation {
    def random = {
      val minLat = 38
      val maxLat = 40
      val minLon = -75
      val maxLon = -78
      Array(minLat + (maxLat - minLat) * scala.util.Random.nextDouble(),
        minLon + (maxLon - minLon) * scala.util.Random.nextDouble())
    }
  }

  object RandomName {
    def random = {
      val randomNumber = scala.util.Random.nextDouble()
      val value = if (randomNumber < 0.25) {
        scala.util.Random.nextDouble() * -1.0 // Generate a random value between 0 and -1
      } else {
        scala.util.Random.nextDouble() // Generate a random value between 0 and 1
      }
      val name = Internet.user_name
      ( name, value)
    }
  }

  object RandomWords {
    def random = {
      Lorem.words(1+scala.util.Random.nextInt(10)).toArray
    }
  }

  object RandomTimestamp {
    def random = {
      val r = scala.util.Random
      1672527600 + r.nextInt(( 1685397562 - 1672527600) + 1)
    }
  }

  object RandomReport{
    def random: Report = {
      val id = RandomId.random
      val location = RandomLocation.random
      val names = Map(RandomName.random, RandomName.random)
      val keywords = RandomWords.random
      val timestamp = RandomTimestamp.random
      Report(id, location, names, keywords, timestamp)
    }

    def randomDrones: List[Report] = {
      val report1 = RandomReport.random.copy(peaceWatcherId = 1)
      val report2 = RandomReport.random.copy(peaceWatcherId = 2)
      val report3 = RandomReport.random.copy(peaceWatcherId = 3)
      val report4 = RandomReport.random.copy(peaceWatcherId = 4)
      val report5 = RandomReport.random.copy(peaceWatcherId = 5)
      val report6 = RandomReport.random.copy(peaceWatcherId = 6)
      val report7 = RandomReport.random.copy(peaceWatcherId = 7)
      val report8 = RandomReport.random.copy(peaceWatcherId = 8)
      val report9 = RandomReport.random.copy(peaceWatcherId = 9)
      val report10 = RandomReport.random.copy(peaceWatcherId = 10)
      List(report1, report2, report3, report4, report5, report6, report7, report8, report9,
        report10)
    }
  }

  def serialized(report : Report) =
    report.asJson.noSpaces.getBytes()

  def deserialized(serialized : Array[Byte]) = {
    val str = new String(serialized)
    decode[Report](str).toOption
  }
}
