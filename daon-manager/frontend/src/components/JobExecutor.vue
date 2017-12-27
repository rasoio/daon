<template>
  <md-sidenav class="md-right md-fixed job-executor" ref="job-executor">
    <md-toolbar>
      <h2 class="md-title" style="flex: 1">Job Executor</h2>

      <md-button class="md-icon-button" @click="close">
        <md-icon>close</md-icon>
      </md-button>

    </md-toolbar>

    <md-layout md-column>
      <md-layout class="container-pad-top">
        <md-table-card class="analyze-card-table">

          <md-toolbar>
            <h1 class="md-title">문장 > 단어 생성</h1>
          </md-toolbar>

          <md-layout class="analyzed-text">
            <md-input-container>
              <label>대상 Index 선택</label>
              <index-select filter="sentences" alias="train_sentences" name="sentencesIndexSelect" id="sentencesIndexSelect" v-model="train_sentences" :multiple="true" :save="true"></index-select>
            </md-input-container>
          </md-layout>

          <md-layout class="analyzed-text">
            <md-layout>
              <md-layout md-vertical-align="center">
                <span class="md-subheading" >문장 > 단어 생성</span>
              </md-layout>
              <md-layout md-align="end">
                <md-button class="md-raised md-primary" :disabled="$store.state.running" @click.native="execute('sentences_to_words')">실행</md-button>
              </md-layout>
            </md-layout>
          </md-layout>
          <md-layout v-show="$store.state.jobName == 'sentences_to_words'" class="analyzed-text" md-gutter>
            <md-progress v-if="$store.state.running" md-theme="green" :md-progress="$store.state.progress"></md-progress>
            <md-layout>
              <span class="md-subheading" v-show="$store.state.running" >실행 중 {{$store.state.progress}} % , 소요시간 : {{ $store.state.elapsedTime | formatDuration}}</span>
            </md-layout>
            <md-layout md-align="end">
              <md-button class="md-raised md-primary" v-show="$store.state.running" @click.native="cancel()">중지</md-button>
            </md-layout>
          </md-layout>

          <md-layout class="analyzed-text">
            <md-layout>
              <md-layout md-vertical-align="center">
                <span class="md-subheading" >문장 > 품사전이 생성</span>
              </md-layout>
              <md-layout md-align="end">
                <md-button class="md-raised md-primary" :disabled="$store.state.running" @click.native="execute('tag_trans')">실행</md-button>
              </md-layout>
            </md-layout>
          </md-layout>
          <md-layout v-show="$store.state.jobName == 'tag_trans'" class="analyzed-text" md-gutter>
            <md-progress v-if="$store.state.running" md-theme="green" :md-progress="$store.state.progress"></md-progress>
            <md-layout>
              <span class="md-subheading" v-show="$store.state.running" >실행 중 {{$store.state.progress}} % , 소요시간 : {{ $store.state.elapsedTime | formatDuration}}</span>
            </md-layout>
            <md-layout md-align="end">
              <md-button class="md-raised md-primary" v-show="$store.state.running" @click.native="cancel()">중지</md-button>
            </md-layout>
          </md-layout>

        </md-table-card>
      </md-layout>

      <md-layout class="container-pad-top">
        <md-table-card class="analyze-card-table">
          <md-toolbar>
            <h1 class="md-title">모델 생성</h1>
          </md-toolbar>

          <md-layout class="analyzed-text">
            <md-input-container>
              <label>모델 생성 Index 선택</label>
              <index-select filter="words" alias="words" name="wordsIndexSelect" id="wordsIndexSelect" v-model="words" :multiple="true" :save="true"></index-select>
            </md-input-container>
          </md-layout>

          <md-layout class="analyzed-text">
            <md-layout>
              <md-layout md-vertical-align="center">
                <span class="md-subheading">모델을 생성합니다.</span>
              </md-layout>
              <md-layout md-align="end">
                <md-button class="md-raised md-primary" :disabled="$store.state.running" @click.native="execute('make_model')">실행</md-button>
              </md-layout>
            </md-layout>
          </md-layout>
          <md-layout v-show="$store.state.jobName == 'make_model'" class="analyzed-text" md-gutter>
            <md-progress v-if="$store.state.running" md-theme="green" :md-progress="$store.state.progress"></md-progress>
            <md-layout>
              <span class="md-subheading" v-show="$store.state.running" >실행 중 {{$store.state.progress}} % , 소요시간 : {{ $store.state.elapsedTime | formatDuration}}</span>
            </md-layout>
            <md-layout md-align="end">
              <md-button class="md-raised md-primary" v-show="$store.state.running" @click.native="cancel()">중지</md-button>
            </md-layout>
          </md-layout>
        </md-table-card>
      </md-layout>
    </md-layout>
  </md-sidenav>
</template>


<style lang="scss">

  $sizebar-size: 500px;

  .job-executor.md-sidenav {
    .md-sidenav-content {
      width: $sizebar-size;
      /*display: flex;*/
      /*flex-flow: column;*/
      /*overflow: hidden;*/
    }

    /*.md-backdrop {*/
      /*@media (min-width: 1281px) {*/
        /*opacity: 0;*/
        /*pointer-events: none;*/
      /*}*/
    /*}*/
  }
</style>

<script>

import MdLayout from "../../node_modules/vue-material/src/components/mdLayout/mdLayout.vue";

export default {
  components: {MdLayout},
  name: 'jobExecutor',
  props: {
    name: String,
    id: String,
  },
  data () {
    return {
      train_sentences: [],
      words: [],
    }
  },
  methods : {
    toggle() {
      this.$refs['job-executor'].toggle();
    },

    close() {
      this.$refs['job-executor'].close();
    },

    execute: function(jobName){
      let vm = this;

      this.$http.get('/v1/job/execute', {params:{'jobName':jobName}})
        .then(function(response) {

          let data = response.data;

          vm.markProgress(data);
        })
    },

    markProgress: function(data){
      this.$store.commit('update', {data: data});
    },

    cancel: function(){
      let vm = this;

      this.$http.get('/v1/job/cancel')
        .then(function(response) {

          let data = response.data;

          vm.markProgress(data);
        })
    },
  }
}
</script>
