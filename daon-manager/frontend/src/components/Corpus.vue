<template>
  <md-layout class="corpus-results">
    <md-table-card class="analyze-card-table">
      <md-toolbar>
        <h1 class="md-title">
          말뭉치 검색 결과
          <small v-show="sentences.total > 0">( {{sentences.total}} ) 건</small>
        </h1>

        <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog('sentenceDialog')">
          <md-icon>add</md-icon>
        </md-button>
      </md-toolbar>

      <md-spinner v-show="loading" md-indeterminate></md-spinner>

      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head width="10">No.</md-table-head>
            <md-table-head width="*">sentence</md-table-head>
            <md-table-head width="10">button</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="sentence in sentences.list" :key="sentence.id">
            <md-table-cell md-numeric>
              {{ sentence.seq }}
            </md-table-cell>
            <md-table-cell>
              <md-layout md-column md-gutter >
                <md-layout>
                  <span class="md-title">
                    {{ sentence.sentence }}
                  </span>
                </md-layout>
                <!--<hr/>-->
                <md-layout v-for="eojeol in sentence.eojeols" :key="eojeol.seq">
                  <span class="md-subheading">
                    {{eojeol.surface}} :
                  </span>
                  <div v-for="morpheme in eojeol.morphemes" :key="morpheme.seq">
                    <span>&nbsp;</span>
                    <keyword :keyword="morpheme" :class="{ highlight: isContains(morpheme.seq) }" theme="round"></keyword>
                  </div>
                </md-layout>

              </md-layout>
            </md-table-cell>
            <md-table-cell>
              <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog('sentenceDialog', sentence.id)">
                <md-icon>edit</md-icon>
              </md-button>
            </md-table-cell>
          </md-table-row>
          <md-table-row v-if="sentences.total === 0">
            <md-table-cell colspan="3">검색 결과가 없습니다.</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table-pagination
        :md-size="pagination.size"
        :md-page="pagination.page"
        :md-total="pagination.total"
        md-label="Sentences"
        md-separator="of"
        :md-page-options="[10]"
        @pagination="onPagination"></md-table-pagination>

    </md-table-card>


    <md-dialog :md-fullscreen="true" ref="sentenceDialog">
      <md-dialog-title v-if="!sentence.sentence">말뭉치 추가</md-dialog-title>
      <md-dialog-title v-if="sentence.sentence">말뭉치 수정</md-dialog-title>

      <md-dialog-content>
        <form>
          <md-input-container>
            <label>문장</label>
            <md-textarea v-model="sentence.sentence"></md-textarea>
          </md-input-container>
          <md-layout v-for="eojeol in sentence.eojeols" :key="eojeol.seq">
                  <span class="md-subheading">
                    {{eojeol.surface}} :
                  </span>
            <div v-for="morpheme in eojeol.morphemes" :key="morpheme.seq">
              <span>&nbsp;</span>
              <keyword :keyword="morpheme" :class="{ highlight: isContains(morpheme.seq) }" theme="round"></keyword>
            </div>
          </md-layout>
        </form>
      </md-dialog-content>

      <md-dialog-actions>
        <md-button class="md-primary" @click.native="closeDialog('sentenceDialog')">취소</md-button>
        <md-button class="md-primary" @click.native="closeDialog('sentenceDialog')">저장</md-button>
      </md-dialog-actions>
    </md-dialog>
  </md-layout>

</template>



<script>
  export default {
    name: 'corpus',
    props: {
      searchFilter: {
        type: Object,
        default: function () {
          return { seqs: [], keyword: '' }
        },
        required: true
      },
    },
    data : function(){
      return {
        loading: false,
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        sentences: {
          list:[],
          total: 0,
        },
        sentence:{}
      }
    },
    methods : {
      openDialog(ref, id) {

      	let vm = this;
      	if(id){
      		let params = {id : id};
          this.$http.get('/v1/corpus/get{?id}', {params : params})
            .then(function(response) {

            	vm.sentence = response.body;

            	console.log(response);
              this.$refs[ref].open();
            })
        }else{
          vm.sentence = {};
          this.$refs[ref].open();
        }
      },
      closeDialog(ref) {
        this.$refs[ref].close();
      },
      search: function (size = 10, page = 1) {

        let vm = this;
        let seqs = vm.searchFilter.seqs;
        let keyword = vm.searchFilter.keyword;

        let params = {
          seq: seqs,
          keyword: keyword,
          from: (size * (page -1)),
          size: size
        };

        console.log('params', params);

        vm.loading = true;

        this.$http.get('/v1/corpus/search{?seq}', {params : params})
          .then(function(response) {

            let data = response.data;

            let hits = data.hits;
            let total = hits.totalHits;
            let list = hits.hits;

            let sentences = [];

            list.forEach(function(s, i){
//              console.log(s, i)

              let obj = s.source;
              let sentence = {
                id: s.id,
                seq: params.from + (i + 1),
                sentence: obj.sentence,
                eojeols: obj.eojeols
              };

              sentences.push(sentence)
            });

            vm.sentences.list = sentences;
            vm.sentences.total = total;

            vm.loading = false;
          })
      },
      onPagination: function(obj){
        if(obj){
          this.search(Number(obj.size), Number(obj.page));
        }
      },
      isContains: function(target){

        let seqs = this.searchFilter.seqs;

//        console.log(seqs, target);
        if(Array.isArray(target)){

          let find = false;
          target.forEach(function(keyword){
            if(seqs.indexOf(keyword.seq) > -1){
              find = true
            }
          });

          return find;
        }else{
          return seqs.indexOf(target) > -1;
        }
      }

    }


  }
</script>

<style lang="scss" scoped>

  .highlight {
    color: crimson;
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

  .md-dialog {
    min-width: 800px;
  }

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
</style>
