package edu.efrei.m1bdml

import edu.efrei.m1bdml.RandomData.{RandomReport, deserialized}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, IntegerDeserializer, StringDeserializer}

import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, IntegerDeserializer, StringDeserializer, StringSerializer}
import java.time.Duration
import scala.collection.JavaConverters._
import java.util.Properties
import java.time._
import java.time.format.DateTimeFormatter


object AlertManager extends App{

  val topicName = "PeaceWatcher_topic"
  val alertTopicName = "alerts"

  val consumerProperties = new Properties()
  consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092")
  consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "group-id-1")
  consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
  consumerProperties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
  consumerProperties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[ByteArrayDeserializer].getName)
  consumerProperties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

  val consumer = new KafkaConsumer[String, Array[Byte]](consumerProperties)
  consumer.subscribe(List(topicName).asJava)


  val producerProperties = new Properties()
  producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092")
  producerProperties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
  producerProperties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

  val producer = new KafkaProducer[String, String](producerProperties)

  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  println("| Key | Message | Partition | Offset |")
  while (true) {
    val polledRecords = consumer.poll(Duration.ofSeconds(1))
    if (!polledRecords.isEmpty) {
      val recordIterator = polledRecords.iterator()
      while (recordIterator.hasNext) {
        val record = recordIterator.next()
        val report = deserialized(record.value())
        report.map { report =>
          val containsNegative = report.names.exists(_._2 < 0) // Vérification que la liste contient au moins une valeur inférieure à 0
          if (containsNegative) {
            val timestamp = report.timestamp
            val dateTime = LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
            val formattedDateTime = dateTime.format(formatter)
            val alertMessage =
              s"""||ALERT ID| : ${record.offset()} \n
                  |PeaceWatcher ID : ${report.peaceWatcherId} \n
                  |Location: ${report.currentLocation.toList} \n
                  |People : ${report.names.toList} \n
                  |Keywords: ${report.keywords.toList} \n
                  |Timestamp: ${formattedDateTime}""".stripMargin
            val alertRecord = new ProducerRecord[String, String](alertTopicName, alertMessage)
            producer.send(alertRecord)
            println(alertMessage)
          }
        }
      }
    }
  }
}



