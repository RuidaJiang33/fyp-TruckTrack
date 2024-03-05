import Vue from 'vue'
import VueRouter from 'vue-router'

const originalPush = VueRouter.prototype.push
VueRouter.prototype.push = function push (location) {
  return originalPush.call(this, location).catch(err => err)
}

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Manager',
    component: () => import('../views/Admin.vue'),
    redirect: '/home', // 重定向到主页
    children: [
      { path: 'home', name: 'Home', meta: { name: ''}, component: () => import('../views/admin/Home') },
      { path: 'order', name: 'Order', meta: { name: 'Order Management'}, component: () => import('../views/admin/Order') },
      { path: 'user', name: 'User', meta: { name: 'User Management'}, component: () => import('../views/admin/User') },
      { path: 'truck', name: 'Truck', meta: { name: 'Truck Management'}, component: () => import('../views/admin/Truck') },
      { path: 'map', name: 'Map', meta: { name: 'Map Page'}, component: () => import('../views/admin/Map.vue') },
      { path: 'report', name: 'Report', meta: { name: 'Report Page'}, component: () => import('../views/admin/Report.vue')},
      { path: 'person', name: 'Person', meta: { name: 'User Information'}, component: () => import('../views/admin/Person.vue')},
      { path: 'password', name: 'Password', meta: { name: 'Change Password'}, component: () => import('../views/admin/Password.vue')},
    ]
  },
  { path: '/login', name: 'Login', meta: { name: 'Login'}, component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', meta: { name: 'Register'}, component: () => import('../views/Register.vue') },
  { path: '*', name: '404',  meta: { name: 'Not Found'}, component: () => import('../views/404.vue') }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

router.beforeEach((to, from, next) => {
  /* to: 到达的路由信息
   * from: 来源的路由信息
   * next: 用于跳转路由的函数
   */
  let adminPaths = ['/user']
  let user = JSON.parse(localStorage.getItem('test-user') || '{}')

  if (user.role !== 'admin' && adminPaths.includes(to.path)) {
    next('/home')
  }
  next()
})

export default router
