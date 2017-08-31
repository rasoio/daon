# Required

- docker (tested version: 17.06.1-ce)
- docker-compose (tested version: 1.14.0)

# Set Up Daon Manager

- git clone https://github.com/rasoio/daon
- cd daon
- ./gradlew :daon-manager:buildDaonManager
- ./gradlew :daon-spark:buildDaonSpark

## 1. Build Daon Manager Docker

- cd docker/daon-manager
- docker build -t daon-manager .

## 2. Build Daon Spark Docker

- cd docker/daon-spark
- docker build -t daon-spark .

## 3. Run Daon Manager

- cd docker
- vi docker-compose.yml 
es 저장 디렉토리 수정 
- docker-compose up

## 4. Init Sentences to ES



### ETC 

- config file Daon Manager - [daonManager.conf](https://github.com/rasoio/daon/blob/master/docker/daon-manager/daonManager.conf)

