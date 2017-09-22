<template>
  <md-select :name="name" :id="id" v-model="sentence" :disabled="disabled" :multiple="multiple" @change="update">
    <md-option v-for="o in options" :key="o.name" :value="o.name">{{o.name}}</md-option>
  </md-select>
</template>

<script>

export default {
  name: 'sentenceSelect',
  props: {
    name: String,
    id: String,
    required: Boolean,
    multiple: Boolean,
    value: [String, Boolean, Number, Array],
    disabled: Boolean,
  },
  data () {
    return {
      options: [],
      sentence:''
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

          vm.options = data.sentences;

          vm.sentence = vm.value;
        });
    },
    update: function(){

      this.$emit('input', this.sentence);
    }

  }
}
</script>
