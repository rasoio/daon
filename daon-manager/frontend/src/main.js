// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import VueRouter from 'vue-router';

/* Configs */
import './config.js';
import routes from './router';
import App from './App.vue';
import store from './store';

let Main = Vue.component('app', App);

//공통 interceptor
Vue.http.interceptors.push(function(request, next) {

  console.log(request);

  next(function(response) {

    console.log(response);

    if(!response.ok){
			let obj = {
        title: response.statusText + ' (' + response.status + ')',
        message: response.data.message,
        type: 'error'
			};
			Main.$refs.simplert.openSimplert(obj)
    }
  });
});


let router = new VueRouter({
  mode: 'hash',
  base: window.location.pathname,
  routes
});

let handleSectionTheme = (currentRoute) => {
  let theme = 'default';
  let name = currentRoute.name;

  if (name) {
    if (name === 'getting-started') {
      theme = 'indigo';
    } else if (name.indexOf('themes') >= 0) {
      theme = 'cyan';
    } else if (name.indexOf('ui-elements') >= 0) {
      theme = 'purple';
    } else if (name === 'changelog') {
      theme = 'orange';
    } else if (name === 'about') {
      theme = 'green';
    } else if (name === 'error') {
      theme = 'red';
    }
  }

  Vue.material.setCurrentTheme(theme);
};

Vue.config.productionTip = false;

Main = new Main({
  el: '#app',
  store,
  router
});


let onFailed = function(frame){

  Main.$refs.simplert.openSimplert({
    title: '모델생성',
    message: '메세지 수신 실패',
    type: 'error'
  });

  console.log('Failed', frame);
};
let headers = {};

let serverEndPoint = '/daon-websocket';

//개발 버전
if(Vue.config.devtools){
  serverEndPoint = 'http://localhost:5001/daon-websocket';
}

Main.connetWM(serverEndPoint, headers, function(frame){

  Main.$stompClient.debug = function(str){};

  let process = Main.$stompClient.subscribe("/model/progress", function(message){

    let data = JSON.parse(message.body);

    store.commit('update', {data:data});

    // console.log('progress', message);
  });

  let message = Main.$stompClient.subscribe("/model/message", function(message){
    let data = JSON.parse(message.body);

    let obj = {
      title: '모델생성',
      message: data.text,
      type: 'info'
    };

    Main.$refs.simplert.openSimplert(obj);

    if(data.type === 'END'){
      store.commit('update', {data:{
        progress: 0,
        running: false,
        elapsedTime: 0
      }});

      // console.log(router, Main.$refs)
			router.replace({path: '/model?t=' + (new Date).getTime()})
    }

    // console.log(obj);

    // console.log('message', message);
  });

}, onFailed);


Main.stompClient = {
  monitorIntervalTime: 10000,
  stompReconnect: false,
  timeout: function(orgCmd) {}
};

handleSectionTheme(router.currentRoute);

router.beforeEach((to, from, next) => {
  Vue.nextTick(() => {
    let mainContent = document.querySelector('.main-content');

    if (mainContent) {
      mainContent.scrollTop = 0;
    }

    Main.closeSidenav();

    next();
  });
});

router.afterEach((to) => {
  handleSectionTheme(to);
});

const test = "test!!";
