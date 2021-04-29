FROM clojure:openjdk-11-lein

ENV HTTP_PORT "13337"

ADD . .

RUN mkdir -p ~/athens/db/athens-sync && \
  lein uberjar

EXPOSE 13337

VOLUME ["/root/athens/db/athens-sync"]

ENTRYPOINT ["java", "-jar", "./target/uberjar/athens-backend.jar"]
