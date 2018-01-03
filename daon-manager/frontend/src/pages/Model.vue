<template>
  <page-content page-title="모델 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>

        <md-layout md-flex="20" md-gutter>
          <md-layout class="container-pad-top">
            <md-table-card class="analyze-card-table">
              <md-toolbar>
                <h1 class="md-title">검색 필터</h1>

                <md-button md-theme="white" class="md-fab md-mini" @click.native="search">
                  <md-icon>search</md-icon>
                </md-button>
              </md-toolbar>

              <form novalidate @submit.stop.prevent="submit">
                <md-layout class="analyzed-text">
                </md-layout>
              </form>

            </md-table-card>
          </md-layout>
        </md-layout>

				<md-layout md-flex="80" md-gutter>
          <md-layout class="container-pad-top">
            <md-table-card class="analyze-card-table">

              <md-progress v-show="loading" md-theme="blue" md-indeterminate></md-progress>

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
                    <md-table-head>create date</md-table-head>
                    <md-table-head>size</md-table-head>
                    <md-table-head>dictionary size</md-table-head>
                    <md-table-head>elapsed time</md-table-head>
                    <md-table-head>button</md-table-head>
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
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="download(model.seq)">
                        <md-icon>save</md-icon>
                      </md-button>
                      <md-button md-theme="white" class="md-fab md-mini"
                                 v-clipboard:copy="copyValue(model.seq)"
                                 v-clipboard:success="onCopy">
                        <md-icon>content_copy</md-icon>
                      </md-button>
                    </md-table-cell>
                  </md-table-row>
                  <md-table-row v-if="models.total === 0">
                    <md-table-cell colspan="8">검색 결과가 없습니다.</md-table-cell>
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
        loading: false,
        baseURL: '',
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        models: {
          list:[],
          total: 0,
        }
      }
    },
    beforeRouteUpdate: function () {
//      console.log('beforeRouteUpdate');
      this.search();
    },
    mounted: function(){
      this.getBaseURL();
      this.search();
    },
    destroyed: function() {
    },

    methods : {

      remove: function(obj){

      },

      search: function () {

        let vm = this;
        let size = vm.pagination.size;
        let page = vm.pagination.page;

        let params = {
          from: (size * (page -1)),
          size: size
        };
        vm.loading = true;

        this.$http.post('/v1/model/search', params)
          .then(function(response) {

            let data = response.data;

            let hits = data.hits;
            let total = hits.total;
            let list = hits.hits.map(function(m){return m._source});

            vm.models.list = list;
            vm.models.total = total;
            vm.pagination.total = total;

            vm.loading = false;
          })
      },
      onPagination: function(obj){
        if(obj){
          this.pagination.page = obj.page;
          this.pagination.size = obj.size;
          this.search();
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
              vm.$root.$refs.simplert.openSimplert({
                title: '모델 적용',
                message: '완료되었습니다.',
                type: 'info'
              });
            }else{
              vm.$root.$refs.simplert.openSimplert({
                title: '모델 적용 실패',
                message: '적용 실패 했습니다.',
                type: 'error'
              });
            }

          })
      },

      download: function(seq){
        location.href = '/v1/model/download?seq=' + seq;
      },

      getBaseURL: function(){

        let vm = this;
        this.$http.get('/v1/model/baseURL')
          .then(function(response) {

            vm.baseURL = response.bodyText;

            console.log(vm.baseURL);
          });
      },

      copyValue: function(seq){
        return this.baseURL + '/v1/model/download?seq=' + seq;
      },
      onCopy: function (e) {
        alert('클립보드에 복사했습니다. 내용 : ' + e.text)
      }
    }
  }
</script>

