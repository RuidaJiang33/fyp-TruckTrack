<template>
  <div>
    <div style="box-shadow: 0 0 10px rgba(0,0,0,.1); padding: 10px 20px; border-radius: 5px; margin-bottom: 10px">
      Hello, {{ user.name }}
    </div>
    <div style="display: flex">
      <el-card style="width: 100%">
        <div>
          <span>TruckTrack: Empowering Trucking Management with Information Handling</span>
        </div>
      </el-card>
    </div>

    <div style="display: flex; margin: 15px 0">
      <el-card style="width: 50%; margin: 15px 0">
        <div style="margin-bottom: 15px; font-size: 20px; font-weight: bold;">System Notice</div>
        <el-collapse v-model="activeName" accordion>
          <el-collapse-item v-for="(item, index) in notices" :key="item.id" :name="index + ''">
            <template slot="title">
              <h4>{{ item.title }}</h4>
            </template>
            <div>{{ item.content }}</div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
      <el-card style="width: 50%; margin: 15px 15px">
        <div class="block">
          <el-timeline>
            <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :icon="activity.icon"
                :type="activity.type"
                :color="activity.color"
                :size="activity.size"
                :timestamp="activity.timestamp">
              {{activity.content}}
            </el-timeline-item>
          </el-timeline>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
export default {
  name: "Home",
  data() {
    return {
      user: JSON.parse(localStorage.getItem("test-user") || '{}'),
      notices: [],
      activeName: '0',
      activities: [{
        content: 'Web embedded map',
        timestamp: '2024-03-25',
        size: 'large',
        type: 'primary',
        icon: 'el-icon-more'
      }, {
        content: 'System notices and AOP system logs',
        timestamp: '2024-03-15',
        color: '#0bbd87'
      }, {
        content: 'Web chart data display',
        timestamp: '2024-03-10',
        color: '#0bbd87'
      }, {
        content: 'Excel import and export',
        timestamp: '2024-02-29',
        color: '#0bbd87'
      }, {
        content: 'JWT token permission authentication',
        timestamp: '2024-02-23',
        color: '#0bbd87'
      }, {
        content: 'Background data management',
        timestamp: '2024-02-20',
        color: '#0bbd87'
      }, {
        content: 'User information and password retrieval',
        timestamp: '2024-02-15',
        color: '#0bbd87'
      }, {
        content: 'Multi-role login, registration',
        timestamp: '2024-02-12',
        color: '#0bbd87'
      }, ]
    }
  },
  created() {
    this.loadNotice()
  },
  methods: {
    loadNotice() {
      this.$request.get('/notice/selectUserData').then(res => {
        this.notices = res.data
      })
    }
  }
}
</script>

<style>
.el-submenu__title:hover i {
  color: #fff !important;
}

.el-menu-item.is-active i,
.el-menu-item.is-active .el-tooltip {
  margin-left: -4px;
}

.el-submenu .el-menu-item {
  min-width: 0 !important;
}

.el-menu--inline .el-menu-item.is-active {
  padding-left: 45px;
}

</style>