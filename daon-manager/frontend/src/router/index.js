import Introduction from '../pages/Introduction';
import Analyze from '../pages/Analyze';
import Corpus from '../pages/Corpus';
import Dictionary from '../pages/Dictionary';

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
  {
    path: '/corpus',
    name: 'corpus',
    component: Corpus
  },
  {
    path: '/dictionary',
    name: 'dictionary',
    component: Dictionary
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
