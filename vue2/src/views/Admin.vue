<template>
  <div class="home">
    <el-container>
      <!-- 侧边栏 -->

      <el-aside :width="asideWidth" style="min-height: 100vh; background-color: #001529;">
        <div style="height: 60px; color: white; display: flex; align-items: center; justify-content: center">
          <img src="@/assets/truck.png" alt="" style="width: 30px; height: 30px;">
          <transition name="el-fade-in-linear">
            <span class="logo-title" v-show="!isCollapse">TruckTrack</span>
          </transition>
        </div>

        <el-menu :collapse="isCollapse" :collapse-transition="false" router background-color="#001529"
          text-color="rgba(255, 255, 255, 0.65)" active-text-color="#fff" style="border: none"
          :default-active="$route.path">
          <el-menu-item index="/home">
            <i class="el-icon-s-home"></i>
            <span slot="title">TruckTrack</span>
          </el-menu-item>
          <el-submenu index="info" :default-active="$route.path">
            <template slot="title">
              <i class="el-icon-menu"></i>
              <span>Dashboard</span>
            </template>
            <el-menu-item index="/map">
              <i class="el-icon-house"></i>
              <span slot="title">Map</span>
            </el-menu-item>
            <el-menu-item index="/report">
              <i class="el-icon-house"></i>
              <span slot="title">Report</span>
            </el-menu-item>
          </el-submenu>
          <el-submenu index="info" :default-active="$route.path" v-if="user.role === 'admin'">
            <template slot="title">
              <i class="el-icon-menu"></i>
              <span>Management</span>
            </template>
            <el-menu-item index="/order">
              <i class="el-icon-house"></i>
              <span slot="title">Order</span>
            </el-menu-item>
            <el-menu-item index="/truck">
              <i class="el-icon-house"></i>
              <span slot="title">Truck</span>
            </el-menu-item>
            <el-menu-item index="/user">
              <i class="el-icon-house"></i>
              <span slot="title">User</span>
            </el-menu-item>
          </el-submenu>

        </el-menu>
      </el-aside>

      <el-container>
        <el-header>
          <i :class="collapseIcon" style="font-size: 26px;" @click="handleCollapse"></i>
          <el-breadcrumb separator="/" style="margin-left: 20px;">
            <el-breadcrumb-item :to="{ path: '/' }">Main</el-breadcrumb-item>
            <el-breadcrumb-item :to="{ path: $route.path }">{{ $route.meta.name }}</el-breadcrumb-item>
          </el-breadcrumb>

          <div style="flex: 1; width: 0; display: flex; align-items: center; justify-content: flex-end;">
            <el-dropdown placement="bottom">
              <div style="display: flex; align-items: center; cursor: default;">
                <img src="@/assets/user.png" alt="" style="width: 30px; height: 30px;">
                <span>{{ user.name }}</span>
              </div>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item @click.native="$router.push('/person')">User Information</el-dropdown-item>
                <el-dropdown-item @click.native="$router.push('/password')">Change Password</el-dropdown-item>
                <el-dropdown-item @click.native="logout">Log Out</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <el-main>
          <router-view @update:user="updateUser"/>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import request from '@/utils/request';

export default {
  name: 'Manager',
  data() {
    return {
      isCollapse: false,
      asideWidth: '240px',
      collapseIcon: 'el-icon-s-fold',
      user: JSON.parse(localStorage.getItem("test-user") || '{}')
    }
  },
  mounted(){ // 页面加载完成之后触发

  },
  methods: {
    updateUser(user) { // 获取子组件传过来的数据，更新当前页面的数据
      this.user = JSON.parse(JSON.stringify(user)) // 让父级的对象和子级的对象分开
    },
    handleCollapse() {
      this.isCollapse = !this.isCollapse;
      this.asideWidth = this.isCollapse ? '64px' : '230px';
      this.collapseIcon = this.isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'
    },
    logout() {
      localStorage.removeItem('test-user') // 清除当前的token和用户数据
      this.$router.push('/login')
    }
  }
}
</script>

<style>
.el-menu--inline .el-menu-item {
  background-color: #000c17 !important;
  padding-left: 49px !important;
}

.el-menu-item:hover,
.el-submenu__title:hover {
  color: #fff !important;
}

.el-submenu__title:hover i {
  color: #fff !important;
}

.el-menu-item.is-active {
  background-color: #40a9ff !important;
  border-radius: 5px !important;
  width: calc(100%-8px);
  margin-left: 4px;
  margin-right: 4px;
}

.el-menu-item.is-active i,
.el-menu-item.is-active .el-tooltip {
  margin-left: -4px;
}

.el-menu-item {
  height: 40px !important;
  line-height: 40px !important;
}

.el-submenu__title {
  height: 40px !important;
  line-height: 40px !important;
}

.el-submenu .el-menu-item {
  min-width: 0 !important;
}

.el-menu--inline .el-menu-item.is-active {
  padding-left: 45px;
}

.el-aside {
  transition: width .3s;
  box-shadow: 2px 0 6px rgba(0, 21, 41, .35);
}

.el-header {
  display: flex;
  align-items: center;
  box-shadow: 2px 0 6px rgba(0, 21, 41, .35);
}

/* .el-submenu__icon-arrow {
    margin-top: -5px;
  } */
.logo-title {
  margin-left: 5px;
  font-size: 16px;
  transition: all .3s;
}
</style>
