# Required

- java : 9+

## Current Version

Lucene : 7.2.1

ES : 6.2.3

버전 정보 : [build.gradle](../build.gradle)

```
    luceneVersion = "7.4.0"
    elasticPluginVersion = "6.4.0"
```

## 1. Build Daon Elasticsearch

```bash
./gradlew :daon-elasticsearch:buildDaonES
```
## 2. Install plugin

```bash
#move es home
cd elasticsearch

#install
bin/elasticsearch-plugin install file://[DAON_HOME]/distributions/analysis-daon-x.x.x.zip

#확인
bin/elasticsearch-plugin list
#결과 값
analysis-daon
```

## 3. Test

```text
# 분석 결과 테스트
GET _analyze
{
  "tokenizer" : "daon_tokenizer",
  "filter" : ["daon_filter"],
  "text" : "다온 형태소 분석기",
  "explain" : true,
  "attributes" : ["text"] 
}

```

## 4 Setting Example

```text
PUT test
{
  "settings":{
    "index" : {
      "analysis" : {
        "analyzer" : {
          "index_korean" : {
            "tokenizer" : "daon_tokenizer",
            "filter": ["lowercase", "index_filter"]
          },
          "search_korean" : {
            "tokenizer" : "daon_tokenizer",
            "filter": ["lowercase", "search_filter"]
          }
        },
        "filter" : {
          "index_filter" : {
            "type" : "daon_filter",
            "mode" : "index"
          },
          "search_filter" : {
            "type" : "daon_filter",
            "mode" : "search",
            "exclude" : ["J","E"]
          }
        }
      }
    }
  },
  "mappings":{
    "contents" : {
      "properties" : {
        "title" : {
          "type" : "text",
          "analyzer" : "index_korean",
          "search_analyzer" : "search_korean"
        },
        "description" : {
          "type" : "text",
          "analyzer" : "index_korean",
          "search_analyzer" : "search_korean"
        }
      }
    }
  }
}

```

### daon_filter Parameters

| ﻿name     | desc             | type | memo                   | priority
|-----------|----------------- |------|----------------------------------|----|
| mode      | "index", "search"| String | index : 어절, 노드 표층형 정보가 추가로 추출 됨 |   |
| include   | 추출에 포함 될 품사(tag) | List | | 1 |
| exclude   | 추출에 제외 될 품사(tag) | List | | 2 |


### 품사(tag) 정보

| ﻿대분류      | tag | 세종 품사 태그                   |
|-------------|------|----------------------------------|
| 체언        | NNG  | 일반 명사                        |
|             | NNP  | 고유 명사                        |
|             | NNB  | 의존 명사                        |
|             | NR   | 수사                             |
|             | NP   | 대명사                           |
|             | NN   |  명사 + 수사 + 대명사 (미등록어 제외)  |
|             | N    |  명사 전체 (미등록어 포함)            |
| 용언        | VV   | 동사                             |
|             | VA   | 형용사                           |
|             | VX   | 보조 용언                        |
|             | VCP  | 긍정 지정사                      |
|             | VCN  | 부정 지정사                      |
|             | VP  | 용언 전체 (지정사 제외)             |
|             | VC  | 지정사 전체                       |
|             | V  | 용언 전체                       |
| 관형사      | MM   | 관형사                           |
| 부사        | MAG  | 일반 부사                        |
|             | MAJ  | 접속 부사                        |
|             | MA  | 부사 전체                        |
|             | M  | 수식언 전체                        |
| 감탄사      | IC   | 감탄사                           |
| 조사        | JKS  | 주격 조사                        |
|             | JKC  | 보격 조사                        |
|             | JKG  | 관형격 조사                      |
|             | JKO  | 목적격 조사                      |
|             | JKB  | 부사격 조사                      |
|             | JKV  | 호격 조사                        |
|             | JKQ  | 인용격 조사                      |
|             | JX   | 보조사                           |
|             | JC   | 접속 조사                        |
|             | JK   | 격조사 전체                        |
|             | J   | 조사 전체                        |
| 선어말 어미 | EP   | 선어말 어미                      |
| 어말 어미   | EF   | 종결 어미                        |
|             | EC   | 연결 어미                        |
|             | ETN  | 명사형 전성 어미                 |
|             | ETM  | 관형형 전성 어미                 |
|             | ET  | 전성 어미 전체                  |
|             | EM  | 어미 전체 (선어말 어미 제외)        |
|             | E   | 어미 전체                       |
| 접두사      | XPN  | 체언 접두사                      |
| 접미사      | XSN  | 명사 파생 접미사                 |
|             | XSV  | 동사 파생 접미사                 |
|             | XSA  | 형용사 파생 접미사               |
|             | XSB  | 부사 파생 접미사                 |
|             | XS  | 접미사 전체                     |
| 어근        | XR   | 어근                             |
| 부호        | SF   | 마침표물음표,느낌표              |
|             | SP   | 쉼표,가운뎃점,콜론,빗금          |
|             | SS   | 따옴표,괄호표,줄표               |
|             | SE   | 줄임표                           |
|             | SO   | 붙임표(물결,숨김,빠짐)           |
|             | SW   | 기타기호 (논리수학기호,화폐기호) |
|             | S   | 부호 전체                   |
| 한글 이외   | SL   | 외국어                           |
|             | SH   | 한자                             |
|             | SN   | 숫자                             |
| 분석 불능   | NF   | 명사추정범주                     |
|             | NV   | 용언추정범주                     |
|             | NA   | 분석불능범주                     |
|             | UNKNOWN   |                      |


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

```text

#현재 모델 정보 확인
GET _daon_model

# 모델 파일 적용 방법
# 예) 변경 모델 파일 경로 : $ES_HOME/config/models/model.dat
GET _daon_model
{ "filePath": "config/models/model.dat"}
 
# url 변경 모델 적용 방법
# 예) 변경 모델 파일 웹 경로 : http://localhost:5001/v1/model/download?seq=1505783582672
GET _daon_model
{ "url": "http://localhost:5001/v1/model/download?seq=1505783582672"}
 
# 초기 모델 롤백 적용 방법 
GET _daon_model
{ "init": "true"}

```