# PeaceState Data Engineering Project

---

<image src="peace-state.jpg">

---
 
 ![Deploy badge](https://github.com/suveta/DataEngineeringProject/actions/workflows/docker-image.yml/badge.svg?event=push)                    [<img src="https://img.shields.io/badge/Scala-2.12.8-red.svg?logo=scala">]([https://hub.docker.com/r/hadolint/hadolint](https://www.scala-lang.org))                 [<img src="https://img.shields.io/badge/Apache Spark-3.1.2-red.svg?logo=apachespark">]([https://spark.apache.org](https://spark.apache.org))   [<img src="https://img.shields.io/badge/dockerhub registry-frimpongefrei/peacewatcher--alerts--manager--microservice:v1.0.0-blue.svg?logo=docsdotrs">]([https://hub.docker.com/r/frimpongefrei/peacewatcher-alerts-manager-microservice](https://hub.docker.com/r/frimpongefrei/peacewatcher-alerts-manager-microservice))                                [<img src="https://img.shields.io/badge/dockerhub registry-frimpongefrei/peacewatchers--dataviz--micro--service:v1.0.0-blue.svg?logo=docsdotrs">]([https://hub.docker.com/r/frimpongefrei/peacewatchers-dataviz-micro-service](https://hub.docker.com/r/frimpongefrei/peacewatchers-dataviz-micro-service))      [<img src="https://img.shields.io/badge/Apache Kafka-confluentinc/cp--kafka-green.svg?logo=apachekafka">]([https://hub.docker.com/r/confluentinc/cp-kafka/](https://hub.docker.com/r/confluentinc/cp-kafka/))         [<img src="https://img.shields.io/badge/Docker Compose-version 3-important.svg?logo=docker">]([https://docs.docker.com/compose/]([https://www.scala-lang.org](https://docs.docker.com/compose/)))                     [<img src="https://img.shields.io/badge/Streamlit-1.22.0-9cf.svg?logo=streamlit">]([https://pypi.org/project/streamlit/]([https://pypi.org/project/streamlit/))             [<img src="https://img.shields.io/badge/Plotly-5.14.1-blue.svg?logo=plotly">]([https://pypi.org/project/plotly/](https://pypi.org/project/plotly/))             [<img src="https://img.shields.io/badge/AWS-orange.svg?logo=amazonaws">]([https://pypi.org/project/plotly/](https://pypi.org/project/plotly/))          [<img src="https://img.shields.io/badge/S3 Bucket-9cf.svg?logo=amazons3">]([https://pypi.org/project/plotly/](https://pypi.org/project/plotly/))                  [<img src="https://img.shields.io/badge/Python-3.11-yellow.svg?logo=python">]([https://pypi.org/project/plotly/](https://pypi.org/project/plotly/))

***
 
 <br />

 ## **DOCKER-COMPOSE**
Le fichier `docker-compose.yml` contient tous les conteneurs docker de certains services que nous allons utiliser ainsi que les configurations adéquates pour chaque conteneur. Vous l'aurez compris, notre architecture est majoritairement basée sur Docker. Il contient:
- **un conteneur pour Zookeeper**
 - **un conteneur pour Kafka**
 - **un conteneur pour alert-manager-microservice (développé par nous-même)**
 - **un conteneur pour dataviz-microservice (développé par nous-même)**

Le contenu du fichier `docker-compose.yml` est:

```yml
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    container_name: peacewatcher-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    hostname: zookeeperDaemon

  kafka:
    image: confluentinc/cp-kafka:latest
    container_name: peacewatcher-kafka
    depends_on:
      - zookeeper
    ports:
      - "29092:29092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    hostname: kafkaBroker

  alerts-manager-microservice:
    build: ./Docker/AlertManagerDocker
    container_name: peacewatcher-alerts-manager
    ports:
      - "8501:8501"
    environment:
      KAFKA_BOOTSTRAP_SERVERS: "kafka:9092"
      KAFKA_TOPIC_NAME: "alerts"

  dataviz-microservice:
    build: ./Docker/DataVizDocker
    container_name: peacewatchers-dataviz
    ports:
      - "8502:8502"

```

Pour ce qui est de l'exécution des services, voici les ports alloués à chaque service:

- **Kafka : 9092 (accès en réseau docker)**
- **Kafka : 29092 (accès localement)**
- **alerts-manager-microservice : 8501 (accès localement)**
- **dataviz-microservice : 8502 (accès localement)**

***
 
 <br />
 
 ## **Simulation Send Report**
 Ce projet contient le code nécessaire pour simuler l'envoie de rapports par des drones sur une stream `Kafka`. Il comprend essentiellement au chemin `SimulationSendReport/src/main/scala/edu/efrei/m1bdml`, les fichiers suivants:

- **AlertManager.scala :** qui contient un programme permettant de filter les alertes et de les envoyer sur le topic `alerts`.
- **Producer.scala :** qui contient un programme permettant d'envoyer chaque minute un rapport sur une stream Kafka au topic `PeaceWatcher_topic`.
- **RandomData.scala :** qui contient un programme générant aléatoirement des rapports de drone.
- **Report.scala :** qui contient un case-class définissant la structure d'un rapport de drone.

Les alertes sont ensuite observables en temps réel à partir d'un microservice. En effet, en faisant appel au `alerts-manager-microservice` via la commande `localhost:8501` dans un navigateur web, on peut visualiser les alertes.

***
 
 <br />
 
 ## **Stream Processing With Spark**
 Ce projet contient le code nécessaire pour récupérer les rapports sur le topic `PeaceWatcher_topic` afin de les envoyer sur bucket `AWS S3`. Il comprend essentiellement le fichier `StreamProcessingWithSpark/src/main/scala/edu/efrei/scala/SparkStreaming.scala` qui permet de faire du stream-processing à l'aide de `Spark-Streaming`.

 À partir de `AWS`, on créé un bucket `S3` puis on génère des `credentials` (`fs.s3a.access.key`, `fs.s3a.secret.key`). On spécifie dans le fichier `SparkStreaming.scala` un endpoint de connexion : `"fs.s3a.endpoint" : "s3.us-east-2.amazonaws.com"`.

 À l'exécution de ce programme, il faut attendre chaque 3 minutes pour observer à intervalle régulier de temps l'enrégistrement de rapports dans le bucket `S3`.


***
 
 <br />
 
 ## **Data Analyse With Spark**

 À l'aide la librairie `Plotly-scala`, nous avons réalisé une panoplie de graphes interractifs et à l'aide de `Spark` nous avons analysé les rapports. Le principal fichier est : `DataAnalyseWithSpark/src/main/scala/edu/efrei/scala/DataVisualisationWithSpark.scala`.

 ***
 
 <br />
 
 ## **Data Vizualisation**
Grâce à la configuration du `docker-compose.yml`, il suffit d'entrer dans un navigateur la commande `localhost:8502` pour accéder à un dashboard interractif qui permet de visualizer sous différents angles, les données stockés dans le bucket `AWS S3`.


 ***
 
 <br />
 
 ## **Comment exécuter le projet ?**
 Il vous faut le `JDK` de `JAVA 11`.

 - Dans un terminal, en se placent dans le répertoire `DataEngineeringProject`, exécuter la commande : 

 ```sh
 > docker-compose up
 ```

  - Dans un autre terminal, en se placent dans le répertoire `DataEngineeringProject/SimulationSendReport`, exécuter la commande pour activer le Producer :
```sh
 > sbt run

 Multiple main classes detected. Select one to run:
 [1] edu.efrei.m1bdml.AlertManager
 [2] edu.efrei.m1bdml.Producer
 [3] edu.efrei.m1bdml.RandomData

Enter number: 2
```

- Dans un autre terminal, en se placent dans le répertoire `DataEngineeringProject/SimulationSendReport`, exécuter la commande pour activer l'Alert-manager :
```sh
 > sbt run
 
 Multiple main classes detected. Select one to run:
 [1] edu.efrei.m1bdml.AlertManager
 [2] edu.efrei.m1bdml.Producer
 [3] edu.efrei.m1bdml.RandomData

Enter number: 1

 ```
 Ensuite se rendre dans un navigateur et entrer `localhost:8501` pour visualiser les alertes en temps réel (attendre généralement 1 minute pour observer les premières alertes).


 - Dans un autre terminal, en se placent dans le répertoire `DataEngineeringProject/StreamProcessingWithSpark`, exécuter la commande (supprimer préalablement le répertoire `DataEngineeringProject/StreamProcessingWithSpark/checkpoints` s'il existe):
```sh
 > sbt run
 ```
Ensuite attendre 3 minutes pour visualiser le premier rapport dans le bucket `S3`, puis encore 3 minutes pour voir 2 rapports et ainsi de suite ...

 - Dans un autre terminal, en se placent dans le répertoire `DataEngineeringProject/DataAnalyseWithSpark`, exécuter la commande :
```sh
 > sbt run
 ```

 Vous verrez des fenêtres s'afficher automatiquement dans votre navigateur avec les différents graphes de l'analyse et dans votre terminal, des statistiques sur les rapports dans le bucket `S3`. 

- Enfin pour visualiser le dashboard final, entrer dans votre navigateur, la commande `localhost:8502`.

- Arrêter tous les processus dans les terminaux à l'aide d'un simple `Ctrl+C` ou `Cmd+C`.
