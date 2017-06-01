<template>
  <page-content page-title="모델 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>

        <md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">모델 생성</h1>

              <md-button class="md-raised md-primary" :disabled="running" @click.native="reload()">모델 적용하기</md-button>
              <md-button class="md-raised md-primary" :disabled="running" @click.native="create()">모델 생성하기</md-button>
              <!--<md-button md-theme="white" class="md-fab md-mini" @click.native="search">-->
                <!--<md-icon>create</md-icon>-->
              <!--</md-button>-->
            </md-toolbar>

            <!--<div class="analyzed-text">-->
              <md-progress v-show="running" md-theme="green" :md-progress="progress" md-indeterminate></md-progress>
            <!--</div>-->
          </md-table-card>
        </md-layout>

				<md-layout md-flex="80" md-gutter>
          <md-layout class="corpus-results">
            <md-table-card class="analyze-card-table">
              <md-toolbar>
                <h1 class="md-title">
                  모델 생성 결과
                  <small v-show="words.total > 0">( {{words.total}} ) 건</small>
                </h1>

              </md-toolbar>

              <md-spinner v-show="loading" md-indeterminate></md-spinner>

              <md-table>
                <md-table-header>
                  <md-table-row>
                    <md-table-head>seq</md-table-head>
                    <md-table-head>create_date</md-table-head>
                    <md-table-head>size</md-table-head>
                    <md-table-head>download</md-table-head>
                  </md-table-row>
                </md-table-header>

                <md-table-body>
                  <md-table-row v-for="word in words.list" :key="word.seq">
                    <md-table-cell md-numeric>{{ word.seq }}</md-table-cell>
                    <md-table-cell>{{ word.create_date }}</md-table-cell>
                    <md-table-cell>{{ word.size | tagName('detail') }}</md-table-cell>
                    <md-table-cell>
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="download(word.seq)">
                        <md-icon>save</md-icon>
                      </md-button>
                    </md-table-cell>
                  </md-table-row>
                  <md-table-row v-if="words.total === 0">
                    <md-table-cell colspan="4">검색 결과가 없습니다.</md-table-cell>
                  </md-table-row>
                </md-table-body>
              </md-table>

              <md-table-pagination
                :md-size="pagination.size"
                :md-page="pagination.page"
                :md-total="pagination.total"
                md-label="Words"
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
        searchFilter: {
          keyword: '',
          tag: ''
        },
        loading: false,
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        words: {
          list:[],
          total: 0,
        },
        word:{},
        progress: 0,
        running: false
      }
    },
    mounted() {
      this.search()
    },
    methods : {

      create: function(){
        this.running = true




      },
      remove: function(obj){
      },
      reload: function(){
        let vm = this;

        let params = {};
        vm.running = true;

        this.$http.get('/v1/analyze/reload', {params : params})
          .then(function(response) {

            let data = response.data;

            vm.running = false;
          })
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
            let total = hits.totalHits;
            let list = hits.hits.map(function(w){return w.source});

            vm.words.list = list;
            vm.words.total = total;

            vm.loading = false;
          })
      },
      onPagination: function(obj){
        if(obj){
          this.search(Number(obj.size), Number(obj.page));
        }
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
    /*width: 100%;*/
    padding: 0 16px;
  }

  .corpus-results {
    padding-top: 16px;
  }

  .md-table {
    .md-table-cell {
      .md-button {
        width: 40px;
        height: 40px;
        line-height: 40px;
        .md-icon {
          width: 24px;
          min-width: 24px;
          height: 24px;
          min-height: 24px;
          font-size: 24px;
          margin: auto;
          color: rgba(255, 255, 255, .87);
        }
      }
    }
  }
</style>
