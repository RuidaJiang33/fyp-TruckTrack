<template>
  <div class="Map">
    <div ref="MyMapContainer" id="MyMapContainer"></div>
    <div class="Selectbox">
      <el-select
          v-model="SelectValue"
          placeholder="Please choose"
          @change="ChangeMapType"
      >
        <el-option
            v-for="item in MapList"
            :key="item.layerId"
            :label="item.name"
            :value="item.layerId"
        >
        </el-option>
      </el-select>
    </div>
    <div class="button">
      <el-button @click="enterRouteSettingMode">Route setting</el-button>
      <el-button v-if="routeSettingMode" @click="handleAdd">Save</el-button>
      <el-button v-if="routeSettingMode" @click="exitRouteSettingMode">Finish</el-button>
    </div>
    <el-card v-if="selectedTruck" class="truck-info" :body-style="{ padding: '20px' }">
      <div slot="header" class="clearfix">
        <span style="font-weight: bold">License Plate: {{ selectedTruck.licensePlate }}</span>
        <el-button @click="showSendMessageDialog = true" style="float: right; padding: 3px 0" type="text">Message
        </el-button>
      </div>
      <div class="text item">Origin: {{ selectedTruck.origin }}</div>
      <div class="text item">Destination: {{ selectedTruck.destination }}</div>
      <div v-if="deviationDistance" class="text item">Deviation: {{ deviationDistance }} km</div>
      <div class="bottom clearfix">
        <time class="time">{{ currentDate }}</time>
      </div>
    </el-card>
    <el-dialog
        title="Message"
        :visible.sync="showSendMessageDialog"
        width="30%"
        @close="resetDialog">
      <el-input
          v-model="messageContent"
          type="textarea"
          :rows="2"
          placeholder="Content">
      </el-input>
      <span slot="footer" class="dialog-footer">
        <el-button @click="showSendMessageDialog = false">Cancel</el-button>
        <el-button type="primary" @click="sendMessage">Send</el-button>
      </span>
    </el-dialog>

    <el-dialog title="Route info" :visible.sync="FormVisible" width="40%">

      <el-form :model="form" label-width="120px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="RouteName" prop="routeName">
          <el-input v-model="form.routeName" placeholder="RouteName"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="FormVisible = false">Cancel</el-button>
        <el-button type="primary" @click="saveRoute">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import mapboxgl from "mapbox-gl";
