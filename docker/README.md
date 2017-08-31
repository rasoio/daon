# Required

- docker (tested version: 17.06.1-ce)
- docker-compose (tested version: 1.14.0)

# Set Up Daon Manager

- git clone https://github.com/rasoio/daon
- cd daon
- ./gradlew :daon-manager:buildDaonManager

## 1. Build Daon Manager Docker

- cd docker/daon-manager
- docker build -t daon-manager .

## 3. Run Daon Manager

- cd docker
- docker-compose up

## 4. Init Sentences to ES

- cd daon
- ./gradlew :daon-spark:buildDaonSpark
- cd jars
- 


### ETC 

- config file Daon Manager - [daonManager.conf](https://github.com/rasoio/daon/blob/master/docker/daon-manager/daonManager.conf)

