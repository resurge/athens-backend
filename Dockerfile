FROM openjdk:8-alpine

COPY target/uberjar/athens-sync.jar /athens-sync/app.jar

EXPOSE 3010

CMD ["java", "-jar", "/athens-sync/app.jar"]
