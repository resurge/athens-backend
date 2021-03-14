FROM openjdk:8-alpine

COPY target/uberjar/athens-sync.jar /athens-sync/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/athens-sync/app.jar"]
