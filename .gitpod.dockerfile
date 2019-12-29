FROM gitpod/workspace-full

RUN wget https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein --output-document  /usr/local/bin/lein
RUN lein
