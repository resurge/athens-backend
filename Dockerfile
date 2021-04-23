FROM clojure:openjdk-11-lein

ENV HTTP_PORT "13337"

ADD . .

RUN lein install

EXPOSE 13337

ENTRYPOINT ["lein", "run"]
