<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search" v-model="no"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
      <el-button type="primary" plain @click="backToOrders">Back</el-button>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="id" label="id" width="70"></el-table-column>
      <el-table-column prop="oid" label="orderId"></el-table-column>
      <el-table-column prop="productName" label="productName"></el-table-column>
      <el-table-column prop="quantity" label="quantity"></el-table-column>
      <el-table-column prop="price" label="price"></el-table-column>
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

    <el-dialog title="OrderDetails info" :visible.sync="FormVisible" width="40%">
      <el-form :model="form" label-width="130px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Order Id" prop="oid">
          <span>{{ form.oid }}</span>
        </el-form-item>
        <el-form-item label="Product name" prop="productName">
          <el-input v-model="form.productName" placeholder="Product name"></el-input>
        </el-form-item>
        <el-form-item label="Quantity" prop="quantity">
          <el-input v-model="form.quantity" placeholder="Quantity"></el-input>
        </el-form-item>
        <el-form-item label="Price" prop="price">
          <el-input v-model="form.price" placeholder="price"></el-input>
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
  name: "OrderDetails",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      no: '',
      total: 0,
      FormVisible: false,
      form: {},
      rules: {
        oid: [
          {required: true, message: 'Please enter order id', trigger: 'blur'}
        ],
        productName: [
          {required: true, message: 'Please enter product name', trigger: 'blur'}
        ],
        quantity: [
          {required: true, message: 'Please enter quantity', trigger: 'blur'}
        ],
        price: [
          {required: true, message: 'Please enter price', trigger: 'blur'}
        ],
      },
      ids: [],
      oid: 0,
    }
  },
  created() {
    this.oid = this.$route.params.oid;
    this.load()
  },
  methods: {
    backToOrders() {
      this.$router.push({ path: '/orders' });
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/orderDetails/delete/batch', {
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
        this.$request.delete('/orderDetails/delete/' + id).then(res => {
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
      this.form = { oid: this.oid };  // 新增数据的时候清空数据
      this.FormVisible = true; //打开弹窗
    },
    save() { // 保存按钮 触发新增或更新
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.$request({
            url: this.form.id? '/orderDetails/update' : '/orderDetails/add',
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
      this.load()
    },
    load(pageNum) { // 分页查询
      if (pageNum) {
        this.pageNum = pageNum
      }
      this.$request.get(`/orderDetails/selectByOrderId/${this.oid}`,{
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        if (res.code === '200') {
          this.tableData = res.data.records;
          this.total = res.data.total;
        } else {
          this.$message.error('Failed to load data');
        }
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