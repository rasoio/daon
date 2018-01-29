<template>
  <page-content page-title="Getting Started">
    <div class="main-content">
      <article>
        <section>
          <h2 class="md-headline">초기 학습 문장 import</h2>

          <p>daon 폴더에서 수행</p>
          <code-block lang="bash">
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
          </code-block>
        </section>
      </article>

      <article>
        <h2 class="md-headline">모델 생성</h2>

        <section>
          <h3 class="md-title">모델 기본 정보 생성</h3>
          <p>
            입력 된 문장을 분석해서 단어와 품사전이 정보를 생성. (초기에 한번만 수행하면 됩니다.)<br/>
          오른쪽 상단에
          <md-button class="md-icon-button md-primary">
            <md-icon>build</md-icon>
          </md-button>
          아이콘 클릭 <br/>
          Job Executor 메뉴에서 <br/>
          1. 문장 > 단어 생성 실행 <br/>
          2. 문장 > 품사전이 생성 실행
          </p>

        </section>

        <section>
          <h3 class="md-title">모델 생성</h3>
          <p>
            오른쪽 상단에
            <md-button class="md-icon-button md-primary">
              <md-icon>build</md-icon>
            </md-button>
            아이콘 클릭 <br/>
            모델 생성 실행
          </p>
        </section>

      </article>

      <article>
        <h2 class="md-headline">모델 적용</h2>

        <section>
          <h3 class="md-title">테스트 분석을 위한 적용</h3>

          <p>
            Model 메뉴에서
            <md-button class="md-icon-button md-primary"><md-icon>get_app</md-icon></md-button> 버튼 클릭 적용 시 해당 모델로 분석 테스트를 수행할수 있습니다
          </p>
        </section>
        <section>
          <h3 class="md-title">모델 적용</h3>
          <p>
            Model 메뉴에서<md-button class="md-icon-button md-primary"><md-icon>save</md-icon></md-button> 버튼 클릭 모델 파일을 다운로드해서 적용할수 있습니다
            Model 메뉴에서<md-button class="md-icon-button md-primary"><md-icon>content_copy</md-icon></md-button> 버튼 클릭 모델 다운로드 경로를 복사해서 적용할수 있습니다
          </p>
        </section>
        <section>
          <h3 class="md-title">Elasticsearch 모델 적용</h3>
          <p>
            <code-block lang="bash">
              # build es plugin
              ./gradlew :daon-elasticsearch:buildDaonES

              #move target es home
              cd [ES_HOME]

              #install
              bin/elasticsearch-plugin install file://[DAON_HOME]/distributions/analysis-daon-x.x.x.zip

              #확인
              bin/elasticsearch-plugin list

              #현재 모델 정보 확인
              curl http://localhost:9200/_daon_model?pretty=true

              # file 모델 적용 방법 (해당 파일은 ES_HOME 폴더 내부에 위치해야함)
              curl http://localhost:9200/_daon_model?pretty=true -d \
              '{ "filePath": "[FILE_PATH]"}'

              # url 모델 적용 방법 (ES 서버에서 접근 가능해야함)
              curl http://localhost:9200/_daon_model?pretty=true -d \
              '{ "url": "http://localhost:5001/v1/model/download?seq=1505783582672"}'

              # 초기 로딩 모델로 롤백
              curl http://localhost:9200/_daon_model?pretty=true -d \
              '{ "init": "true"}'
            </code-block>
          </p>
        </section>

      </article>
    </div>
  </page-content>
</template>

<style lang="scss" scoped>
  .main-content {
    position: relative;
  }
  article {
    max-width: 960px;
  }
  section + section,
  article + article {
    margin-top: 36px;
  }
  .code-block,
  .md-tabs {
    max-width: 100%;
  }
  .md-tab {
    border-top: 1px solid rgba(#000, .12);
    padding: 0;
  }
  iframe {
    height: auto;
    min-height: 620px;
  }
</style>
