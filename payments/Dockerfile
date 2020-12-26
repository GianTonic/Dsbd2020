FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY entities/ entities
WORKDIR /project/entities
RUN mvn install
WORKDIR /project
COPY payments .
#ADD ./target/productmanager-0.0.1-SNAPSHOT.jar ./productmanager.jar
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/payments-0.0.1-SNAPSHOT.jar ./payments.jar

CMD java -jar payments.jar