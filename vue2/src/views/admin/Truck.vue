<template>
  <div>
    <el-table :data="tableData" >
      <el-table-column prop="tid" label="tid"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "Truck",
  data() {
    return {
      tableData: []
    }
  }
}
</script>

<style scoped>

</style>