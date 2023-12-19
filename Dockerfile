FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY *.jar /app/
ENTRYPOINT ["sh", "-c", "java -jar *.jar"]
EXPOSE 8080