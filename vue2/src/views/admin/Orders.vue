<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search no" v-model="no"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
      <el-select v-model="selectedStatus" placeholder="Select status" @change="() => load(1)" style="padding-left: 10px">
        <el-option
            v-for="status in selectOptions"
            :key="status.value"
            :label="status.label"
            :value="status.status">
        </el-option>
      </el-select>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
      <el-button type="info" style="background-color: #6C7DF8" @click="exportData">Download</el-button>
      <el-upload action="http://localhost:9090/orders/import" :headers="{token: user.token}" :on-success="handleImport" :show-file-list="false" style="display: inline-block; margin-left: 10px">
        <el-button type="primary" plain>Batch Import</el-button>
      </el-upload>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="id" label="id" width="70"></el-table-column>
      <el-table-column prop="no" label="no"></el-table-column>
      <el-table-column prop="uid" label="userId"></el-table-column>
      <el-table-column prop="tid" label="truckId"></el-table-column>
      <el-table-column prop="origin" label="origin"></el-table-column>
      <el-table-column prop="destination" label="destination"></el-table-column>
      <el-table-column prop="orderDate" label="orderDate">
        <template v-slot="{ row }">
          {{ formatDate(row.orderDate) }}
        </template>
      </el-table-column>

      <el-table-column prop="deliveryDate" label="deliveryDate" width="110">
        <template v-slot="{ row }">
          {{ formatDate(row.deliveryDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="status"></el-table-column>
      <el-table-column label="operation" align="center" width="230">
        <template v-slot="scope">
          <el-button type="primary" plain @click="handleEdit(scope.row)" size="mini">edit</el-button>
          <el-button type="danger" plain @click="del(scope.row.id)" size="mini">delete</el-button>
          <el-button type="success" plain @click="navigateToOrderDetails(scope.row.id)" size="mini">Details</el-button>
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

    <el-dialog title="Orders info" :visible.sync="FormVisible" width="40%">
      <el-form :model="form" label-width="130px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Truck Id" prop="tid">
          <el-input v-model="form.tid" placeholder="Truck Id"></el-input>
        </el-form-item>
        <el-form-item label="Origin location" prop="origin">
          <el-input v-model="form.origin" placeholder="Origin location"></el-input>
        </el-form-item>
        <el-form-item label="Destination" prop="destination">
          <el-input v-model="form.destination" placeholder="Destination"></el-input>
        </el-form-item>
        <el-form-item label="Order Date" prop="orderDate">
          <div class="block">
            <el-date-picker
                v-model="form.orderDate"
                type="date"
                placeholder="Choose Order Date">
            </el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="Delivery Date" prop="deliveryDate">
          <div class="block">
            <el-date-picker
                v-model="form.deliveryDate"
                type="date"
                placeholder="Choose Delivery Date">
            </el-date-picker>
          </div>
        </el-form-item>
        <el-form-item label="Status" prop="status">
          <el-select v-model="form.status" placeholder="Choose Status">
            <el-option
                v-for="item in options"
                :key="item.status"
                :label="item.label"
                :value="item.status">
            </el-option>
          </el-select>
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
  name: "Orders",
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
        tid: [
          {required: true, message: 'Please enter truck id', trigger: 'blur'}
        ],
        origin: [
          {required: true, message: 'Please enter origin location', trigger: 'blur'}
        ],
        destination: [
          {required: true, message: 'Please enter destination', trigger: 'blur'}
        ],
      },
      ids: [],
      orderDate: '',
      deliveryDate: '',
      status: '',
      selectedStatus: '',
      options: [
        { status: 'Pending', label: 'Pending' },
        { status: 'In Progress', label: 'In Progress' },
        { status: 'Completed', label: 'Completed' }
      ],
      selectOptions: [
        { status: '', label: 'All' },
        { status: 'Pending', label: 'Pending' },
        { status: 'In Progress', label: 'In Progress' },
        { status: 'Completed', label: 'Completed' }
      ],
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
      if (!this.ids.length) { // 没有选择行时全部导出，或根据搜索结果导出
        window.open('http://localhost:9090/orders/export?token=' + this.user.token + "&no=" + this.no
            + "&status" + this.selectedStatus) // 通过js打开新窗口
      } else {
        let idStr = this.ids.join(',')
        window.open('http://localhost:9090/orders/export?token=' + this.user.token +'&ids=' + idStr) // 通过js打开新窗口
      }
    },
    navigateToOrderDetails(orderId) {
      this.$router.push({ path: `/orderDetails/${orderId}` });
    },
    formatDate(dateString) {
      const date = new Date(dateString);
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/orders/delete/batch', {
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
        this.$request.delete('/orders/delete/' + id).then(res => {
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
            url: this.form.id? '/orders/update' : '/orders/add',
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
      this.no = ''
      this.orderDate = ''
      this.deliveryDate = ''
      this.status = ''
      this.load()
    },
    load(pageNum) {
      if (pageNum) {
        this.pageNum = pageNum;
      }
      this.$request.get('/orders/selectByPage', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          no: this.no,
          status: this.selectedStatus,  // 添加状态参数
        }
      }).then(res => {
        this.tableData = res.data.records;
        this.total = res.data.total;
      });
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