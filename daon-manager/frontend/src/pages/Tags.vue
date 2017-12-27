<template>
  <page-content page-title="품사 연결 가중치 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>
				<md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">검색 필터</h1>

              <md-button md-theme="white" class="md-fab md-mini" @click.native="search">
                <md-icon>search</md-icon>
              </md-button>
            </md-toolbar>

            <div class="analyzed-text">
							<form novalidate @submit.stop.prevent="submit">

                <md-input-container>
									<label>연결 종류</label>
                  <md-select v-model="searchFilter.position" multiple @change="update">
                    <md-option value="first">시작 품사</md-option>
                    <md-option value="middle">어절내 품사</md-option>
                    <md-option value="connect">어절간 품사</md-option>
                    <md-option value="last">마지막 품사</md-option>
                  </md-select>
								</md-input-container>
                <!--<p>{{searchFilter.position}}</p>-->

								<md-input-container>
									<label>품사</label>
									<md-input v-model="searchFilter.tag" @keyup.enter.native="search"></md-input>
								</md-input-container>
							</form>
            </div>
          </md-table-card>
				</md-layout>
				<md-layout md-flex="80" md-gutter>
          <md-layout class="container-pad-top">
            <md-table-card class="analyze-card-table">

              <md-progress v-show="loading" md-theme="blue" md-indeterminate></md-progress>

              <md-toolbar>
                <h1 class="md-title">
                  단어 검색 결과
                  <small v-show="tags.total > 0">( {{tags.total}} ) 건</small>
                </h1>

                <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog">
                  <md-icon>add</md-icon>
                </md-button>
              </md-toolbar>

              <md-table>
                <md-table-header>
                  <md-table-row>
                    <md-table-head>seq</md-table-head>
                    <md-table-head>position</md-table-head>
                    <md-table-head>tag1</md-table-head>
                    <md-table-head>tag2</md-table-head>
                    <md-table-head>cost</md-table-head>
                    <md-table-head>button</md-table-head>
                  </md-table-row>
                </md-table-header>

                <md-table-body>
                  <md-table-row v-for="tag in tags.list" :key="tag.seq">
                    <md-table-cell>{{ tag.seq }}</md-table-cell>
                    <md-table-cell>{{ tag.position }}</md-table-cell>
                    <md-table-cell>{{ tag.tag1 }} <small>({{ tag.tag1 | tagName}})</small></md-table-cell>
                    <md-table-cell>{{ tag.tag2 }} <small v-if="tag.tag2">({{ tag.tag2 | tagName}})</small></md-table-cell>
                    <md-table-cell>{{ tag.cost }}</md-table-cell>
                    <md-table-cell></md-table-cell>
                  </md-table-row>
                  <md-table-row v-if="tags.total === 0">
                    <md-table-cell colspan="8">검색 결과가 없습니다.</md-table-cell>
                  </md-table-row>
                </md-table-body>
              </md-table>

              <md-table-pagination
                :md-size="pagination.size"
                :md-page="pagination.page"
                :md-total="pagination.total"
                md-label="Tags"
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
        searchFilter: {
          position: ['first'],
          tag: ''
        },
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        tags: {
          list:[],
          total: 0,
        },
        loading:false
      }
    },
    methods : {

      update: function(obj){
        console.log(obj);

        this.search();
      },

    	remove: function(obj){
    		console.log(obj);
      },

      search: function () {

        let vm = this;

        let size = vm.pagination.size;
        let page = vm.pagination.page;

        let params = {
          position: vm.searchFilter.position,
          tag: vm.searchFilter.tag
        };

        vm.loading = true;

        this.$http.post('/v1/tag/search', params)
          .then(function(response) {

            let data = response.data;

            let hits = data.hits;
            let total = hits.total;
            let list = hits.hits;

            let tags = [];

            list.forEach(function(s, i){
              let obj = s._source;
              let tag = {
                id: s._id,
                seq: (i + 1),
                index: s._index,
                position: obj.position,
                tag1: obj.tag1,
                tag2: obj.tag2,
                cost: obj.cost
              };

              tags.push(tag)
            });

            vm.tags.list = tags;
            vm.tags.total = total;
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

    }
  }
</script>
