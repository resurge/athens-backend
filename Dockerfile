FROM clojure:openjdk-11-lein

ENV HTTP_PORT "1337"

ADD . .

RUN lein install

ENTRYPOINT ["lein", "run"]
