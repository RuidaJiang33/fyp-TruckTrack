<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search username" v-model="username"></el-input>
      <el-input style="width: 200px; margin:0 5px" placeholder="Search name" v-model="name"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 5px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
      <el-button type="info" style="background-color: #6C7DF8" @click="exportData">Download</el-button>
      <el-upload action="http://localhost:9090/user/import" :headers="{token: user.token}" :on-success="handleImport" :show-file-list="false" style="display: inline-block; margin-left: 10px">
        <el-button type="primary" plain>Batch Import</el-button>
      </el-upload>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="uid" label="uid" width="70"></el-table-column>
      <el-table-column prop="username" label="username"></el-table-column>
      <el-table-column prop="name" label="name"></el-table-column>
      <el-table-column prop="email" label="email"></el-table-column>
      <el-table-column prop="phone" label="phone"></el-table-column>
      <el-table-column prop="role" label="role"></el-table-column>
      <el-table-column label="operation" align="center" width="180">
        <template v-slot="scope">
          <el-button type="primary" plain @click="handleEdit(scope.row)">edit</el-button>
          <el-button type="danger" plain @click="del(scope.row.uid)">delete</el-button>
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

    <el-dialog title="User info" :visible.sync="userFormVisible" width="30%">

      <el-form :model="form" label-width="100px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Username" prop="username">
          <el-input v-model="form.username" placeholder="Username"></el-input>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="form.name" placeholder="Name"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="form.email" placeholder="Email"></el-input>
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="form.phone" placeholder="Phone"></el-input>
        </el-form-item>
        <el-form-item label="Role" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio label="admin"></el-radio>
            <el-radio label="user"></el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="userFormVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">Confirm</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
export default {
  name: "User",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      username: '',
      name: '',
      total: 0,
      userFormVisible: false,
      form: {},
      rules: {
        username: [
          {required: true, message: 'Please enter username', trigger: 'blur'}
        ]
      },
      uids: []
    }
  },
  created() {
    this.load()
  },
  methods: {
    handleImport(res, file, fileList){
      if (res.code === '200') {
        this.$message.success("success")
        this.load(1)
      } else {
        this.$message.error(res.msg)
      }
    },
    exportData() {
      if (!this.uids.length) { // 没有选择行时全部导出，或根据搜索结果导出
        window.open('http://localhost:9090/user/export?token=' + this.user.token + "&username=" + this.username
        + "&name" + this.name) // 通过js打开新窗口
      } else {
        let uidStr = this.uids.join(',')
        window.open('http://localhost:9090/user/export?token=' + this.user.token +'&uids=' + uidStr) // 通过js打开新窗口
      }
    },
    delBatch() {
      if (!this.uids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/user/delete/batch', {
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
    del(uid) {
      this.$confirm('Confirm deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/user/delete/' + uid).then(res => {
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
      this.userFormVisible = true
    },
    handleAdd() {
      this.form = { role: 'user'}  // 新增数据的时候清空数据
      this.userFormVisible = true; //打开弹窗
    },
    save() { // 保存按钮 触发新增或更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.uid? '/user/update' : '/user/add',
            method: this.form.uid ? 'PUT' : 'POST',
            data: this.form
          }).then(res => {
            if (res.code === '200') {
              this.$message.success('Success')
              this.load(1)
              this.userFormVisible = false
            } else {
              this.$message.error(res.msg) // 弹出错误信息
            }
          })
        }
      })
    },
    reset() {
      this.name = ''
      this.username = ''
      this.load()
    },
    load(pageNum) { // 分页查询
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get('/user/selectByPage',{
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          username: this.username,
          name: this.name
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

<style scoped>

</style>