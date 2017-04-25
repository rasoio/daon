import Introduction from '../pages/Introduction';
import Analyze from '../pages/Analyze';
import Sentence from '../pages/Sentence';

const main = [
  {
    path: '/',
    name: 'introduction',
    component: Introduction
  },
  {
    path: '/analyze',
    name: 'analyze',
    component: Analyze
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

const corpus = [
  {
    path: '/corpus',
    name: 'corpus',
    redirect: '/corpus/sentences'
  },
  {
    path: '/corpus/sentences',
    name: 'corpus:sentences',
    component: Sentence
  },
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

export default [].concat(main, corpus);
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
