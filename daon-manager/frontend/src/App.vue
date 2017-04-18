<template>
  <div id="app">
    <md-toolbar>
      <h1 class="md-title">Daon</h1>
    </md-toolbar>

    <div class="main-content">
      <p>텍스트 분석 예제.</p>

      <md-layout md-gutter>
        <md-layout md-flex="50">
          <!--<form novalidate @submit.stop.prevent="submit">-->
            <md-input-container>
              <label>분석 대상 텍스트 입력</label>
              <md-textarea v-model="text" placeholder="분석할 텍스트를 입력하세요" required maxlength="10000"></md-textarea>

              <!--<span class="md-error">Textarea validation message</span>-->
            </md-input-container>
          <md-button class="md-raised md-primary" @click.native="analyze" @click="analyze">분석</md-button>
          <!--</form>-->

        </md-layout>
        <md-layout md-flex="50">
          분석 결과
          <!--<md-chips v-model="terms" md-static>-->
            <!--<template scope="term">{{ term.surface }}</template>-->
          <!--</md-chips>-->

          <ul id="example-1">
            <li v-for="term in terms">
              {{ term.surface }}

              <span v-for="keyword in term.keywords">
                {{ keyword.seq }} {{ keyword.word }} {{ keyword.tag }} {{ keyword.tf }}
              </span>
              freqScore : {{ term.explainInfo.freqScore }}
              tagScore : {{ term.explainInfo.tagScore }}
              score : {{ term.explainInfo.score }}

            </li>
          </ul>
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
Vue.use(VueResource);

export default {
  name: 'app',
  data : function(){
    return {
      text: this.$route.query.text || '',
      loading: false,
      terms: []
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
//          response.data.forEach(function(t) {
//              console.log(t)
//            terms.push(t)
//            that.suggestions.push(q)
//          })
        })
    }
  }
}
</script>

<style>
  .main-content {
    padding: 16px;
  }
</style>
