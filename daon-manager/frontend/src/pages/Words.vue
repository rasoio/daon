<template>
  <page-content page-title="단어 관리">
    <div class="main-content">

      <md-layout md-column>
				<md-layout md-flex="20" >
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">검색 필터</h1>

              <md-button md-theme="white" class="md-fab md-mini" @click.native="search">
                <md-icon>search</md-icon>
              </md-button>
            </md-toolbar>


            <form novalidate @submit.stop.prevent="submit">
              <md-layout md-gutter class="analyzed-text">
                <md-input-container>
                  <label>검색 대상 (체크안하면 전체)</label>
                  <index-select filter="words" name="indexName" id="indexName" v-model="searchFilter.indices" :multiple="true" @change="search" ref="wordsIndexSelect"></index-select>
                </md-input-container>
                <div>
                  <label>검색 조건</label>
                  <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'like';search()" id="condition-like" name="condition" md-value="like">Like</md-radio>
                  <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'equals';search()" id="condition-equals" name="condition" md-value="equals">Equals</md-radio>
                  <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'prefix';search()" id="condition-prefix" name="condition" md-value="prefix">Prefix</md-radio>
                  <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'suffix';search()" id="condition-suffix" name="condition" md-value="suffix">Suffix</md-radio>
                </div>
                <md-input-container>
                  <label>표층형</label>
                  <md-input v-model="searchFilter.surface" @keyup.enter.native="search"></md-input>
                </md-input-container>

                <md-input-container>
                  <label>단어</label>
                  <md-input v-model="searchFilter.word" @keyup.enter.native="search"></md-input>
                </md-input-container>
              </md-layout>
            </form>

          </md-table-card>
				</md-layout>
				<md-layout md-flex="80">
          <md-layout class="container-pad-top">
            <md-table-card class="analyze-card-table">

              <md-progress v-show="loading" md-theme="blue" md-indeterminate></md-progress>

              <md-toolbar>
                <h1 class="md-title">
                  단어 검색 결과
                  <small v-show="words.total > 0">( {{words.total}} ) 건</small>
                </h1>

                <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog">
                  <md-icon>add</md-icon>
                </md-button>
              </md-toolbar>

              <md-table>
                <md-table-header>
                  <md-table-row>
                    <md-table-head>seq</md-table-head>
                    <md-table-head>surface</md-table-head>
                    <md-table-head>morphemes</md-table-head>
                    <md-table-head>weight</md-table-head>
                    <md-table-head>button</md-table-head>
                  </md-table-row>
                </md-table-header>

                <md-table-body>
                  <md-table-row v-for="word in words.list" :key="word.seq">
                    <md-table-cell>{{ word.seq }}</md-table-cell>
                    <md-table-cell>
                      <md-layout md-column md-gutter >
                        <md-layout>
													<span>
														{{ word.index }}
													</span>
                        </md-layout>
                        <md-layout>
													<span class="md-title">
														{{ word.surface }}
													</span>
                        </md-layout>
                      </md-layout>
                    </md-table-cell>
                    <md-table-cell>
                      <md-layout>
												<div v-for="morpheme in word.morphemes" :key="morpheme.seq">
													<span>&nbsp;</span>
													<keyword :keyword="morpheme" :class="{ highlight: isContains(morpheme) }" theme="round"></keyword>
												</div>
                      </md-layout>
                    </md-table-cell>
                    <md-table-cell>
                      {{ word.weight }}
                    </md-table-cell>
                    <md-table-cell>
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog(word.id, word.index)">
                        <md-icon>edit</md-icon>
                      </md-button>
                      <md-button md-theme="white" class="md-fab md-mini" @click.native="remove(word.id, word.index)">
                        <md-icon>delete</md-icon>
                      </md-button>
                    </md-table-cell>
                  </md-table-row>
                  <md-table-row v-if="words.total === 0">
                    <md-table-cell colspan="8">검색 결과가 없습니다.</md-table-cell>
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

            <word-form ref="wordForm"></word-form>

          </md-layout>
				</md-layout>

        <md-layout class="container-pad-top">
          <md-table-card class="analyze-card-table">

            <md-progress v-show="upload_loading" md-theme="blue" md-indeterminate></md-progress>

            <md-toolbar>
              <h1 class="md-title">파일 업로드</h1>

              <md-button class="md-raised md-primary" @click.native="download()">샘플 파일 다운로드</md-button>

              <md-button md-theme="white" class="md-fab md-mini" @click.native="fileUpload" :disabled="$store.state.running || upload_loading" >
                <md-icon>file_upload</md-icon>
              </md-button>

            </md-toolbar>

            <!--<form novalidate @submit.stop.prevent="submit" enctype="multipart/form-data">-->

            <md-layout md-column>
              <md-layout class="analyzed-text">
                <md-layout md-vertical-align="start">
                  <md-input-container>
                    <label>업로드 할 index prefix</label>
                    <md-input v-model="prefix"></md-input>
                  </md-input-container>
                </md-layout>
                <md-layout md-vertical-align="start">
                  <md-input-container>
                    <label>업로드 파일 Only CSV</label>
                    <md-file accept=".csv" @selected="onFileSeleted" ></md-file>
                  </md-input-container>
                </md-layout>
                <md-layout>
                  <md-radio v-model="isAppend" id="append" name="is-append-group1" :md-value="'true'">append</md-radio>
                  <md-radio v-model="isAppend" id="overwrite" name="is-append-group1" :md-value="'false'">overwrite</md-radio>
                </md-layout>
              </md-layout>
            <!--</form>-->
            </md-layout>

            <md-table v-if="errors.length > 0">
              <md-table-header>
                <md-table-row>
                  <md-table-head>error message's</md-table-head>
                </md-table-row>
              </md-table-header>

              <md-table-body>
                <md-table-row v-for="error in errors" :key="error">
                  <md-table-cell><span class="highlight">{{ error }}</span></md-table-cell>
                </md-table-row>
              </md-table-body>
            </md-table>

          </md-table-card>
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
        upload_loading: false,
        searchFilter: {
          surface: '',
          word: '',
          condition: 'like',
          indices:[]
        },
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        words: {
          list:[],
          total: 0,
        },
        prefix:null,
        uploadFile:null,
        isAppend:false,
        errors:[],
      }
    },
    computed: {
      searchSeqs : function(){
      	return []
      }
    },
    methods : {
      openDialog: function(id, index) {
        this.$refs.wordForm.openDialog(id, index);
      },
    	remove: function(id, index){
    		console.log(id, index);

        let vm = this;
        let params = {
          id: vm.id,
          index: vm.index,
        };

        this.$http.post('/v1/word/delete', params)
          .then(function(response) {
//            console.log(response);

            vm.$root.$refs.simplert.openSimplert({
              title: '삭제 되었습니다.',
              type: 'info'
            });

          })
      },

      search: function () {

        let vm = this;
    	  let size = vm.pagination.size;
    	  let page = vm.pagination.page;

        let params = {
          surface: vm.searchFilter.surface,
          word: vm.searchFilter.word,
          condition: vm.searchFilter.condition,
          indices: vm.searchFilter.indices,
          from: (size * (page -1)),
          size: size
        };

        vm.loading = true;

        this.$http.post('/v1/word/search', params)
          .then(function(response) {

            let data = response.data;

            let hits = data.hits;
            let total = hits.total;
            let list = hits.hits;

            let words = [];

            list.forEach(function(s, i){
//              console.log(s, i)

              let obj = s._source;
              let sentence = {
                id: s._id,
                seq: params.from + (i + 1),
                index: s._index,
                surface: obj.surface,
                morphemes: obj.morphemes,
                weight: obj.weight
              };

              words.push(sentence)
            });


            vm.words.list = words;
            vm.words.total = total;
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
      isContains: function(target){
        let vm = this;

        if(typeof(target) === 'string'){
          return vm.word.indexOf(target) > -1;

        }else{
          let find = false;

//          if(vm.word.indexOf(vm.toKey(target)) > -1){
//            find = true
//          }

          return find;
        }
      },

      onFileSeleted (evt) {
        this.uploadFile = evt[0]
      },

      fileUpload: function(){
        let vm = this;
        let formData = new FormData();
        formData.append('uploadFile', vm.uploadFile);
        formData.append('prefix', vm.prefix);
        formData.append('isAppend', vm.isAppend);

        vm.upload_loading = true;

        this.$http.post('/v1/word/upload', formData)
        .then(response => {
          vm.upload_loading = false;
          console.log('response', response);

          vm.errors = response.data;

          vm.$root.$refs.simplert.openSimplert({
            title: '등록 되었습니다.',
            type: 'info'
          });

//          debugger;

          vm.$refs.wordsIndexSelect.load();
//          vm.$root.$refs['job-executor'].$refs.wordsIndexSelect.load();
//          vm.$root.$router.replace({ path: 'words?t=1212' })
        }, response =>{
          vm.upload_loading = false;
        })
      },
      download: function(){
        location.href = '/static/sample.csv';
      },
    }
  }
</script>
