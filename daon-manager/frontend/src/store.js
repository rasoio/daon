import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

// root state object.
// each Vuex instance is just a single state tree.
const state = {
  progress: 0,
  running: false,
  elapsedTime: 0
};

// mutations are operations that actually mutates the state.
// each mutation handler gets the entire state tree as the
// first argument, followed by additional payload arguments.
// mutations must be synchronous and can be recorded by plugins
// for debugging purposes.
const mutations = {
  update (state, { data }) {
    // console.log('mutations', data);
    state.progress = data.progress;
    state.running = data.running;
    state.elapsedTime = data.elapsedTime;
  }
};


// A Vuex instance is created by combining the state, mutations, actions,
// and getters.
export default new Vuex.Store({
  state: state,
  mutations: mutations
})
