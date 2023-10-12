ARG JRE_VERSION=notset
FROM eclipse-temurin:${JRE_VERSION}-jre
COPY ./target/sel-repro-*.jar .
ENTRYPOINT java -jar $JAVA_OPTS ./*.jar