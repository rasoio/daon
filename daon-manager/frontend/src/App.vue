<template>
  <div id="app">
    <md-toolbar>
      <h1 class="md-title">Daon</h1>
    </md-toolbar>

    <div class="main-content">
      <p>텍스트 분석 예제.</p>

      <md-layout md-gutter>

        <md-layout md-flex="40">
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">분석 텍스트 입력</h1>
            </md-toolbar>
            <div class="analyze-text-table">
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
            </md-toolbar>

              <md-table>
                <md-table-header>
                  <md-table-row>
                    <md-table-head>surface</md-table-head>
                    <md-table-head>keywords</md-table-head>
                    <md-table-head>type</md-table-head>
                    <md-table-head>matchSeqs</md-table-head>
                    <md-table-head md-numeric>score</md-table-head>
                    <md-table-head md-numeric>freqScore</md-table-head>
                    <md-table-head md-numeric>tagScore</md-table-head>
                  </md-table-row>
                </md-table-header>

                <md-table-body>
                  <md-table-row v-for="term in terms" :key="term.surface">
                    <md-table-cell>{{ term.surface }}</md-table-cell>
                    <md-table-cell>
                      <div v-for="keyword in term.keywords" class="md-raised md-primary" >
                        <md-checkbox :id="'keyword_' + keyword.seq" name="keywords-seq" v-model="keyword.chk"
                                     class="md-primary" @input="search" :disabled="keyword.seq == 0 ? true : false">
                          {{ keyword.word }} / {{ keyword.tag }} ({{ keyword.seq }})
                        </md-checkbox>
                      </div>
                    </md-table-cell>
                    <md-table-cell>{{ term.explainInfo.matchType.type }}</md-table-cell>
                    <md-table-cell>
                      <div v-for="seq in term.explainInfo.matchType.matchSeqs" >
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

    </div>
  </div>
</template>

<script>
import Vue from 'vue'
import VueMaterial from 'vue-material'
import 'vue-material/dist/vue-material.css'

Vue.use(VueMaterial)

import VueResource from 'vue-resource'
Vue.use(VueResource)


Vue.filter('formatScore', function(number) {
  return isNaN(number) ? 0 : parseFloat(number.toFixed(5))
})

export default {
  name: 'app',
  data : function(){
    return {
      text: this.$route.query.text || '',
      loading: false,
      terms: [],
      corpus: []
    }
  },
  methods : {
    analyze : function () {
      if(!this.text){
        return;
      }

      let params = {text : this.text}

      console.log(params)

      this.$http.get('/v1/analyze/text', {params : params})
        .then(function(response) {

          this.terms = response.data
          response.data.forEach(function(t) {
              console.log(t)
//            terms.push(t)
//            that.suggestions.push(q)
          })
        })
    },
    search : function () {

//        alert('test')

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

      let params = {seq : seqs}

      console.log('seqs', seqs)

      this.$http.get('/v1/corpus/search{?seq}', {params : params})
        .then(function(response) {

          let data = response.data
          console.log(data)
//          response.data.forEach(function(t) {
//            terms.push(t)
//            that.suggestions.push(q)
//          })
        })
    }
  }
}
</script>

<style scoped>
  .main-content {
    padding: 16px;
  }

  .analyze-results {
    padding-left: 16px;
  }

  .analyze-card-table {
    width: 100%;
  }

  .analyze-text-table {
    /*width: 100%;*/
    padding: 16px;
  }
</style>
