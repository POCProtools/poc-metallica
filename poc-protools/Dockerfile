FROM maven:3-jdk-11-slim AS build

WORKDIR /pwd

COPY ./pom.xml /pwd/
COPY ./poc-protools/pom.xml /pwd/poc-protools/
COPY ./poc-protools-command/pom.xml /pwd/poc-protools-command/

RUN mvn package -Dspring-boot.repackage.skip=true

COPY ./poc-protools/src /pwd/poc-protools/src
COPY ./poc-protools-command/src /pwd/poc-protools-command/src

RUN mvn package -DskipTests=true

FROM openjdk:11-jre-slim

COPY --from=build /pwd/poc-protools/target/*.jar /usr/src/app/main.jar

WORKDIR /usr/src/app
CMD java $JAVA_OPTS -jar main.jar
