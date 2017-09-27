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
| ﻿name     | desc             | type | memo                   | priority
|-----------|----------------- |------|----------------------------------|----|
| init      | 기동 시 모델로 rollback 여부 | boolean | 초기 로딩했던 모델로 rollback 됨  |  1 |
| filePath  | 변경 모델 경로      | String | 모델 파일은 elasticsearch config 폴더 하위에 위치해야함 | 2 |
| url       | 변경 모델 웹 경로    | String | default timeout : 30 sec | 3 |
| timeout   | timeout 값       | String or int | "30s", "1m", 30000, ...                     |

```bash

#현재 모델 정보 확인
curl http://localhost:9200/_daon_model?pretty=true

# 예) 변경 모델 파일 경로 : $ES_HOME/config/models/model.dat
# file 변경 모델 적용 방법 1 
curl "http://localhost:9200/_daon_model?pretty=true&filePath=config/models/model.dat"

# file 변경 모델 적용 방법 2
curl http://localhost:9200/_daon_model?pretty=true -d \
'{ "filePath": "onfig/models/model.dat"}'

# 예) 변경 모델 파일 웹 경로 : http://localhost:5001/v1/model/download?seq=1505783582672
# url 변경 모델 적용 방법 1
curl "http://localhost:9200/_daon_model?pretty=true&url=http://localhost:5001/v1/model/download?seq=1505783582672"

# url 변경 모델 적용 방법 2
curl http://localhost:9200/_daon_model?pretty=true -d \
'{ "url": "http://localhost:5001/v1/model/download?seq=1505783582672"}'
 
# 초기 모델 롤백 적용 방법 1 
curl "http://localhost:9200/_daon_model?pretty=true&init=true"

# 초기 모델 롤백 적용 방법 2
curl http://localhost:9200/_daon_model?pretty=true -d \
'{ "init": "true"}'




```