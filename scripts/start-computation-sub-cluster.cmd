ECHO Starting queue manager node
start "Queue manager node" java -jar ..\vertx-cluster-task-queue\target\vertx-cluster-task-queue-1.0-SNAPSHOT-fat.jar

ECHO Starting workers
start "Worker 1" java -jar ..\vertx-mma-rankings\target\vertx-mma-rankings-1.0-SNAPSHOT-fat.jar
start "Worker 2" java -jar ..\vertx-mma-rankings\target\vertx-mma-rankings-1.0-SNAPSHOT-fat.jar
start "Worker 3" java -jar ..\vertx-mma-rankings\target\vertx-mma-rankings-1.0-SNAPSHOT-fat.jar