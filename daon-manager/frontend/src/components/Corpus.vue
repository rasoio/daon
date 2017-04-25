<template>
  <md-layout class="corpus-results">
    <md-table-card class="analyze-card-table">
      <md-toolbar>
        <h1 class="md-title">말뭉치 검색 결과</h1>
        <small v-show="total > 0">{{total}} 건</small>
      </md-toolbar>

      <md-table>
        <md-table-header>
          <md-table-row>
            <md-table-head width="10">No.</md-table-head>
            <md-table-head width="*">sentence</md-table-head>
          </md-table-row>
        </md-table-header>

        <md-table-body>

          <md-table-row v-for="sentence in sentences" :key="sentence.id">
            <md-table-cell md-numeric>{{ sentence.seq }}</md-table-cell>
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
          </md-table-row>
          <md-table-row v-if="sentences.length === 0">
            <md-table-cell colspan="2">검색 결과가 없습니다.</md-table-cell>
          </md-table-row>
        </md-table-body>
      </md-table>

      <md-table-pagination
        :md-size="pagination.size"
        :md-page="pagination.page"
        :md-total="pagination.total"
        md-label="Sentences"
        md-separator="of"
        :md-page-options="[10, 20, 50]"
        @pagination="onPagination"></md-table-pagination>
    </md-table-card>

    <md-button class="md-fab md-fab-bottom-right" id="fab" @click.native="openDialog('dialog2')">
      <md-icon>add</md-icon>
      <!--<md-tooltip md-direction="left">add</md-tooltip>-->
    </md-button>





    <md-dialog md-open-from="#fab" md-close-to="#fab" ref="dialog2">
      <md-dialog-title>Create new note</md-dialog-title>

      <md-dialog-content>
        <form>
          <md-input-container>
            <label>문장</label>
            <md-textarea></md-textarea>
          </md-input-container>
        </form>
      </md-dialog-content>

      <md-dialog-actions>
        <md-button class="md-primary" @click.native="closeDialog('dialog2')">Cancel</md-button>
        <md-button class="md-primary" @click.native="closeDialog('dialog2')">Create</md-button>
      </md-dialog-actions>
    </md-dialog>
  </md-layout>




</template>



<script>
  export default {
    name: 'corpus',
    props: ['sentences', 'page'],
    data : function(){
      return {
        text: this.$route.query.text || '',

        filter : {
          seqs: [],
          text: ''
        },
        total: 0,
        pagination: {
          size: 10,
          page: 1,
          total: 'Many'
        }
      }
    },
    methods : {
      openDialog(ref) {
        this.$refs[ref].open();
      },
      closeDialog(ref) {
        this.$refs[ref].close();
      },
      onOpen() {
        console.log('Opened');
      },
      onClose(type) {
        console.log('Closed', type);
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
