FROM gitpod/workspace-full

USER root
RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein --output-document  /usr/local/bin/lein
RUN chmod +x /usr/local/bin/lein
RUN lein

USER gitpod