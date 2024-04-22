<template>
  <div>
    <el-row :gutter="10">
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="line">
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div style="width: 100%; height: 400px" id="bar">
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="10">
      <el-col :span="12" style="margin: 10px 0">
        <el-card>
          <div style="width: 100%; height: 400px" id="pie">
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'

const option = {
  title: {
    text: 'Order Volume Trend Chart',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value'
  },
  series: [
    {
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

const truck_status_option = {
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      // Use axis to trigger tooltip
      type: 'shadow' // 'shadow' as default; can also be 'line' or 'shadow'
    }
  },
  legend: {},
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value'
  },
  yAxis: {
    type: 'category',
    data: []
  },
  series: [
    {
      name: 'leisure time',
      type: 'bar',
      stack: 'total',
      label: {
        show: true
      },
      emphasis: {
        focus: 'series'
      },
      data: []
    },
    {
      name: 'service time',
      type: 'bar',
      stack: 'total',
      label: {
        show: true
      },
      emphasis: {
        focus: 'series'
      },
      data: []
    },
  ]
};

const order_status_distribution_option = {
  title: {
    text: 'Orders Status Distribution',
    subtext: 'Current Data',
    left: 'center'
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: 'Access From',
      type: 'pie',
      radius: '50%',
      center: ['50%', '60%'],
      data: [],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
};

export default {
  name: "Charts",

  mounted() { // 等待页面的元素全部加载完成再初始化
    let lineDom = document.getElementById('line');
    let lineChart = echarts.init(lineDom);


    let barDom = document.getElementById('bar');
    let barChart = echarts.init(barDom);

    let pieDom = document.getElementById('pie');
    let pieChart = echarts.init(pieDom);
    pieChart.setOption(order_status_distribution_option)

    this.$request.get('/orderTrendCharts').then(res => {
      console.log(res.data)
      option.xAxis.data = res.data?.map(v => v.orderDate) || []
      option.series[0].data = res.data?.map(v => v.value) || []
      lineChart.setOption(option)
    })

    this.$request.get('/truckStatusCharts').then(res => {
      console.log(res.data);
      // 准备图表数据
      const licensePlates = res.data.map(item => item.licensePlate);
      const leisureTimes = res.data.map(item => item.leisureTime);
      const serviceTimes = res.data.map(item => item.serviceTime);

      // 更新图表配置
      truck_status_option.yAxis.data = licensePlates;
      truck_status_option.series[0].data = leisureTimes;
      truck_status_option.series[1].data = serviceTimes;

      // 应用配置更新图表
      barChart.setOption(truck_status_option);
    });

    this.$request.get('/orderStatusCharts').then(res => {
      console.log(res.data)
      const updatedData = res.data.map(item => ({
        value: item.count,
        name: item.status
      }));
      // 更新饼图配置对象的数据部分
      let updatedOptions = {
        ...order_status_distribution_option,
        series: [{
          ...order_status_distribution_option.series[0],
          data: updatedData
        }]
      };

      // 应用配置更新饼图
      pieChart.setOption(updatedOptions);
    })
  }
}
</script>

<style scoped>

</style>