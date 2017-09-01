# Required

- docker (tested version: 17.06.1-ce)
- docker-compose (tested version: 1.14.0)

[docker install](https://docs.docker.com/engine/installation/)

[docker-compose install](https://docs.docker.com/compose/install/)

# Set Up Daon Manager

```bash
git clone https://github.com/rasoio/daon
cd daon

# build daonManager.jar 
./gradlew :daon-manager:buildDaonManager
```

## 1. Build Daon Manager Docker

```bash
cd docker/daon-manager
docker build -t daon-manager .
```
## 2. Run Daon Manager

```bash
# docker 폴더로 이동
cd ../
docker-compose up
```

## 3. Init Sentences to ES

```bash
# daon 홈으로 이동
cd ../

# build daonSpark 
./gradlew :daon-spark:buildDaonSpark

# daonSpark.jar 파일 위치 이동
cd jars

# 말뭉치 파일 다운로드 & 압축풀기
wget https://www.dropbox.com/s/4diw6u3bwap9ntk/sentences.tar.gz && tar xvzf sentences.tar.gz && rm sentences.tar.gz

# 세종 말뭉치 초기 입력
java -cp daonSpark.jar \
-Dspark.es.nodes=localhost \
-Dindex.sentences.jsonPath=./sejong_sentences.json \
daon.spark.write.SejongSentences

# NIADic 말뭉치 초기 입력
java -cp daonSpark.jar \
-Dspark.es.nodes=localhost \
-Dindex.prefix=niadic \
-Dindex.sentences.jsonPath=./niadic_sentences.json \
daon.spark.write.UserSentences
```