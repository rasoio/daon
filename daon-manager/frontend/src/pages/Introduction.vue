<template>
  <page-content page-title="Introduction">
    <div class="main-content">
      <div class="introduction">
        <div class="button-actions">
          <div class="example">
            <!--<img src="../assets/vue-material-example.png" alt="Material Design">-->
          </div>

          <div class="introduction">한글 형태소 분석기</div>

          <md-button class="md-primary md-raised" href="#/analyze">분석 예제</md-button>
          <md-button class="md-primary md-raised" href="#/corpus">말뭉치 관리</md-button>
        </div>

        <div class="row">
          <div class="column">
            <h2 class="md-headline">말뭉치 관리 기능 제공</h2>
            <p>
              <a href="https://www.elastic.co/kr/products/elasticsearch" target="_blank" rel="noopener">Elasticsearch</a> 를 통한 말뭉치 관리 기능<br/>
              기본 말뭉치로 <a href="https://ithub.korean.go.kr/user/member/memberPdsReferenceManager.do" target="_blank" rel="noopener">세종 말뭉치</a>,
              <a href="https://kbig.kr/index.php?page=0&sv=title&sw=&q=knowledge/pds_&tgt=view&page=1&idx=16451&sw=&sv=title" target="_blank" rel="noopener">NIADic</a> 을 사용
            </p>
          </div>

          <div class="column">
            <h2 class="md-headline">모델 생성 & 리로딩 가능</h2>
            <p>말뭉치 기반 Spark job을 통해 Model 생성<br/> URL, File, Elasticsearch 를 통한 모델 리로딩 지원</p>
          </div>

          <div class="column">
            <h2 class="md-headline">빠른 분석 속도 & 정확도</h2>
            <p>Lucene FST를 사용한 빠른 성능 및 메모리 효율화,<br/> 96%의 정확도(측정 방식 : EvaluateModel)</p>
          </div>
        </div>
      </div>
    </div>
  </page-content>
</template>

<style lang="scss" scoped>
  .introduction {
    max-width: 960px;
    margin: 0 auto;
  }
  .example {
    margin-bottom: 24px;
    overflow: hidden;
  img {
    margin: -45px auto;
    display: block;
  }
  }
  .button-actions {
    margin-bottom: 16px;
    text-align: center;
  .introduction {
    margin-bottom: .8em;
    font-size: 20px;
    line-height: 1.6em;
  }
  .md-button {
    margin-left: 0;
    color: #fff !important;
  }
  }
  .row {
    margin: 0 -10px;
    display: flex;
    flex-flow: row wrap;
  }
  .column {
    margin: 0 10px;
    flex: 1 1 30%;
  @media (max-width: 640px) {
    flex: 1 1 100%;
  }
  }
</style>
