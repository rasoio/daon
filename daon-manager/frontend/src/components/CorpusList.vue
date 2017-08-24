<template>
  <md-layout class="corpus-results">
    <md-table-card class="analyze-card-table">
      <md-progress v-show="loading" md-indeterminate></md-progress>

      <md-toolbar>
        <h1 class="md-title">
          말뭉치 검색 결과
          <small v-show="sentences.total > 0">( {{sentences.total}} ) 건</small>
        </h1>

        <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog">
          <md-icon>add</md-icon>
        </md-button>
      </md-toolbar>

      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head width="10">No.</md-table-head>
            <md-table-head width="*">sentence</md-table-head>
            <md-table-head width="10">button</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>
          <md-table-row v-for="sentence in sentences.list" :key="sentence.seq">
            <md-table-cell md-numeric>
              {{ sentence.seq }}
            </md-table-cell>
            <md-table-cell>
              <md-layout md-column md-gutter >
                <md-layout>
                  <span>
                    {{ sentence.index }}
                  </span>
                </md-layout>
                <md-layout>
                  <span class="md-title">
                    {{ sentence.sentence }}
                  </span>
                </md-layout>
                <!--<hr/>-->
                <md-layout v-for="eojeol in sentence.eojeols" :key="eojeol.seq">
                  <span class="md-subheading " :class="{ highlight: isContains(eojeol.surface) }" >
                    {{eojeol.surface}} :
                  </span>
                  <div v-for="morpheme in eojeol.morphemes" :key="morpheme.seq">
                    <span>&nbsp;</span>
                    <keyword :keyword="morpheme" :class="{ highlight: isContains(morpheme) }" theme="round"></keyword>
                  </div>
                </md-layout>

              </md-layout>
            </md-table-cell>
            <md-table-cell>
              <md-button md-theme="white" class="md-fab md-mini" @click.native="openDialog(sentence.id, sentence.index)">
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

    <corpus-form ref="corpusForm"></corpus-form>

  </md-layout>

</template>



<script>

  export default {
    name: 'corpusList',
    props: {
      searchFilter: {
        type: Object,
        default: function () {
          return {
            checkTerms: [],
            sentence: '',
            eojeol: ''
          }
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
        surfaces:[],
        keywords:[]
      }
    },
    methods : {
      openDialog: function(id, index) {
        this.$refs.corpusForm.openDialog(id, index);
      },
      search: function (size = 10, page = 1) {

        let vm = this;
        let checkTerms = vm.searchFilter.checkTerms;
        let sentence = vm.searchFilter.sentence;
        let eojeol = vm.searchFilter.eojeol;

        let params = {
          checkTerms: checkTerms,
          sentence: sentence,
          eojeol: eojeol,
          from: (size * (page -1)),
          size: size
        };

//        console.log('params', params);

        //체크 된 조건이 없는 경우 검색 안함.
        if(checkTerms.length === 0 && !sentence && !eojeol){
          vm.sentences.list = [];
          vm.sentences.total = 0;
          return;
        }

        vm.surfaces = this.searchFilter.checkTerms.filter(function(info){
          return info.surface;
        }).map(function(checkInfo){
          return checkInfo.surface
        });

        vm.keywords = this.searchFilter.checkTerms.filter(function(info){
          return info.keywords.length > 0;
        }).map(function(checkInfo){
          let wordTags = [];

          checkInfo.keywords.forEach(function(k){
            wordTags.push(vm.toKey(k))
          });
          return wordTags
        }).flatMap(x=>x);

        vm.loading = true;

        this.$http.post('/v1/corpus/search', params)
          .then(function(response) {

            let data = response.data;
//            console.log(data);

            let hits = data.hits;
            let total = hits.total;
            let list = hits.hits;

            let sentences = [];

            list.forEach(function(s, i){
//              console.log(s, i)

              let obj = s._source;
              let sentence = {
                id: s._id,
                seq: params.from + (i + 1),
                index: s._index,
                sentence: obj.sentence,
                eojeols: obj.eojeols
              };

              sentences.push(sentence)
            });


            vm.sentences.list = sentences;
            vm.sentences.total = total;

            vm.loading = false;

//            console.log(vm.sentences.list);

          }, function(response){
            vm.loading = false;
          })
      },
      toKey: function(keyword){
        return keyword.word + "/" + keyword.tag;
      },
      onPagination: function(obj){
        if(obj){
          this.search(Number(obj.size), Number(obj.page));
        }
      },
      isContains: function(target){
        let vm = this;

        if(typeof(target) === 'string'){
          return vm.surfaces.indexOf(target) > -1;

        }else{
          let find = false;

          if(vm.keywords.indexOf(vm.toKey(target)) > -1){
            find = true
          }

          return find;
        }
      }

    }

  }
</script>
