FROM clojure:openjdk-11-lein-slim-buster

#USER root
#RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein --output-document  /usr/local/bin/lein
#RUN chmod +x /usr/local/bin/lein
RUN lein

USER gitpod
ENV JAVA_HOME=/usr/local/openjdk-11
ENV PATH=/usr/local/openjdk-11/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/usr/local/bin/
