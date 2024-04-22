<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search" v-model="operation"></el-input>
      <el-select style="margin: 0 5px" v-model="type">
        <el-option v-for="item in ['ADD', 'UPDATE', 'DELETE', 'BATCH_DELETE', 'LOGIN', 'REGISTER']" :key="item" :value="item" :label="item" placeholder="Select an operation"></el-option>
      </el-select>
      <el-input style="width: 200px" placeholder="Search operator" v-model="optUser"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="id" label="id" width="70"></el-table-column>
      <el-table-column prop="operation" label="operation"></el-table-column>
      <el-table-column prop="type" label="type"></el-table-column>
      <el-table-column>
        <template v-slot="scope">
          <el-tag type="primary" v-if="scope.row.type === 'ADD'">{{ scope.row.type }}</el-tag>
          <el-tag type="info" v-if="scope.row.type === 'UPDATE'">{{ scope.row.type }}</el-tag>
          <el-tag type="danger" v-if="scope.row.type === 'DELETE'">{{ scope.row.type }}</el-tag>
          <el-tag type="danger" v-if="scope.row.type === 'BATCH_DELETE'">{{ scope.row.type }}</el-tag>
          <el-tag type="success" v-if="scope.row.type === 'LOGIN'">{{ scope.row.type }}</el-tag>
          <el-tag type="success" v-if="scope.row.type === 'REGISTER'">{{ scope.row.type }}</el-tag>

        </template>
      </el-table-column>
      <el-table-column prop="ip" label="ip"></el-table-column>
      <el-table-column prop="user" label="user"></el-table-column>
      <el-table-column prop="time" label="time"></el-table-column>
      <el-table-column label="operation" align="center" width="180">
        <template v-slot="scope">
          <el-button type="danger" plain @click="del(scope.row.id)">delete</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="margin: 10px 0">
      <el-pagination
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total">
      </el-pagination>
    </div>
  </div>
</template>

<script>

export default {
  name: "Logs",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      operation: '',
      name: '',
      total: 0,
      ids: [],
      content: '',
      type: '',
      optUser: ''
    }
  },
  created() {
    this.load()
  },
  methods: {
    delBatch() {
      if (!this.uids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/logs/delete/batch', {
          data: this.uids
        }).then(res => {
          if (res.code === '200') {
            this.$message.success('Success')
            this.load(1)
          } else {
            this.$message.error(res.msg) // 弹出错误信息
          }
        })
      }).catch(() => {})
    },
    handleSelectionChange(rows) { // 当前选中的所有的行数据
      this.uids = rows.map(v => v.uid)
      console.log(this.uids)
    },
    del(id) {
      this.$confirm('Confirm deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/logs/delete/' + id).then(res => {
          if (res.code === '200') {
            this.$message.success('Success')
            this.load(1)
          } else {
            this.$message.error(res.msg) // 弹出错误信息
          }
        })
      }).catch(() => {})
    },
    reset() {
      this.operation = ''
      this.type = ''
      this.optUser = ''
      this.load()
    },
    load(pageNum) { // 分页查询
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get('/logs/selectByPage',{
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          operation: this.operation,
          type: this.type,
          user: this.optUser
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      })
    },
    handleCurrentChange(pageNum) {
      this.load(pageNum)
    }
  }
}
</script>

<style>
.el-tooltip__popper{ max-width: 200px !important; }
</style>