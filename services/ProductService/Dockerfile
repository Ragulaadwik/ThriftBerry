

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ProductService-*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
