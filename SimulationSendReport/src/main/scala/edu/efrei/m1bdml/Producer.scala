package edu.efrei.m1bdml

import edu.efrei.m1bdml.RandomData.{RandomReport, serialized}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, IntegerSerializer, StringSerializer}

import java.util.Properties
import java.util.Timer
import java.util.concurrent._


import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


object Producer extends App {

  val topicName = "PeaceWatcher_topic"

  val producerProperties = new Properties()
  producerProperties.setProperty(
    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092"
  )
  producerProperties.setProperty(
    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName
  )

  producerProperties.setProperty(
    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[ByteArraySerializer].getName
  )

  val producer: KafkaProducer[String, Array[Byte]] = new KafkaProducer[String, Array[Byte]](producerProperties)
  val key = "report1"

  while (true) {
    val resTest = RandomReport.randomDrones
    resTest.map(x => producer.send(new ProducerRecord[String, Array[Byte]](topicName, key, serialized(x))))
    println(s"Sent message: $topicName")
    Thread.sleep(1.minute.toMillis) // Send a message every minute
  }

  producer.flush()
  producer.close()
}
