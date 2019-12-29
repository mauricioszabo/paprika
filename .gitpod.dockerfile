FROM clojure:openjdk-11-lein-slim-buster

USER root
RUN ln -s /usr/local/openjdk-11/bin/java /usr/local/bin/
RUN apt update && apt -qy install wget xz-utils

RUN wget https://nodejs.org/dist/v12.14.0/node-v12.14.0-linux-x64.tar.xz
RUN tar -xf node*
RUN cp -av node*/* /usr/local/
RUN rm node* -R

USER gitpod
ENV JAVA_HOME=/usr/local/openjdk-11
