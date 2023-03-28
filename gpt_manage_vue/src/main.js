import Vue from 'vue'
import App from './App.vue'
import Router from "vue-router";
Vue.use(Router);
import './assets/download/font_tgwy4rgxsxn/iconfont.css'
import 'element-ui/lib/theme-chalk/index.css';
import '@wangeditor/editor/dist/css/style.css';
import mavonEditor from 'mavon-editor'
import 'mavon-editor/dist/css/index.css'
// use
Vue.use(mavonEditor)
Vue.config.productionTip = false
const routes = [
  {
    path: "",
    component: () => import("@/views/App"),
    name: "Home",
    props: { isHomePage: true }
  },
  {
    path: "/save",
    component: () => import("@/views/save"),
    name: "保存问题",
  }
]
const createRouter = () =>
  new Router({
    mode: 'history', // require service support
    scrollBehavior: () => ({ y: 0 }),
    routes
  });
const router = createRouter();
// element-plus 全局引用
import Element from "element-ui";
Vue.use(Element, {
  //size: Cookies.get("size") || "medium" // set element-ui default size
  size: "mini"
});
new Vue({
  el:"#app",
  router,
  render: h => h(App)
})