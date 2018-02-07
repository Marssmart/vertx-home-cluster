ECHO Starting network node
start "Network node" java -jar ..\vertx-network\target\vertx-network-1.0-SNAPSHOT-fat.jar

ECHO Starting sensor node
start "Sensor node" java -jar ..\vertx-sensors\target\vertx-sensors-1.0-SNAPSHOT-fat.jar

ECHO Starting Http server node
start "Http server node" java -jar ..\vertx-http\target\vertx-http-1.0-SNAPSHOT-fat.jar