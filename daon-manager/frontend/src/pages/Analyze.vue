<template>
  <page-content page-title="형태소 분석 예제">
    <div class="main-content">
      <md-layout md-gutter>

        <md-layout md-flex="40">
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">분석 텍스트 입력</h1>
            </md-toolbar>
            <div class="analyzed-text">
              <md-input-container >
                <label>분석 대상 텍스트 입력</label>
                <md-textarea v-model="text" placeholder="분석할 텍스트를 입력하세요" required maxlength="10000" @keyup.enter.native="analyze"></md-textarea>

              </md-input-container>
              <md-button class="md-raised md-primary" @click.native="analyze" @click="analyze">분석</md-button>
            </div>
          </md-table-card>
        </md-layout>

        <md-layout md-flex="60" class="analyze-results">
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">분석 결과</h1>
              <small>체크 박스 체크 시 학습 말뭉치 검색</small>
            </md-toolbar>

            <md-table>
              <md-table-header>
                <md-table-row>
                  <md-table-head>surface</md-table-head>
                  <md-table-head>keywords</md-table-head>
                  <md-table-head>type</md-table-head>
                  <md-table-head>match seq's</md-table-head>
                  <md-table-head md-numeric>score</md-table-head>
                  <md-table-head md-numeric>freq score</md-table-head>
                  <md-table-head md-numeric>tag score</md-table-head>
                </md-table-row>
              </md-table-header>

              <md-table-body>
                <md-table-row v-for="term in terms" :key="term.surface">
                  <md-table-cell>{{ term.surface }}</md-table-cell>
                  <md-table-cell>
                    <div v-for="keyword in term.keywords" class="md-raised md-primary" >
                      <md-checkbox :id="'keyword_' + keyword.seq" name="keywords-seq" v-model="keyword.chk"
                                   class="md-primary" @input="onCheck" :disabled="keyword.seq == 0 ? true : false">
                        <keyword :keyword="keyword"></keyword>
                      </md-checkbox>
                    </div>
                  </md-table-cell>
                  <md-table-cell>{{ term.explainInfo.matchInfo.type }}</md-table-cell>
                  <md-table-cell>
                    <div v-for="seq in term.explainInfo.matchInfo.matchSeqs" >
                      {{ seq }}
                    </div>
                  </md-table-cell>
                  <md-table-cell md-numeric>{{ term.explainInfo.score | formatScore }}</md-table-cell>
                  <md-table-cell md-numeric>{{ term.explainInfo.freqScore | formatScore }}</md-table-cell>
                  <md-table-cell md-numeric>{{ term.explainInfo.tagScore | formatScore }}</md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </md-layout>
      </md-layout>



      <md-layout md-gutter>
        <md-layout class="corpus-results">
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">학습 말뭉치 검색 결과</h1>
              <small v-show="total > 0">{{total}} 건</small>
            </md-toolbar>

            <md-table>
              <md-table-header>
                <md-table-row>
                  <md-table-head width="10">No.</md-table-head>
                  <md-table-head width="*">sentence</md-table-head>
                </md-table-row>
              </md-table-header>

              <md-table-body>
                <md-table-row v-for="sentence in sentences" :key="sentence.id">
                  <md-table-cell md-numeric>{{ sentence.seq }}</md-table-cell>
                  <md-table-cell>
                    <md-layout md-column md-gutter >
                      <md-layout>
                        <span class="md-title">
                          {{ sentence.sentence }}
                        </span>
                      </md-layout>
                      <!--<hr/>-->
                      <md-layout v-for="eojeol in sentence.eojeols" :key="eojeol.seq">
                        <span class="md-subheading">
                          {{eojeol.surface}} :
                        </span>
                        <div v-for="morpheme in eojeol.morphemes" :key="morpheme.seq">
                          <span>&nbsp;</span>
                          <keyword :keyword="morpheme" :class="{ highlight: isContains(morpheme.seq) }" theme="round"></keyword>
                        </div>
                      </md-layout>

                    </md-layout>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>

            <md-table-pagination
              :md-size="pagination.size"
              :md-page="pagination.page"
              :md-total="pagination.total"
              md-label="Sentences"
              md-separator="of"
              :md-page-options="[10, 20, 50]"
              @pagination="onPagination"></md-table-pagination>
          </md-table-card>
        </md-layout>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    name: 'analyze',
    data : function(){
      return {
        text: this.$route.query.text || '',
        loading: false,
        terms: [],
        corpus: [],
        sentences: [],
        searchSeqs: [],
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        }
      }
    },
    methods : {
      analyze: function () {
        if(!this.text){
          return;
        }

        let params = {text : this.text}

        console.log(params)

        this.$http.get('/v1/analyze/text', {params : params})
          .then(function(response) {

            this.terms = response.data
//          response.data.forEach(function(t) {
//              console.log(t)
//            terms.push(t)
//            that.suggestions.push(q)
//          })
          })
      },
      onCheck: function(){
        this.pagination.page = 1
        this.search()
      },
      search: function () {

        let seqs = Array.prototype.concat.apply([], this.terms.map(function(term){

            let keywords = []
            if(term.keywords){
              term.keywords.forEach(function(keyword){
                if(keyword.chk){
                  keywords.push(keyword)
                }
              })
            }

            return keywords
          }).filter(function(keywords){
            return keywords.length > 0
          })
        ).map(function(keyword){
          return keyword.seq
        })

        let vm = this

        vm.searchSeqs = seqs

        let params = {
          seq: seqs,
          from: (vm.pagination.size * (vm.pagination.page -1)),
          size: vm.pagination.size
        }

        console.log('params', params)

        this.$http.get('/v1/corpus/search{?seq}', {params : params})
          .then(function(response) {

            let data = response.data

            let hits = data.hits
            let total = hits.totalHits
            let list = hits.hits

            let sentences = []

            list.forEach(function(s, i){
              console.log(s, i)

              let obj = s.source
              let sentence = {
                id: s.id,
                seq: params.from + (i + 1),
                sentence: obj.sentence,
                eojeols: obj.eojeols
              }

              sentences.push(sentence)
            })

            vm.sentences = sentences

            vm.total = total
          })
      },
      onPagination: function(obj){
        console.log('page', obj)

        if(obj){
          this.pagination.size = obj.size
          this.pagination.page = obj.page
          this.search()
        }
      },
      isContains: function(target){

        let seqs = this.searchSeqs

        if(Array.isArray(target)){

          let find = false
          target.forEach(function(keyword){
            if(seqs.indexOf(keyword.seq) > -1){
              find = true
            }
          })

          return find
        }else{
          return seqs.indexOf(target) > -1
        }
      }

    }
  }
</script>

<style lang="scss" scoped>
  .analyze-results {
    padding-left: 16px;
  }

  .analyze-card-table {
    width: 100%;
  }

  .analyzed-text {
    padding: 16px;
  }

  .corpus-results {
    padding-top: 16px;
  }
</style>
