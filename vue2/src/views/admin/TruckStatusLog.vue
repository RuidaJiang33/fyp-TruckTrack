<template>
  <div>
    <div>
      <el-input style="width: 200px" placeholder="Search"></el-input>
      <el-button type="primary" @click="load(1)" style="margin-left: 10px">Search</el-button>
      <el-button type="info" @click="reset" style="background-color: #6C7DF8">Reset</el-button>
    </div>
    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">Add</el-button>
      <el-button type="danger" plain @click="delBatch">Batch delete</el-button>
      <el-button type="primary" plain @click="backToTruck">Back</el-button>
    </div>
    <el-table :data="tableData" stripe :header-cell-style="{ backgroundColor:'aliceblue', color:'#666'}" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center"></el-table-column>
      <el-table-column prop="id" label="id" width="100"></el-table-column>
      <el-table-column prop="truckId" label="truckId"></el-table-column>
      <el-table-column prop="licensePlate" label="licensePlate"></el-table-column>
      <el-table-column prop="status" label="status"></el-table-column>
      <el-table-column prop="timestamp" label="timestamp"></el-table-column>
      <el-table-column label="operation" align="center" width="230">
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

    <el-dialog title="TruckStatusLog info" :visible.sync="FormVisible" width="40%">
      <el-form :model="form" label-width="130px" style="padding-right: 20px" :rules="rules" ref="formRef">
        <el-form-item label="Truck Id" prop="oid">
          <span>{{ form.truckId }}</span>
        </el-form-item>
        <el-form-item label="LicensePlate" prop="licensePlate">
          <span>{{ this.licensePlate }}</span>
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
        <el-form-item label="Timestamp" prop="timestamp">
          <div class="block">
            <el-date-picker
                v-model="form.timestamp"
                type="datetime"
                placeholder="Choose date and time">
            </el-date-picker>
          </div>
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
  name: "TruckStatusLog",
  data() {
    return {
      tableData: [], // 所有的数据
      pageNum: 1, // 当前的页码
      pageSize: 5, // 每页显示的个数
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      total: 0,
      FormVisible: false,
      form: {},
      rules: {
        status: [
          {required: true, message: 'Please enter product name', trigger: 'blur'}
        ],
        timestamp: [
          {required: true, message: 'Please enter timestamp', trigger: 'blur'}
        ],
      },
      ids: [],
      truckId: 0,
      licensePlate: '',
      options: [{
        status: 'On The Way',
        label: 'On The Way'
      }, {
        status: 'Leisure',
        label: 'Leisure'
      }],
    };
  },
  created() {
    this.truckId = this.$route.params.truckId;
    this.load()
  },
  methods: {
    backToTruck() {
      this.$router.push({ path: '/truck' });
    },
    formatTimestamp(timestampArray) {
      // 由于JavaScript中的月份是从0开始的，因此需要对月份进行-1处理
      const [year, month, day, hour, minute] = timestampArray;
      const formattedDate = new Date(year, month - 1, day, hour, minute);
      // 使用模板字符串和padStart方法保证两位数字的格式
      return `${formattedDate.getFullYear()}-${(formattedDate.getMonth() + 1).toString().padStart(2, '0')}-${formattedDate.getDate().toString().padStart(2, '0')} ${formattedDate.getHours().toString().padStart(2, '0')}:${formattedDate.getMinutes().toString().padStart(2, '0')}`;
    },
    delBatch() {
      if (!this.ids.length) {
        this.$message.warning("Please select data")
        return
      }
      this.$confirm('Confirm bulk deletion?', 'Deletion confirm', {type: "warning"}).then(res => {
        this.$request.delete('/truckStatusLog/delete/batch', {
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
        this.$request.delete('/truckStatusLog/delete/' + id).then(res => {
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
      this.form = {
        truckId: this.truckId,
        licensePlate: this.licensePlate,
      };  // 新增数据的时候清空数据
      this.FormVisible = true; //打开弹窗
    },
    save() { // 保存按钮 触发新增或更新
      this.$refs.formRef.validate((valid) => {
        console.log(this.form)
        if (valid) {
          this.$request({
            url: this.form.id? '/truckStatusLog/update' : '/truckStatusLog/add',
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
    load(pageNum) {
      if (pageNum) {
        this.pageNum = pageNum;
      }
      this.$request.get(`/truckStatusLog/selectByTruckId/${this.truckId}`, {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
        }
      }).then(res => {
        if (res.code === '200') {
          console.log(res.data.records[0].licensePlate);
          if (res.data !== null) {
            this.licensePlate = res.data.records[0].licensePlate
          }
          // 在这里调用formatTimestamp函数来转换每个记录的timestamp字段
          this.tableData = res.data.records.map(record => {
            // 检查是否存在timestamp字段且它是一个数组
            if (record.timestamp && Array.isArray(record.timestamp)) {
              // 转换timestamp字段为字符串格式
              record.timestamp = this.formatTimestamp(record.timestamp);
            }
            return record;
          });
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