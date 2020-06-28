cd /Users/tianming/Downloads/kafka_2.12-2.4.1 
zookeeper-server-start config/zookeeper.properties
kafka-server-start config/server.properties

kafka-topics --zookeeper 127.0.0.1:2181 --list 
kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1
nano config/server.properties

kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic 
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning

kafka-consumer-groups --bootstrap-server localhost:9092 --list
kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group first-application
kafka-consumer-groups --bootstrap-server localhost:9092 --group first-application --reset-offsets --to-datetime 2017-08-04T00:00:00.000 --execute --topic first_topic
kafka-consumer-groups --bootstrap-server localhost:9092 --group first-application --reset-offsets --shift-by -2 --execute --topic first_topic
kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group first-application

connect-standalone connect-standalone.properties twitter.properties
lsof -nP -iTCP:8083 | grep LISTEN
kill -9 66201
