# Daon Manager
말뭉치 및 단어를 관리하기 위한 웹 어플리케이션입니다.

단어셋을 지정해서 손쉽게 다향한 분석기 모델을 생성 할수 있습니다.

# Required

- Memory : 16g+
- java : 1.8+
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

## 3. 초기 말뭉치 입력

```bash
# daon 홈으로 이동
cd ../

# build daonSpark 
./gradlew :daon-spark:buildDaonSpark

# daonSpark.jar 파일 위치 이동
cd distributions

# 말뭉치 파일 다운로드 & 압축풀기
wget https://www.dropbox.com/s/i1owxda42uhs6ti/sentences.tar.gz && \
tar xvzf sentences.tar.gz && rm sentences.tar.gz

# 세종 말뭉치 초기 입력
java -cp daonSpark.jar \
-Dspark.es.nodes=localhost \
-Dindex.jsonPath=./sejong_sentences.json \
daon.spark.sentences.SejongSentences

# 사용자 말뭉치 추가 예제
java -cp daonSpark.jar \
-Dspark.es.nodes=localhost \
-Dindex.prefix=niadic \
-Dindex.jsonPath=./user_sentences.json \
daon.spark.sentences.UserSentences
```