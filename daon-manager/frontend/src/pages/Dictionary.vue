<template>
  <page-content page-title="사전 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>
				<md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">사전 검색 필터</h1>

              <md-button md-theme="white" class="md-fab md-mini" @click.native="search">
                <md-icon>search</md-icon>
              </md-button>
            </md-toolbar>

            <div class="analyzed-text">
							<form novalidate @submit.stop.prevent="submit">
                <div>
                  <md-switch v-model="checked0" id="my-test1" name="my-test1" class="md-primary">완전 일치 결과만</md-switch>
                </div>
								<md-input-container>
									<label>검색어</label>
									<md-input v-model="searchFilter.keyword" @keyup.enter.native="search"></md-input>
								</md-input-container>

                <md-input-container>
                  <label>품사</label>
                  <md-input v-model="searchFilter.tag"></md-input>
                </md-input-container>
							</form>
            </div>
          </md-table-card>
				</md-layout>
				<md-layout md-flex="80" md-gutter>
				  <dictionary :search-filter="searchFilter" ref="dictionary"></dictionary>
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
        checked0: true,
      }
    },
    computed: {
      searchSeqs : function(){
      	return []
      }
    },
    methods : {

    	remove: function(obj){
    		console.log(obj);
      },

      search: function () {
    		this.$refs.dictionary.search();
      },

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
