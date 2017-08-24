<template>
  <page-content page-title="모델 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>

        <md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">

            <md-progress v-show="making" md-theme="green" :md-progress="progress"></md-progress>

            <md-toolbar>
              <h1 class="md-title">모델 생성</h1>
              <span class="md-subheading" v-show="making" >모델 생성 중... 소요시간 : {{ elapsedTime | formatDuration}}</span>
              <md-button class="md-raised md-primary" :disabled="making" @click.native="make()">모델 생성하기</md-button>
            </md-toolbar>

          </md-table-card>
        </md-layout>

				<md-layout md-flex="80" md-gutter>
          <md-layout class="corpus-results">
            <md-table-card class="analyze-card-table">

              <md-progress v-show="loading" md-theme="blue" :md-progress="progress" md-indeterminate></md-progress>

              <md-toolbar>
                <h1 class="md-title">
                  모델 생성 결과
                  <small v-show="models.total > 0">( {{models.total}} ) 건</small>
                </h1>

              </md-toolbar>

              <md-table>
                <md-table-header>
                  <md-table-row>
                    <md-table-head>seq</md-table-head>
                    <md-table-head>create_date</md-table-head>
                    <md-table-head>size</md-table-head>
                    <md-table-head>dictionary size</md-table-head>
                    <md-table-head>elapsed time</md-table-head>
                    <md-table-head>apply</md-table-head>
                    <md-table-head>download</md-table-head>
                  </md-table-row>
                </md-table-header>

                <md-table-body>
                  <md-table-row v-for="model in models.list" :key="model.seq">
                    <md-table-cell>{{ model.seq }}</md-table-cell>
                    <md-table-cell>{{ model.create_date | formatDate }}</md-table-cell>
                    <md-table-cell>{{ model.size | formatBytes(2) }}</md-table-cell>
                    <md-table-cell>{{ model.dictionary_count }}</md-table-cell>
                    <md-table-cell>{{ model.elapsed_time | formatDuration }}</md-table-cell>
                    <md-table-cell>
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="applyModel(model.seq)">
                        <md-icon>get_app</md-icon>
                      </md-button>
                    </md-table-cell>
                    <md-table-cell>
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="download(model.seq)">
                        <md-icon>save</md-icon>
                      </md-button>
                    </md-table-cell>
                  </md-table-row>
                  <md-table-row v-if="models.total === 0">
                    <md-table-cell colspan="7">검색 결과가 없습니다.</md-table-cell>
                  </md-table-row>
                </md-table-body>
              </md-table>

              <md-table-pagination
                :md-size="pagination.size"
                :md-page="pagination.page"
                :md-total="pagination.total"
                md-label="Models"
                md-separator="of"
                :md-page-options="[10, 20, 50, 100]"
                @pagination="onPagination"></md-table-pagination>
            </md-table-card>

            <simplert :useRadius="true"
                      :useIcon="true"
                      ref="simplert">
            </simplert>

          </md-layout>
				</md-layout>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    data : function(){
      return {
        making: true,
        loading: false,
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        models: {
          list:[],
          total: 0,
        },
        model:{},
        progress: 0,
        elapsedTime: 0
      }
    },
    mounted() {
      this.search();
      this.connectSrv();
      this.getProgress();
    },
    methods : {

      getProgress: function(){
        let vm = this;

        this.$http.get('/v1/model/progress')
          .then(function(response) {

            let data = response.data;

            vm.markProgress(data);
          })
      },

      markProgress: function(data){
        let vm = this;
        vm.progress = data.progress;

        if(data.running){
          vm.making = true;
          vm.elapsedTime = data.elapsedTime;
        }else{
          vm.making = false;
        }
      },
      make: function(){
        let vm = this;
        vm.making = true;

        this.$http.get('/v1/model/make')
          .then(function(response) {

            let data = response.data;

            vm.markProgress(data);
          })
      },

      remove: function(obj){

      },

      search: function (size = 10, page = 1) {

        let vm = this;

        let params = {
          from: (size * (page -1)),
          size: size
        };
        vm.loading = true;

        this.$http.get('/v1/model/search', {params : params})
          .then(function(response) {

            let data = response.data;

            let hits = data.hits;
            let total = hits.total;
            let list = hits.hits.map(function(m){return m._source});

            vm.models.list = list;
            vm.models.total = total;

            vm.loading = false;
          })
      },
      onPagination: function(obj){
        if(obj){
          this.search(Number(obj.size), Number(obj.page));
        }
      },

      applyModel: function(seq){
        let vm = this;

        let params = {
          seq: seq
        };
        vm.loading = true;

        this.$http.get('/v1/model/apply', {params : params})
          .then(function(response) {
            vm.loading = false;

            let data = response.data;

            if(data){
              let obj = {
                title: '모델 적용',
                message: '완료되었습니다.',
                type: 'info'
              };
              vm.$refs.simplert.openSimplert(obj);
            }

          })
      },

      download: function(seq){
        let vm = this;
//        vm.loading = true;

        location.href = 'http://localhost:5001/v1/model/download?seq=' + seq;

//        vm.loading = false;

//        this.$http.get('/v1/model/download', {params : {seq:seq}})
//          .then(function(response) {
//
//            let data = response.data;
//
//
//            vm.loading = false;
//          })
      },

      onConnected(frame){
        console.log('Connected: ' + frame);

        this.$stompClient.debug = function(str){};
        this.$stompClient.subscribe('/model/progress', this.responseCallback, this.onFailed);
      },
      onFailed(frame){
        console.log('Failed: ' + frame);
      },
      connectSrv(){
        let headers = {};
        this.connetWM('/daon-websocket', headers, this.onConnected, this.onFailed);
      },
      send(){
        let destination = '/exchange/test'
        let invokeId = this.getInvokeId();
        let body = msgHead + invokeId + msgBody;
        this.sendWM(destination, body, invokeId, this.responseCallback, 3000);
      },
      responseCallback(frame){
        let vm = this;

        let data = JSON.parse(frame.body);

        vm.markProgress(data);

        console.log(data);
      },
      disconnect(){
        this.disconnetWM();
      }

    },
    stompClient: {
      monitorIntervalTime: 10000,
      stompReconnect: true,
      timeout: function(orgCmd) {}
    }
  }
</script>

