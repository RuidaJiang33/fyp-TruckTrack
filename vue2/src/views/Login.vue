<template>
  <div style="height: 100vh; overflow: hidden; display: flex; align-items: center; justify-content: center; background-color: #0f9876">
    <div style="display: flex; background-color: white; width: 60%; height: 60%; border-radius: 5px; overflow:hidden;">
      <div style="flex: 1; display: flex; align-items: center">
        <img src="@/assets/login1.png" alt="" style="width: 100%;">
      </div>
      <div style="flex: 1; display: flex; align-items: center; justify-content: center">
        <el-form :model="user" style="width: 80%" :rules="rules" ref="loginRef">
          <div style="font-size: 20px; font-weight: bold; text-align: center">Login</div>
          <el-form-item prop="username">
            <el-input prefix-icon="el-icon-user" size="medium" placeholder="username" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input prefix-icon="el-icon-lock" size="medium" show-password placeholder="password" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="code">
            <div style="display: flex">
              <el-input prefix-icon="el-icon-circle-check" size="medium" style="flex: 1" v-model="user.code"></el-input>
              <div style="flex: 1; height: 36px">
                <valid-code @update:value="getCode"/>
              </div>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width: 100%" @click="login">Login</el-button>
          </el-form-item>
          <div style="display: flex">
            <div style="flex: 1; font-size: 13px">Without an account?  <span style="color: #0f9876; cursor: pointer" @click="$router.push('/register')">Sign in</span></div>
            <div style="flex: 1; text-align: right; font-size: 13px"><span style="color: #0f9876; cursor: pointer" @click="handleForgetPassword">Forget password</span></div>
          </div>
        </el-form>
      </div>
    </div>

    <el-dialog title="Forget Password" :visible.sync="forgetPassDialogVis" width="30%">
      <el-form :model="forgetUserForm" label-width="100px" style="padding-right: 20px">
        <el-form-item label="username">
          <el-input v-model="forgetUserForm.username" autocomplete="off" placeholder="Please enter username"></el-input>
        </el-form-item>
        <el-form-item label="phone">
          <el-input v-model="forgetUserForm.phone" autocomplete="off" placeholder="Please enter phone"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="forgetPassDialogVis = false">Cancel</el-button>
        <el-button type="primary" @click="resetPassword">Confirm</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ValidCode from "@/components/ValidCode";

export default {
  name: "Login",
  components: {
    ValidCode
  },
  data(){

    // 验证码校验
    const validateCode = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('Enter verification code'))
      } else if (value.toLowerCase() !== this.code) {
        callback(new Error(('Invalid verification code')))
        } else {
        callback()
      }
    }

    return{
      forgetUserForm: {}, // 忘记密码的表单数据
      forgetPassDialogVis: false,
      code:'', // 验证码组件传的code
      user: {
        code:'', // 表单里用户输入的code
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: 'Enter username', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Enter password', trigger: 'blur' }
        ],
        code: [
          { validator: validateCode, trigger: 'blur' }
        ],
      }
    }
  },
  methods: {
    handleForgetPassword() { // 初始化表单的数据
      this.forgetUserForm = {}
      this.forgetPassDialogVis = true
    },
    resetPassword() {
      this.$request.put('/password', this.forgetUserForm).then(res => {
        if (res.code === '200') {
          this.$message.success('success')
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    getCode(code){
      console.log(code)
      this.code = code.toLowerCase()
    },
    login(){
      this.$refs['loginRef'].validate((valid) => {
        if (valid) {
          // 验证通过
          this.$request.post('/login', this.user).then(res => {
            // 后台返回的code是200时，表示验证通过；存储用户信息，跳转到主页
            if(res.code === '200') {
              this.$router.push('/')
              this.$message.success('Login success')
              localStorage.setItem('test-user', JSON.stringify(res.data)) // 存储用户数据
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