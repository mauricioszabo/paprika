FROM clojure:openjdk-11-lein-slim-buster

USER root
RUN ln -s /usr/local/openjdk-11/bin/java /usr/local/bin/

USER gitpod
ENV JAVA_HOME=/usr/local/openjdk-11
