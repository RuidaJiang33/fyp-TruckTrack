<template>
  <div>
    <el-table :data="tableData" >
      <el-table-column prop="oid" label="oid"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: "Order",
  data() {
    return {
      tableData: []
    }
  }
}
</script>

<style scoped>

</style>