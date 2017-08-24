<template>
  <page-content page-title="형태소 분석 예제">
    <div class="main-content">
      <md-layout md-gutter>

        <md-layout md-flex="40">
					<md-table-card class="analyze-card-table">
						<md-toolbar>
							<h1 class="md-title">분석 텍스트 입력</h1>
              <div style="text-align: right">
                <md-button class="md-raised md-primary" @click.native="analyze" @click="analyze">분석</md-button>
              </div>
						</md-toolbar>
						<div class="analyzed-text">
              <md-input-container >
                <label>분석 대상 텍스트 입력</label>
                <md-textarea v-model="text" placeholder="분석할 텍스트를 입력하세요" required maxlength="10000" @keyup.native="analyze"></md-textarea>
              </md-input-container>
						</div>

					</md-table-card>
        </md-layout>

        <md-layout md-flex="60" class="analyze-results">
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">분석 결과</h1>
              <small>체크 박스 체크 시 학습 말뭉치 검색</small>

              <div style="text-align: right">
                <md-button class="md-raised md-primary" @click.native="load" @click="load">말뭉치 검색</md-button>
              </div>
            </md-toolbar>

            <md-table ref="analyzeTable" >
              <md-table-header>
                <md-table-row>
                  <md-table-cell class="md-table-selection">
                    <md-checkbox v-model="toggleCheck" @change="onToggleCheck"></md-checkbox>
                  </md-table-cell>
                  <md-table-head>No.</md-table-head>
                  <md-table-head>어절</md-table-head>
                  <md-table-head>term's</md-table-head>
                </md-table-row>
              </md-table-header>

              <md-table-body>
                <md-table-row v-for="(eojeol, rowIndex) in eojeols" :key="rowIndex">
                  <md-table-cell class="md-table-selection">
                    <md-checkbox v-model="eojeol.chk" @input="onCheck"></md-checkbox>
                  </md-table-cell>
                  <md-table-cell>{{ rowIndex }}</md-table-cell>
                  <md-table-cell>{{ eojeol.surface }}</md-table-cell>
                  <md-table-cell>
                    <div v-for="term in eojeol.terms" class="md-raised md-primary" >
                      <div>{{ term.surface }}</div>
                      <div v-for="keyword in term.keywords" class="md-raised md-primary" >
												<md-checkbox :id="'keyword_' + keyword.seq" name="keywords-seq" v-model="keyword.chk"
																		 class="md-primary" @input="onCheck" :disabled="keyword.seq === 0">
													<keyword :keyword="keyword"></keyword>
												</md-checkbox>
                      </div>
                    </div>
                  </md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>
          </md-table-card>
        </md-layout>
      </md-layout>

      <md-layout md-gutter>
        <corpus-list :search-filter="searchFilter" ref="corpusList"></corpus-list>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    data : function(){
      return {
        text: this.$route.query.text || '',
        eojeols: [],
        searchFilter: {
          checkTerms: []
        },
        checkedRows:[],
        toggleCheck: false,
      }
    },
    methods : {
      analyze: function () {
        let vm = this;

        vm.toggleCheck = false;
        vm.searchFilter.checkTerms = [];

        if(!vm.text){
          vm.eojeols = [];
          return;
        }

        let params = {text : this.text};

//        console.log(params);

        this.$http.get('/v1/analyze/text', {params : params})
          .then(function(response) {
            this.eojeols = response.data
          })
      },
      load: function(){

        let checkTerms = Array.prototype.concat.apply([], this.eojeols.map(function(eojeol){
          let surface = '';

          if(eojeol.chk){
            surface = eojeol.surface
          }

          let keywords = [];
          eojeol.terms.forEach(function(term){
            term.keywords.forEach(function(keyword){
              if(keyword.chk){
                keywords.push({word: keyword.word, tag: keyword.tag})
              }
            })
          });

          return {surface: surface, keywords: keywords}
        }).filter(function(info){
          return info.surface || info.keywords.length > 0;
        }));

//        console.log(checkTerms);

        this.searchFilter.checkTerms = checkTerms;

        this.$refs.corpusList.search();

      },
      onCheck: function(){
        this.load();
      },
      onToggleCheck: function(){
      	let vm = this;

        this.eojeols.forEach(function(eojeol){
        	eojeol.chk = !vm.toggleCheck;
          eojeol.terms.forEach(function(term){
            term.keywords.forEach(function(keyword){
              keyword.chk = !vm.toggleCheck;
            })
          })
        });

        this.load();
      }

    }
  }
</script>

