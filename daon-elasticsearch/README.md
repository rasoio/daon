## 1. Build Daon Elasticsearch

```bash
./gradlew :daon-elasticsearch:buildDaonES
```
## 2. Install plugin

```bash

cd distributions

export ES_TAR="elasticsearch-5.6.0.tar.gz";\
wget https://artifacts.elastic.co/downloads/elasticsearch/$ES_TAR && \
tar xvzf $ES_TAR && rm $ES_TAR

cd elasticsearch-5.6.0



```

## 3. Test

```bash

```