import "mapbox-gl/dist/mapbox-gl.css";
import * as turf from '@turf/turf';
import MapboxWorker from "worker-loader!mapbox-gl/dist/mapbox-gl-csp-worker"; // Load worker code separately with worker-loader
mapboxgl.workerClass = MapboxWorker; // Wire up loaded worker to be used instead of the default
mapboxgl.accessToken = 'pk.eyJ1IjoiY2RrYTMzIiwiYSI6ImNscHU2dnBuNTBtM3Qya3F6YWNtbTQybXIifQ.Y1K9DYW4v8mqRWSZDVDV-A';
export default {
  name: "HomeViewPage",
  components: {},
  data() {
    return {
      MapList: [
        {name: "mapbox standard", layerId: "mapbox://styles/mapbox/standard"},
        {name: "mapbox navigation-day-v1", layerId: "mapbox://styles/mapbox/navigation-day-v1"},
        {name: "mapbox light-v11", layerId: "mapbox://styles/mapbox/light-v11"},
        {name: "mapbox dark-v11", layerId: "mapbox://styles/mapbox/dark-v11"},
        {name: "mapbox satellite", layerId: "mapbox://styles/mapbox/satellite-v9"},
      ],
      SelectValue: "mapbox://styles/mapbox/standard",
      TruckLocations: [],
      selectedTruck: null,
      currentDate: new Date().toISOString(),
      showSendMessageDialog: false,
      messageContent: '',
      currentRouteData: null,
      deviationDistance: null,
      refreshIntervalId: null,
      routeSettingMode: false,
      waypoints: [],
      optimizedRoute: null,
      markerSourceIds: [],
      FormVisible: false,
      routeName: '',
      form: {},
      rules: {
        routeName: [
          {required: true, message: 'Please enter the route name', trigger: 'blur'}
        ]
      },
    };
  },
  mounted() {
    this.$nextTick(() => {
      this.InitMap();
      this.fetchTruckLocations();
      this.refreshIntervalId = setInterval(this.fetchTruckLocations, 10000);
    });
  },
  beforeDestroy() {
    // 组件销毁前清除定时器
    if (this.refreshIntervalId) {
      clearInterval(this.refreshIntervalId);
    }
  },
  methods: {
    fetchTruckLocations() {
      this.$request.get('/truck/fetchTruckLocations').then(res => {
        this.TruckLocations = res.data;
        this.AddTruckIcon();
      }).catch(error => console.error('Failed to fetch truck locations:', error));
    },
    sendMessage() {
      // 这里发送消息到后端或进行其他处理
      console.log("Message sent: ", this.messageContent);
      // 发送成功后重置对话框并关闭
      this.resetDialog();
    },
    resetDialog() {
      this.showSendMessageDialog = false;
      this.messageContent = ''; // 重置消息内容
    },
    updateCurrentTime() {
      this.currentDate = new Date().toISOString();
    },
    InitMap() {
      this.map = new mapboxgl.Map({
        container: this.$refs.MyMapContainer,
        style: this.MapList.find(item => item.layerId === this.SelectValue).layerId,
        center: [-7.1101, 52.8593], // 初始化中心点
        zoom: 7, // 初始化层级
        // projection: "globe",
        // language: "zh-Hans",
        // pitch: 65,
        // bearing: -180,
        //interactive: false,
      });
      this.map.on("load", () => {
        this.AddTruckIcon();
          this.map.loadImage(require('@/assets/location.png'), (error, image) => {
            if (error) throw error;
            if (!this.map.hasImage('location-icon')) {
              this.map.addImage('location-icon', image);
            }
          });
      });

      const THRESHOLD = 0;
      this.map.on('click', async (e) => {
        // 动态生成图层ID列表
        const truckLayerIds = this.TruckLocations.map(truck => `truck-layer-${truck.id}`);
        var features = this.map.queryRenderedFeatures(e.point, {layers: truckLayerIds});

        if (features.length > 0) {
          // 如果点击了卡车图标，找到对应的卡车信息并显示卡片
          const clickedTruckId = features[0].properties.id;
          const clickedTruck = this.TruckLocations.find(truck => truck.id === parseInt(clickedTruckId));
          if (clickedTruck) {
            this.updateCurrentTime();
            this.selectedTruck = clickedTruck; // 存储被点击的卡车信息

            await this.getRoute(clickedTruck.originCoords, clickedTruck.destinationCoords);
          }
          if (this.currentRouteData) {
            const truckPosition = turf.point([clickedTruck.lng, clickedTruck.lat]);
            const routeLine = turf.lineString(this.currentRouteData.geometry.coordinates);
            const distance = turf.pointToLineDistance(truckPosition, routeLine, {units: 'kilometers'});

            if (distance > THRESHOLD) {
              // 更新偏离距离
              this.deviationDistance = distance.toFixed(2);
            } else {
              // 如果不超过阈值，可以清除之前的偏离距离显示
              this.deviationDistance = null;
            }
          }
        } else {
          // 如果点击的是卡车图标之外的地方，隐藏卡片
          this.selectedTruck = null;
          this.deviationDistance = null;
          if (this.map.getLayer('route')) {
            // 使用setLayoutProperty方法隐藏图层
            this.map.setLayoutProperty('route', 'visibility', 'none');
          }
        }
      });

      this.map.on("webglcontextrestored", () => {
        console.log("A webglcontextrestored event occurred.");
      });

      this.map.on("webglcontextlost", () => {
        console.log("A webglcontextlost event occurred.");
      });
    },

    AddTruckIcon() {
      const truckIcon = require('@/assets/truck_icon.png');
      this.map.loadImage(truckIcon, (error, image) => {
        if (error) throw error;
        // 确保图标未被重复添加
        if (!this.map.hasImage('truck-icon')) {
          this.map.addImage('truck-icon', image);
        }

        this.TruckLocations.forEach((truck) => {
          const sourceId = `truck-${truck.id}`;
          const layerId = `truck-layer-${truck.id}`;

          // 准备geojson数据
          const geojsonData = {
            type: 'Feature',
            properties: {
              id: truck.id,
              licensePlate: truck.licensePlate,
              origin: truck.origin,
              destination: truck.destination
            },
            geometry: {
              type: 'Point',
              coordinates: [truck.lng, truck.lat]
            }
          };

          // 检查是否已存在对应的source和layer
          if (this.map.getSource(sourceId)) {
            // 如果source已存在，则直接更新其数据
            this.map.getSource(sourceId).setData(geojsonData);
          } else {
            // 如果source不存在，则添加新的source和layer
            this.map.addSource(sourceId, {
              type: 'geojson',
              data: geojsonData
            });

            this.map.addLayer({
              id: layerId,
              type: 'symbol',
              source: sourceId,
              layout: {
                'icon-image': 'truck-icon',
                'icon-size': 0.20
              }
            });
          }
        });
      });
    },

    ChangeMapType(selectedLayerId) {
      const selectedStyle = this.MapList.find(item => item.layerId === selectedLayerId).layerId;
      if (this.map && selectedStyle) {
        this.map.setStyle(selectedStyle);
        // 等待地图样式加载完成
        this.map.once('style.load', () => {
          this.AddTruckIcon();
        });
      }
    },

    async getRoute(start, end) {
      return new Promise(async (resolve, reject) => {
        try {
          const query = await fetch(
              `https://api.mapbox.com/directions/v5/mapbox/cycling/${start[0]},${start[1]};${end[0]},${end[1]}?steps=true&geometries=geojson&access_token=${mapboxgl.accessToken}`,
              {method: 'GET'}
          );
          const json = await query.json();
          const data = json.routes[0];
          const route = data.geometry.coordinates;
          const geojson = {
            type: 'Feature',
            properties: {},
            geometry: {
              type: 'LineString',
              coordinates: route
            }
          };
          this.currentRouteData = geojson;
          if (this.map.getSource('route')) {
            this.map.getSource('route').setData(geojson);
            if (this.map.getLayoutProperty('route', 'visibility') === 'none') {
              this.map.setLayoutProperty('route', 'visibility', 'visible');
            }
          } else {
            this.map.addLayer({
              id: 'route',
              type: 'line',
              source: {
                type: 'geojson',
                data: geojson
              },
              layout: {
                'line-join': 'round',
                'line-cap': 'round'
              },
              paint: {
                'line-color': '#3887be',
                'line-width': 5,
                'line-opacity': 0.75
              }
            });
          }
          resolve(); // 成功时解析
        } catch (error) {
          console.error('Error fetching the route:', error);
          reject(error); // 出错时拒绝
        }
      });
    },

    enterRouteSettingMode() {
      this.routeSettingMode = true;
      // 监听地图点击事件以添加路线点
      this.map.on('click', this.addWaypoint);
    },

    exitRouteSettingMode() {
      this.routeSettingMode = false;
      // 移除地图点击事件监听
      this.map.off('click', this.addWaypoint);
      // 清理地图上的临时标记和路线
      this.clearWaypointsAndRoute();
    },

    addWaypoint(e) {
      const coordinates = [e.lngLat.lng, e.lngLat.lat];
      this.waypoints.push(coordinates);
      // 添加标记
      this.addMarker(coordinates);
      // 如果有足够的点来形成路线，请求优化的路线
      if (this.waypoints.length > 1) {
        this.fetchOptimizedRoute();
      }
    },

    clearWaypointsAndRoute() {
      // 移除所有标记
      this.markerSourceIds.forEach(sourceId => {
        if (this.map.getSource(sourceId)) {
          this.map.removeLayer(sourceId);
          this.map.removeSource(sourceId);
        }
      });
      // 清空存储的sourceId数组
      this.markerSourceIds = [];

      // 移除地图上的路线，如果有的话
      if (this.map.getLayer('route')) {
        this.map.removeLayer('route');
        this.map.removeSource('route');
      }

      // 重置waypoints和优化路线数据
      this.waypoints = [];
      this.optimizedRoute = null;
    },


    async fetchOptimizedRoute() {
      const coordinatesString = this.waypoints.map(coord => `${coord[0]},${coord[1]}`).join(';');
      const url = `https://api.mapbox.com/optimized-trips/v1/mapbox/driving/${coordinatesString}?geometries=geojson&access_token=${mapboxgl.accessToken}`;

      try {
        const response = await fetch(url);
        const data = await response.json();
        if (data.trips && data.trips.length > 0) {
          const route = data.trips[0].geometry;
          this.addRoute(route);
        }
      } catch (error) {
        console.error('Error fetching the optimized route:', error);
      }
    },

    addRoute(route) {
      // 确保先移除已存在的路线
      if (this.map.getSource('route')) {
        this.map.removeLayer('route');
        this.map.removeSource('route');
      }

      this.map.addSource('route', {
        type: 'geojson',
        data: {
          type: 'Feature',
          properties: {},
          geometry: route,
        },
      });

      this.map.addLayer({
        id: 'route',
        type: 'line',
        source: 'route',
        layout: {
          'line-join': 'round',
          'line-cap': 'round',
        },
        paint: {
          'line-color': '#3887be',
          'line-width': 5,
          'line-opacity': 0.75,
        },
      });
    },

    addMarker(coordinates) {
      this.createMarker(coordinates);
    },

    createMarker(coordinates) {
      const markerGeoJSON = {
        type: 'FeatureCollection',
        features: [{
          type: 'Feature',
          geometry: {
            type: 'Point',
            coordinates: coordinates
          }
        }]
      };

      const sourceId = `marker-${Math.random().toString(36).substring(2, 15)}`;

      // 在这里添加sourceId到数组
      this.markerSourceIds.push(sourceId);

      this.map.addSource(sourceId, {
        type: 'geojson',
        data: markerGeoJSON
      });

      this.map.addLayer({
        id: sourceId,
        type: 'symbol',
        source: sourceId,
        layout: {
          'icon-image': 'location-icon',
          'icon-size': 0.2 // 调整大小以适应你的图标
        }
      });
    },

    async saveRoute() {
      console.log(this.waypoints)
      if (this.waypoints.length < 2) {
        alert("Please add at least two points to form a route.");
        return;
      }
      const valid = await this.$refs.formRef.validate();
      if (valid) {
        const createAt = new Date().toISOString();  // 获取当前时间并转换为 ISO 格式字符串
        const response = await this.$request({
          url: '/routes/add',
          method: 'POST',
          data: {
            routes: {
              routeName: this.form.routeName,
              createAt: createAt  // 添加创建时间
            },
            wayPointsList: this.waypoints.map((wp, index) => ({
              latitude: wp[1], // 第二个元素是纬度
              longitude: wp[0], // 第一个元素是经度
              sequence: index + 1 // 为每个点添加序列号
            }))
          }
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('Success')
            this.$message.success('Route saved successfully');
            this.FormVisible = false;
            this.clearWaypointsAndRoute();
            this.exitRouteSettingMode();
          } else {
            this.$message.error(res.msg) // 弹出错误信息
          }
        })
      }
    },

    handleAdd() {
      this.FormVisible = true;
    }

  },
};
</script>
<style lang="scss" scoped>
.Map {
  padding: 0;
  margin: 0;
  width: 100%;
  height: 100%;
  position: relative;

  #MyMapContainer {
    width: 100%;
    height: 100%;

    ::v-deep .mapboxgl-ctrl {
      display: none !important;
    }
  }

  .Selectbox {
    position: absolute;
    top: 10px;
    left: 10px;
  }

  .button {
    position: absolute;
    top: 10px;
    left: 240px;
  }

  .truck-info {
    position: absolute;
    top: 10px;
    right: 10px;
    width: 300px;
    z-index: 1; /* 确保卡片显示在地图之上 */
  }

  .text {
    font-size: 14px;
  }

  .time {
    font-size: 13px;
    color: #999;
  }

  .bottom {
    margin-top: 13px;
    line-height: 12px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }

  .clearfix:after {
    clear: both
  }
}
</style>