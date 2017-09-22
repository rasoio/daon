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

## 4. Model management

### API
/_daon_model

### Parameters
| ﻿name     | desc             | memo                   |
|-----------|----------------- |----------------------------------|
| filePath  | 변경 모델 경로      | 모델 파일은 elasticsearch config 폴더 하위에 위치해야함 |
| url       | 변경 모델 웹 경로    | default timeout : 30 sec |
| default   | 기본 모델 사용여부   |  jar 파일 안에 있는 모델  |
| timeout   | timeout 값       |                       |

```bash

#현재 모델 정보 확인
curl http://localhost:9200/_daon_model?pretty=true

#변경 모델 적용 예) 변경 모델 파일 경로 : $ES_HOME/config/models/model.dat
curl "http://localhost:9200/_daon_model?pretty=true&filePath=config/models/model.dat"

#변경 모델 적용 예) 변경 모델 파일 웹 경로 : http://localhost:5001/v1/model/download?seq=1505783582672
curl "http://localhost:9200/_daon_model?pretty=true&url=http://rndserver3:5001/v1/model/download?seq=1505783582672"


```