<template>
  <page-content page-title="말뭉치 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>
				<md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">말뭉치 검색 필터</h1>
            </md-toolbar>

            <div class="analyzed-text">
							<form novalidate @submit.stop.prevent="submit">
								<md-input-container>
									<label>문장 (like)</label>
									<md-input v-model="searchText"></md-input>
								</md-input-container>

                <md-chip v-for="keyword in searchKeywords" md-deletable>
                  <keyword :keyword="keyword"></keyword>
                </md-chip>

                <md-input-container>
                  <label>단어</label>
                  <md-input v-model="searchText"></md-input>
                </md-input-container>

							</form>
            </div>
          </md-table-card>
				</md-layout>
				<md-layout md-flex="80" md-gutter>
				  <corpus :sentences="[]" ></corpus>
				</md-layout>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    name: 'analyze',
    data : function(){
      return {
        text: this.$route.query.text || '',
        loading: false,
        terms: [],
        corpus: [],
        sentences: [],
        searchSeqs: [],
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        },
        searchKeywords: [
        	{seq:10, word:'테스트', tag:'NNG'}
        ]
      }
    },
    methods : {
      onCheck: function(){
        this.pagination.page = 1
        this.search()
      },
      search: function () {

        let seqs = []

        let vm = this

        vm.searchSeqs = seqs

        let params = {
          seq: seqs,
          from: (vm.pagination.size * (vm.pagination.page -1)),
          size: vm.pagination.size
        }

        console.log('params', params)

        this.$http.get('/v1/corpus/search{?seq}', {params : params})
          .then(function(response) {

            let data = response.data

            let hits = data.hits
            let total = hits.totalHits
            let list = hits.hits

            let sentences = []

            list.forEach(function(s, i){
              console.log(s, i)

              let obj = s.source
              let sentence = {
                id: s.id,
                seq: params.from + (i + 1),
                sentence: obj.sentence,
                eojeols: obj.eojeols
              }

              sentences.push(sentence)
            })

            vm.sentences = sentences

            vm.total = total
          })
      },
      onPagination: function(obj){
        console.log('page', obj)

        if(obj){
          this.pagination.size = obj.size
          this.pagination.page = obj.page
          this.search()
        }
      },
      isContains: function(target){

        let seqs = this.searchSeqs

        if(Array.isArray(target)){

          let find = false
          target.forEach(function(keyword){
            if(seqs.indexOf(keyword.seq) > -1){
              find = true
            }
          })

          return find
        }else{
          return seqs.indexOf(target) > -1
        }
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
</style>
