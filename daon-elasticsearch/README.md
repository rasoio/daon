## 1. Build Daon Elasticsearch

```bash
./gradlew :daon-elasticsearch:buildDaonES
```
## 2. Install plugin

```bash

cd distributions

#download es
export ES_TAR="elasticsearch-5.6.0.tar.gz";\
wget https://artifacts.elastic.co/downloads/elasticsearch/$ES_TAR && \
tar xvzf $ES_TAR && rm $ES_TAR

#move es home
cd elasticsearch-5.6.0

#install
bin/elasticsearch-plugin install file://[DAON_HOME]/distributions/analysis-daon-5.6.0.zip

#확인
bin/elasticsearch-plugin list

```

## 3. Test

```bash
# 테스트
curl http://localhost:9200/_analyze?pretty=true -d \
'{ "analyzer": "daon_analyzer", "text": "다온 형태소 분석기"}'

```