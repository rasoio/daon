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
            <h2 class="md-headline">말뭉치 관리</h2>
            <p>Vue Material is lightweight framework built exactly according to the <a href="http://material.google.com" target="_blank" rel="noopener">Material Design</a> specs. Build powerful and well-designed web apps that can can fit on every screen.</p>
          </div>

          <div class="column">
            <h2 class="md-headline">모델 생성 & 리로딩</h2>
            <p>You can generate and use themes dynamically, use components on demand, take advantage of UI Elements and Components with an ease-to-use API and more...</p>
          </div>

          <div class="column">
            <h2 class="md-headline">빠른 분석 속도 & 정확도</h2>
            <p>It aims to deliver a collection of reusable components and a series of UI Elements to build applications with support to all modern Web Browsers through Vue 2.0.</p>
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
