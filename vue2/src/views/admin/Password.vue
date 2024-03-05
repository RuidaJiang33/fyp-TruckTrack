<template>
  <div>
    <el-card style="width: 50%;">
      <el-form :model="user" label-width="150px" :rules="rules" ref="formRef">
        <el-form-item label="Original Password" prop="password">
          <el-input show-password v-model="user.password" placeholder="original password"></el-input>
        </el-form-item>
        <el-form-item label="New Password" prop="newPassword">
          <el-input show-password v-model="user.newPassword" placeholder="new password"></el-input>
        </el-form-item>
        <el-form-item label="Confirm Password" prop="confirmPassword">
          <el-input show-password v-model="user.confirmPassword" placeholder="confirm password"></el-input>
        </el-form-item>
        <div style="text-align: center; margin-bottom: 20px">
          <el-button type="primary" @click="update">Confirm</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "Password",
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Enter confirmed Password'))
      } else if (value !== this.user.newPassword) {
        callback(new Error(('Invalid confirmed Password')))
      } else {
        callback()
      }
    }
    return {
      user: JSON.parse(localStorage.getItem('test-user') || '{}'),
      rules: {
        password: [
          { required: true, message: 'Please enter original password', trigger: 'blur' },
        ],
        newPassword: [
          { required: true, message: 'Please enter new password', trigger: 'blur' },
        ],
        confirmPassword: [
          { validator: validatePassword, required: true, trigger: 'blur' },
        ],
      }
    }
  },
  methods: {
    update() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          this.user.password = this.user.newPassword
          this.$request.put('user/update', this.user).then(res => {
            if(res.code === '200') {
              this.$message.success('success')
              this.$router.push('/login')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    },
  }
}
</script>

<style scoped>

</style>