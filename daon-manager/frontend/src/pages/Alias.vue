<template>
  <page-content page-title="말뭉치 Alias 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>
				<md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">말뭉치 Alias 관리</h1>
            </md-toolbar>


            <div class="analyzed-text">

              <span class="md-title">Corpus 검색 대상 말뭉치 alias</span>

              <md-input-container>
                <label for="sentences">sentences alias</label>
                <md-select name="sentences" id="sentences" required multiple v-model="sentences" @change="change">

                  <md-option v-for="option in options" :key="option.seq" :value="option.name">{{option.name}}</md-option>
                </md-select>
              </md-input-container>


              <span class="md-title">학습 대상 말뭉치 alias</span>

              <md-input-container>
                <label for="train_sentences">train_sentences alias</label>
                <md-select name="train_sentences" id="train_sentences" required multiple v-model="train_sentences" @change="change">
                  <md-option v-for="option in options" :key="option.seq" :value="option.name">{{option.name}}</md-option>
                </md-select>
              </md-input-container>


              <span class="md-title">테스트 대상 말뭉치 alias</span>

              <md-input-container>
                <label for="test_sentences">test_sentences alias</label>
                <md-select name="test_sentences" id="test_sentences" required multiple v-model="test_sentences" @change="change">
                  <md-option v-for="option in options" :key="option.seq" :value="option.name">{{option.name}}</md-option>
                </md-select>
              </md-input-container>

            </div>
          </md-table-card>
				</md-layout>
				<md-layout md-flex="80" md-gutter>
				</md-layout>
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    data : function(){
      return {
        options: [],
        sentences: [],
        train_sentences: [],
        test_sentences: []
      }
    },
    mounted: function(){
      this.load();
    },
    methods : {

      load: function(){
        this.options = [
          {seq:0, name: 'sejong_train_sentences_v3'},
          {seq:1, name: 'sejong_test_sentences_v3'},
          {seq:2, name: 'niadic_sentences_v3'}
        ];
        this.sentences = ['sejong_train_sentences_v3','sejong_test_sentences_v3','niadic_sentences_v3'];
        this.train_sentences = ['sejong_train_sentences_v3','niadic_sentences_v3'];
        this.test_sentences = ['sejong_test_sentences_v3'];

      },

      change: function(model){
        console.log('change', model);
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
