<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search Route Name" v-model="routeName"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="routeId" label="routeId" width="160"></el-table-column>
      <el-table-column prop="routeName" label="routeName"></el-table-column>
      <el-table-column prop="createAt" label="createAt">
        <template v-slot="{ row }">
          {{ formatDate(row.createAt) }}
        </template>
      </el-table-column>
      <el-table-column label="operation" align="center" width="230">
        <template v-slot="scope">
          <el-button type="primary" plain @click="handleEdit(scope.row)" size="mini">edit</el-button>
          <el-button type="danger" plain @click="del(scope.row.routeId)" size="mini">delete</el-button>
          <el-button type="success" plain @click="showRouteDetails(scope.row.routeId)" size="mini">Details</el-button>
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

    <el-dialog title="Routes info" :visible.sync="FormVisible" width="40%">

      <el-form :model="form" label-width="100px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Routes" prop="routes">
          <el-input v-model="form.routeName" placeholder="Routes"></el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="FormVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>

export default {
  name: "Routes",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      routeName: '',
      routeId: '',
      name: '',
      total: 0,
      FormVisible: false,
      form: {},
      rules: {
        routeName: [
          {required: true, message: 'Please enter the route name', trigger: 'blur'}
        ]
      },
      ids: [],
      content: ''
    }
  },
  created() {
    this.load()
  },
  methods: {
    showRouteDetails(routeId) {
      this.$router.push({ name: 'RouteMap', params: { routeId: String(routeId) }});
    },
    formatDate(createAtArray) {
      if (!createAtArray || createAtArray.length < 6) {
        return '';
      }
      // 月份需要减1因为JS的月份是从0开始的
      const date = new Date(createAtArray[0], createAtArray[1] - 1, createAtArray[2], createAtArray[3], createAtArray[4], createAtArray[5]);
      return date.toISOString().substring(0, 19).replace('T', ' ');
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/routes/delete/batch', {
          data: this.ids
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
      this.ids = rows.map(v => v.routeId)
    },
    del(routeId) {
      this.$confirm('Confirm deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/routes/delete/' + routeId).then(res => {
          if (res.code === '200') {
            this.$message.success('Success')
            this.load(1)
          } else {
            this.$message.error(res.msg) // 弹出错误信息
          }
        })
      }).catch(() => {})
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row)) // 给form对象赋值 深拷贝数据
      this.FormVisible = true
    },
    handleAdd() {
      this.form = {}  // 新增数据的时候清空数据
      this.FormVisible = true; //打开弹窗
    },
    save() { // 保存按钮 触发新增或更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.routeId? '/routes/update' : '/routes/add',
            method: this.form.routeId ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('Success')
              this.load(1)
              this.FormVisible = false
            } else {
              this.$message.error(res.msg) // 弹出错误信息
            }
          })
        }
      })
    },
    reset() {
      this.routeName = ''
      this.load()
    },
    load(pageNum) { // 分页查询
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get('/routes/selectByPage',{
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          routeName: this.routeName
        }
      }).then(res => {
        console.log(res.data)
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