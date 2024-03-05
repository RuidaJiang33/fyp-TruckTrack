import axios from 'axios'
import router from "@/router";

const request = axios.create({
    baseURL: 'http://localhost:9090',
    timeout: 30000
})
/*
* request拦截器
* 可以自请求发送前对请求做一些处理
* 比如统一加token，对请求参数统一加密
* */
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    let user = JSON.parse(localStorage.getItem("test-user") || '{}')
    config.headers['token'] = user.token // 设置请求头

    return config
}, error => {
    console.log('request error: ' + error)
    return Promise.reject(error)
});

/*
* response拦截器
* 可以在接口相应后统一处理结果
* */
request.interceptors.response.use(
    response => {
        let res = response.data;

        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            router.push('/login')
        }
        return res;
    },
    error => {
        console.error('response error: ' + error)
        return Promise.reject(error)
    }
)

export default request