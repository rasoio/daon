<template>
  <md-select :name="name" :id="id" v-model="indices" :disabled="disabled" :multiple="multiple" @change="update" @closed="close">
    <md-option v-if="all">전체</md-option>
    <md-option v-for="o in options" :key="o.name" :value="o.name">{{o.name}}</md-option>
  </md-select>
</template>

<script>

export default {
  name: 'indexSelect',
  props: {
    name: String,
    id: String,
    filter: String,
    required: Boolean,
    value: [String, Boolean, Number, Array],
    all: Boolean,
    multiple: Boolean,
    disabled: Boolean,
    alias: String,
    save: Boolean,
  },
  data () {
    return {
      options: [],
      indices: []
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

          vm.options = data.filter(index=>{
            let name = index.name;

            return name.indexOf(vm.filter) > -1;
          });

          if(vm.save) {
            //check alias
            vm.options.forEach((o) => {
              if (o.alias && o.alias.includes(vm.alias)) {
                vm.indices.push(o.name);
//              console.log(vm.alias, o.alias, vm.indices);
              }
            })
          }

        });
    },
    update: function(){
      let vm = this;
//      console.log('change', vm.indices);
      this.$emit('input', vm.indices);
      this.$emit('change', vm.indices);
    },
    close: function(){
      let vm = this;
      if(vm.save){
        vm.saveAlias();
      }
    },
    saveAlias: function(){
      let vm = this;

      let options = vm.options.map(o=>{return o.name});

      let params = {
        alias: vm.alias,
        options: options,
        indices: vm.indices
      };

      this.$http.post('/v1/alias/save', params)
        .then(function(response) {

          let isSuccess = response.data;

          if(isSuccess){
            vm.$root.$refs.simplert.openSimplert({
              title: 'Alias 적용',
              message: '완료되었습니다.',
              type: 'info'
            });

          }else{
            vm.$root.$refs.simplert.openSimplert({
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
