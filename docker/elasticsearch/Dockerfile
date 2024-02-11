FROM elasticsearch:8.12.0

RUN bin/elasticsearch-plugin install analysis-nori

COPY dic/userdict_ko.txt config/userdict_ko.txt
COPY dic/synonyms.txt config/synonyms.txt

ENV discovery.type "single-node"
ENV node.name "default-node"
ENV xpack.security.enabled "false"
ENV TZ "Asia/Seoul"
ENV ES_JAVA_OPTS "-Xmx256m -Xms256m"

EXPOSE 9200
