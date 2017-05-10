<template>
  <span :class="theme">
  <!--<md-layout>-->
    ({{keyword.seq}}) {{ keyword.word }} &nbsp;<small>({{ keyword.tag | tagName}})</small>
    <!--<md-tooltip md-direction="top">{{ keyword.desc }} {{ keyword.tag | tagName('detail') }} - {{keyword.seq}}</md-tooltip>-->
  <!--</md-layout>-->
  </span>
</template>

<script>

export default {
  name: 'keyword',
  props: ['keyword', 'theme'],
  data () {
    return {

    }
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .round {
    border: 1px solid #a1a1a1;
    /*margin: 5px;*/
    padding: 2px 5px;
    /*background: #dddddd;*/
    /*width: 300px;*/
    border-radius: 5px;
  }
</style>
