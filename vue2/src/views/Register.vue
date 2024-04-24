<template>
  <div style="height: 100vh; overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: #3babe7">
    <div style="display: flex; background-color: white; width: 60%; height: 60%; border-radius: 5px; overflow:hidden;">
      <div style="flex: 1; display: flex; align-items: center">
        <img src="@/assets/register1.png" alt="" style="width: 100%;">
      </div>
      <div style="flex: 1; display: flex; align-items: center; justify-content: center">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="registerRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center">Register</div>
          <el-form-item prop="username">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="username" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="password" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="conformPass">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="confirm password" v-model="user.conformPass"></el-input>
          </el-form-item>
          <el-form-item>
            <el-radio-group v-model="user.role">
              <el-radio label="user"></el-radio>
              <el-radio label="admin"></el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%" @click="register">Register</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1; font-size: 13px">With an account?  <span style="color: #3BABE7; cursor: pointer" @click="$router.push('/login')">Log in</span></div>
          </div>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>

export default {
  name: "Register",
  data(){
    const validatePass = (rule, conformPass, callback) => {
      if (conformPass === '') {
        callback(new Error('Confirm password'))
      } else if (conformPass !== this.user.password) {
        callback(new Error('The passwords don\'t match. Please try again.'))
      } else {
        callback()
      }
    }

    return{
      user: {
        username: '',
        password: '',
        conformPass: '',
        role:'',
      },
      rules: {
        username: [
          { required: true, message: 'Enter username', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Enter password', trigger: 'blur' }
        ],
        // role: [
        //   { required: true, message: 'Please choose a role', trigger: 'blur' }
        // ],
        conformPass: [
          { validator: validatePass, trigger: 'blur' }
        ],
      }
    }
  },
  methods: {
    register(){
      this.$refs['registerRef'].validate((valid) => {
        if (valid) {
          this.$request.post('/register', this.user).then(res => {
            // 后台返回的code是200时，表示验证通过；存储用户信息，跳转到主页
            if(res.code === '200') {
              this.$router.push('/login')
              this.$message.success('Register success')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped>

</style>