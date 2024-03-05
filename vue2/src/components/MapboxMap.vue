<template>
  <div>
    <div ref="map" class="map"></div>
  </div>
</template>

<script>
import 'mapbox-gl/dist/mapbox-gl.css';
import mapboxgl from 'mapbox-gl';

export default {
  data() {
    return {
      map: null,
    };
  },
  mounted() {
    mapboxgl.accessToken = 'pk.eyJ1IjoiY2RrYTMzIiwiYSI6ImNscHU2dnBuNTBtM3Qya3F6YWNtbTQybXIifQ.Y1K9DYW4v8mqRWSZDVDV-A';
    
    this.map = new mapboxgl.Map({
      container: this.$refs.map,
      style: 'mapbox://styles/mapbox/standard',
      center: [-74.5, 40],
      zoom: 9,
    });
  },
};
</script>

<style scoped>
.map {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 100%;
}
</style>
