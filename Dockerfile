FROM openjdk:21
COPY target/orders-0.1.jar orders-0.1.jar
ENTRYPOINT ["java","-jar","/orders-0.1.jar"]