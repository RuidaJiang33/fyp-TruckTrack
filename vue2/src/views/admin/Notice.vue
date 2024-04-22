<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search title" v-model="title"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="id" label="id" width="70"></el-table-column>
      <el-table-column prop="title" label="title"></el-table-column>
      <el-table-column prop="content" label="content" show-overflow-tooltip></el-table-column>
      <el-table-column prop="user" label="author"></el-table-column>
      <el-table-column prop="time" label="time"></el-table-column>
      <el-table-column>
        <template v-slot="scope">
          <el-switch v-model="scope.row.open" @change="changeOpen(scope.row)"></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="operation" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" plain @click="handleEdit(scope.row)">edit</el-button>
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

    <el-dialog title="Notice info" :visible.sync="FormVisible" width="40%">

      <el-form :model="form" label-width="100px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Title" prop="title">
          <el-input v-model="form.title" placeholder="Title"></el-input>
        </el-form-item>
        <el-form-item label="Content" prop="content">
          <el-input type="textarea" v-model="form.content" placeholder="Content"></el-input>
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
  name: "Notice",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      title: '',
      name: '',
      total: 0,
      FormVisible: false,
      form: {},
      rules: {
        title: [
          {required: true, message: 'Please enter title', trigger: 'blur'}
        ],
        content: [
          {required: true, message: 'Please enter content', trigger: 'blur'}
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
    changeOpen(form){
      // 调用更新的接口 更新数据到数据库
      this.form = JSON.parse(JSON.stringify(form))
      this.$request({
        url: '/notice/update',
        method: 'PUT',
        data: this.form
      }).then(res => {
        if (res.code === '200') {
          this.load(1)
          this.FormVisible = false
        } else {
          this.$message.error(res.msg) // 弹出错误信息
        }
      })
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/notice/delete/batch', {
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
      this.ids = rows.map(v => v.id)
      console.log(this.ids)
    },
    del(id) {
      this.$confirm('Confirm deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/notice/delete/' + id).then(res => {
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
            url: this.form.id? '/notice/update' : '/notice/add',
            method: this.form.id ? 'PUT' : 'POST',
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
      this.title = ''
      this.load()
    },
    load(pageNum) { // 分页查询
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get('/notice/selectByPage',{
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          title: this.title
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