<template>
  <page-content page-title="말뭉치 관리">
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
                  <md-input-container>
                    <label>검색 대상</label>
                    <index-select filter="sentences" name="indexName" id="indexName" v-model="searchFilter.indices" :multiple="true"></index-select>
                  </md-input-container>
                  <div>
                    <label>검색 조건</label>
                    <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'like';search()" id="condition-like" name="condition" md-value="like">Like</md-radio>
                    <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'equals';search()" id="condition-equals" name="condition" md-value="equals">Equals</md-radio>
                    <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'prefix';search()" id="condition-prefix" name="condition" md-value="prefix">Prefix</md-radio>
                    <md-radio v-model="searchFilter.condition" @change="searchFilter.condition = 'suffix';search()" id="condition-suffix" name="condition" md-value="suffix">Suffix</md-radio>
                  </div>
                  <md-input-container>
                    <label>문장 검색어</label>
                    <md-input v-model="searchFilter.sentence" @keyup.enter.native="search"></md-input>
                  </md-input-container>
                  <md-input-container>
                    <label>어절 검색어</label>
                    <md-input v-model="searchFilter.eojeol" @keyup.enter.native="search"></md-input>
                  </md-input-container>
                </md-layout>
              </form>

            </md-table-card>
          </md-layout>
        </md-layout>
        <md-layout md-flex="80" md-gutter>
          <sentence-list :search-filter="searchFilter" ref="sentenceList"></sentence-list>
        </md-layout>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  import MdLayout from "../../node_modules/vue-material/src/components/mdLayout/mdLayout.vue";

  export default {
    components: {MdLayout},
    data : function(){
      return {
        text: this.$route.query.text || '',
        searchKeywords: [],
        searchFilter: {
          checkTerms: [],
          sentence: '',
          eojeol: '',
          condition: 'like',
          indices: []
        }
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
    		this.$refs.sentenceList.search();
      },

    }
  }
</script>
