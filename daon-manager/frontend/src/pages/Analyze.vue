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

						</div>

            <div style="text-align: right">
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

            <md-table ref="analyzeTable" >
              <md-table-header>
                <md-table-row>
                  <md-table-cell class="md-table-selection">
                    <md-checkbox v-model="toggleCheck" @change="onToggleCheck"></md-checkbox>
                  </md-table-cell>
                  <md-table-head>No.</md-table-head>
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
                <md-table-row v-for="(term, rowIndex) in terms" :key="rowIndex">
                  <md-table-cell class="md-table-selection">
                    <md-checkbox v-model="term.chk" @change="onSelect(term)"></md-checkbox>
                  </md-table-cell>
                  <md-table-cell>{{ rowIndex }}</md-table-cell>
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
                    <div v-for="seq in term.explainInfo.matchInfo.matchSeqs">
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
        <corpus :search-filter="searchFilter" ref="corpus"></corpus>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    data : function(){
      return {
        text: this.$route.query.text || '',
        terms: [],
        searchFilter: {
        	seqs: [],
          keyword: ''
        },
        checkedRows:[],
        toggleCheck: false,
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
      load: function(){
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
        });

        this.searchFilter.seqs = seqs;

        this.$refs.corpus.search();
      },
      onCheck: function(){
        this.load();
      },
      onSelect: function(term){

      },
      onToggleCheck: function(){
      	let vm = this;

        this.terms.forEach(function(term){
        	term.chk = !vm.toggleCheck;
        })


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
    height: 500px;
  }

  .analyzed-text {
    padding: 16px;
  }

  .corpus-results {
    padding-top: 16px;
  }
</style>
