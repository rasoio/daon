<template>
  <page-content page-title="말뭉치 Alias 관리">
    <div class="main-content">

      <md-layout md-column md-gutter>
				<md-layout md-flex="20" md-gutter>
          <md-table-card class="analyze-card-table">
            <md-toolbar>
              <h1 class="md-title">말뭉치 Alias 관리</h1>
                <md-button class="md-raised md-primary" @click.native="save">저장</md-button>
            </md-toolbar>


            <div class="analyzed-text">

              <div class="md-title">Corpus 검색 대상 말뭉치 </div>
              <div>
                <md-checkbox v-for="option in sentences"  :id="'sentences_' + option.name" name="sentences" v-model="option.exist">{{option.name}}</md-checkbox>
              </div>

              <span class="md-title">학습 대상 말뭉치</span>
              <div>
                <md-checkbox v-for="option in train_sentences"  :id="'train_sentences_' + option.name" name="train_sentences" v-model="option.exist">{{option.name}}</md-checkbox>
              </div>

              <span class="md-title">테스트 대상 말뭉치</span>
              <div>
                <md-checkbox v-for="option in test_sentences"  :id="'test_sentences_' + option.name" name="test_sentences" v-model="option.exist">{{option.name}}</md-checkbox>
              </div>

            </div>
          </md-table-card>

          <simplert :useRadius="true"
                    :useIcon="true"
                    ref="simplert">
          </simplert>
				</md-layout>
				<!--<md-layout md-flex="80" md-gutter>-->
				<!--</md-layout>-->
      </md-layout>
    </div>
  </page-content>
</template>



<script>
  export default {
    data : function(){
      return {
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

        let vm = this;
        this.$http.get('/v1/alias/get')
          .then(function(response) {

            let data = response.data;

            vm.sentences = data.sentences;
            vm.train_sentences = data.train_sentences;
            vm.test_sentences = data.test_sentences;

          });
      },

      save: function(){
        let vm = this;

        let params = {
          sentences: vm.sentences,
          train_sentences: vm.train_sentences,
          test_sentences: vm.test_sentences
        };

        this.$http.post('/v1/alias/save', params)
          .then(function(response) {

            let isSuccess = response.data;

            if(isSuccess){
							vm.$refs.simplert.openSimplert({
								title: 'Alias 적용',
								message: '완료되었습니다.',
								type: 'info'
							});

            }else{
              vm.$refs.simplert.openSimplert({
                title: 'Alias 적용',
                message: '에러가 발생했습니다.',
                type: 'error'
              });
            }
          });

      }

    }
  }
</script>
