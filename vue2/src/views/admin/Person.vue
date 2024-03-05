<template>
  <div>
    <el-card style="width: 50%;">
      <el-form :model="user" label-width="100px" :rules="rules">
        <el-avatar style="background-color: #0f9876;"> user </el-avatar>
        <el-form-item label="Username" prop="username">
          <el-input v-model="user.username" placeholder="username" disabled></el-input>
        </el-form-item>
        <el-form-item label="Name" prop="name">
          <el-input v-model="user.name" placeholder="name"></el-input>
        </el-form-item>
        <el-form-item label="Email" prop="email">
          <el-input v-model="user.email" placeholder="email"></el-input>
        </el-form-item>
        <el-form-item label="Phone" prop="phone">
          <el-input v-model="user.phone" placeholder="phone"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">Save</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Person",
  data() {
    return {
      circleUrl: "https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png",
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      rules: {
        name: [
          { message: 'Enter name', trigger: 'blur' },
          { max: 30, message: 'Name cannot exceed 30 characters', trigger: 'blur' }
        ],
        email: [
          { message: 'Enter email address', trigger: 'blur' },
          { type: 'email', message: 'Please enter a valid email address', trigger: 'blur,change' }
        ],
        phone: [
          { message: 'Enter phone number', trigger: 'blur' },
          { max: 11, message: 'Phone number cannot exceed 11 characters', trigger: 'blur' },
          { pattern: /^\d{1,11}$/, message: 'Phone number must be digits', trigger: 'blur' }
        ]
      }
    }
  },
  created() {

  },
  methods: {
    update() {
      this.$request.put('user/update', this.user).then(res => {
        if(res.code === '200') {
          this.$message.success('success')
          localStorage.setItem('test-user', JSON.stringify(this.user))
          // 触发父级的数据更新
          this.$emit("update:user", this.user)
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    load() {

    }
  }
}
</script>

<style scoped>
/deep/ .el-form-item__label {
  font-weight: bold;
}
</style>