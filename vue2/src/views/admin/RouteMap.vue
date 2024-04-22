<template>
  <div class="map-container">
    <div ref="mapElement" class="map"></div>
    <div class="controls">
      <el-select v-model="selectedRouteId" placeholder="Select a route" @change="loadRoute">
        <el-option
            v-for="route in routes"
            :key="route.routeId"
            :label="route.routeName"
            :value="route.routeId">
        </el-option>
      </el-select>
    </div>
  </div>
</template>

<script>
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

export default {
  name: 'RouteMap',
  props: {
    routeId: {
      type: String,
      default: null,
      // 添加一个自定义的类型验证器，确保即使传入的是数字也转换成字符串
      validator(value) {
        // 尝试转换成字符串，验证是否为有效的 ID
        const stringValue = String(value);
        return !isNaN(Number(stringValue));
      }
    }
  },
  data() {
    return {
      routes: [],
      selectedRouteId: null,
    };
  },
  mounted() {
    this.initMap();
    this.fetchRoutes();

    if (this.routeId) {
      this.selectedRouteId = this.routeId;
      this.$nextTick(() => {
        this.loadRoute();
      });
    }
  },
  methods: {
    initMap() {
      mapboxgl.accessToken = 'pk.eyJ1IjoiY2RrYTMzIiwiYSI6ImNscHU2dnBuNTBtM3Qya3F6YWNtbTQybXIifQ.Y1K9DYW4v8mqRWSZDVDV-A';
      this.map = new mapboxgl.Map({
        container: this.$refs.mapElement,
        style: 'mapbox://styles/mapbox/standard', // 可以根据需求修改地图样式
        center: [-7.1101, 52.8593],
        zoom: 7
      });
    },
    fetchRoutes() {
      this.$request.get('/routes/selectAll').then(res => {
        this.routes = res.data;
        if (this.routeId) {
          // 设置 selectedRouteId 为传入的 routeId，确保下拉框显示正确
          this.selectedRouteId = this.routeId.toString();
          this.loadRoute(); // 可以在此加载对应的路线
        }
      }).catch(error => {
        console.error('Failed to fetch routes', error);
      });
    },
    async loadRoute() {
      if (!this.selectedRouteId) return;

      try {
        const res = await this.$request.get(`/routes/selectById/${this.selectedRouteId}`);
        const routeData = res.data;
        const coordinates = routeData.map(wp => [wp.longitude, wp.latitude]);

        if (coordinates.length > 1) {
          const routeQuery = coordinates.join(';');
          const directionUrl = `https://api.mapbox.com/directions/v5/mapbox/driving/${routeQuery}?geometries=geojson&access_token=${mapboxgl.accessToken}`;
          const response = await fetch(directionUrl);
          const json = await response.json();
          const route = json.routes[0].geometry;

          if (this.map.getSource('route')) {
            this.map.getSource('route').setData({
              type: 'Feature',
              properties: {},
              geometry: route
            });
          } else {
            this.map.addSource('route', {
              type: 'geojson',
              data: {
                type: 'Feature',
                properties: {},
                geometry: route
              }
            });
            this.map.addLayer({
              id: 'route',
              type: 'line',
              source: 'route',
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
        }
      } catch (error) {
        console.error('Failed to load route details:', error);
      }
    }

  }
};

</script>

<style scoped>
.map-container {
  position: relative;
  height: 100vh;
  width: 100%;
}

.map {
  height: 100%;
  width: 100%;
}

.controls {
  position: absolute;
  top: 10px;
  left: 10px;
  z-index: 1;
}
</style>