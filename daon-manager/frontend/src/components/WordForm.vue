<template>

    <md-dialog ref="wordDialog">
      <md-dialog-title>단어 {{title}}</md-dialog-title>

      <md-dialog-content>
        <form>
          <md-layout md-column>
            <md-layout md-flex="20" v-show="isNew">
              <md-input-container>
                <label>Index</label>
                <index-select filter="words" name="indexName" id="indexName" v-model="index"></index-select>
              </md-input-container>
            </md-layout>
            <md-layout md-flex="20" v-show="!isNew">
              <span class="md-subheading">
                Index : {{index}}
              </span>
            </md-layout>
            <md-layout md-flex="20">
							<md-input-container>
								<label>표층형</label>
								<md-input v-model="surface" @input.native="analyze"></md-input>
                <div style="text-align: right">
                  <md-button class="md-raised md-primary" @click.native="analyze">표층형 분석</md-button>
                </div>
							</md-input-container>
            </md-layout>
            <md-layout>
							<md-input-container>
								<label>분석결과</label>
								<md-input v-model="morphemes"></md-input>
							</md-input-container>
            </md-layout>
            <md-layout>
							<md-input-container>
								<label>weight</label>
								<md-input v-model="weight"></md-input>
							</md-input-container>
            </md-layout>
          </md-layout>

        </form>
      </md-dialog-content>

      <md-dialog-actions>
        <md-button class="md-primary" @click.native="closeDialog('wordDialog')">취소</md-button>
        <md-button class="md-primary" @click.native="save()">저장</md-button>
      </md-dialog-actions>
    </md-dialog>

</template>



<script>
  export default {
    name: 'wordForm',
    props: {},
    data : function(){
      return {
        title:'신규',
        id:'',
        index:'',
        surface:'',
        morphemes:'',
        weight:1,
        isNew:true
      }
    },
    methods : {
      analyze: function (e) {
        let vm = this;

        if(e.target.value){
          vm.surface = e.target.value;
        }

        if(!vm.surface){
          return;
        }

        this.$http.post('/v1/analyze/text', vm.surface)
          .then(function(response) {
            vm.morphemes = vm.toStringAnalyzed(response.data);
          })
      },
      openDialog: function(id, index, ref='wordDialog') {

      	let vm = this;

      	if(id && index){
      		let params = {id : id, index: index};
      		vm.id = id;
      		vm.index = index;
      		vm.title = '수정';
      		vm.isNew = false;
          this.$http.get('/v1/word/get', {params : params})
            .then(function(response) {

            	let word = response.body;
            	vm.surface = word.surface;
            	vm.morphemes = vm.toString(word.morphemes);
            	vm.weight = word.weight;

//            	console.log(response);
              vm.$refs[ref].open();
            })
        }else{
          vm.title = '신규';
          vm.id = '';
          vm.index = '';
          vm.surface = '';
          vm.morphemes = '';
          vm.weight = 1;
          vm.isNew = true;
          vm.$refs[ref].open();
        }
      },

      toStringAnalyzed: function(analyzed){
//        console.log(JSON.stringify(analyzed, null, 2));

        let morph = [];

        analyzed.forEach(function(e){
          e.terms.map(x=>x.keywords).flatMap(x=>x).forEach(function(m){
            morph.push(m.word + "/" + m.tag);
          });

        });
        return morph.join(" ");
      },
      toString: function(morphemes){
//        console.log(JSON.stringify(eojeols, null, 2));

        /**
         * 어절 구분 : 줄바꿈 문자
         * 어절-형태소 간 구분 : ' - '
         * 형태소 간 구분 : 공백(스페이스) 문자
         * 형태소 내 단어-태그 구분 : '/'
         * @type {Array}
         */

        let morph = [];
        morphemes.forEach(function(m){
          morph.push(m.word + "/" + m.tag);
        });

        return morph.join(" ");
      },
      closeDialog: function(ref='wordDialog') {
        this.$refs[ref].close();
      },

      save: function(){
        let vm = this;

        let params = {
          id: vm.id,
          index: vm.index,
          surface: vm.surface,
          morphemes: vm.morphemes,
          weight: vm.weight
        };

        console.log('save params', params);

        this.$http.post('/v1/word/save', params)
          .then(function(response) {
//            console.log(response);

            vm.$root.$refs.simplert.openSimplert({
              title: '저장 되었습니다.',
              type: 'info'
            });

            vm.closeDialog('wordDialog');
          })
      }

    }


  }
</script>

<style lang="scss">

  /*.md-textarea { height: 300px; }*/
</style>
