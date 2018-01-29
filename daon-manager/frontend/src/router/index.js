import Introduction from '../pages/Introduction.vue';
import GettingStarted from '../pages/GettingStarted.vue';
import Analyze from '../pages/Analyze.vue';
import Sentences from '../pages/Sentences.vue';
import Words from '../pages/Words.vue';
import Tags from '../pages/Tags.vue';
import Alias from '../pages/Alias.vue';
import Model from '../pages/Model.vue';

const main = [
  {
    path: '/',
    name: 'introduction',
    component: Introduction
  },
  {
    path: '/getting-started',
    name: 'getting-started',
    component: GettingStarted
  },
  {
    path: '/analyze',
    name: 'analyze',
    component: Analyze
  },
  {
    path: '/sentences',
    name: 'sentences',
    component: Sentences
  },
  {
    path: '/words',
    name: 'words',
    component: Words
  },
  {
    path: '/tags',
    name: 'tags',
    component: Tags
  },
  {
    path: '/alias',
    name: 'alias',
    component: Alias
  },
  {
    path: '/model',
    name: 'model',
    component: Model
  },
  // {
  //   path: '/about',
  //   name: 'about',
  //   component: About
  // },
  // {
  //   path: '/changelog',
  //   name: 'changelog',
  //   component: Changelog
  // }
];

//
// const theme = [
//   {
//     path: '/themes',
//     name: 'themes',
//     redirect: '/themes/configuration'
//   },
//   {
//     path: '/themes/configuration',
//     name: 'themes:configuration',
//     component: Configuration
//   },
//   {
//     path: '/themes/dynamic-themes',
//     name: 'themes:dynamic-themes',
//     component: DynamicThemes
//   }
// ];
//
// const uiElements = [
//   {
//     path: '/ui-elements',
//     name: 'ui-elements',
//     redirect: '/ui-elements/typography'
//   },
//   {
//     path: '/ui-elements/typography',
//     name: 'ui-elements:typography',
//     component: Typography
//   },
//   {
//     path: '/ui-elements/layout',
//     name: 'ui-elements:layout',
//     component: Layout
//   }
// ];
//
// const error = [
//   {
//     path: '*',
//     name: 'error',
//     component: Error404
//   }
// ];

export default [].concat(main);
// export default [].concat(main, components, theme, uiElements, error);


// export default new Router({
//   routes: [
//     {
//       path: '/',
//       name: 'App',
//       component: App
//     }
//   ]
// })
