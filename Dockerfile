FROM openjdk:8-jdk-alpine
VOLUME /tmp
WORKDIR /app

COPY target/java-agent-framework-1.0.0.jar app.jar
COPY uploads /app/uploads

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1024m -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
