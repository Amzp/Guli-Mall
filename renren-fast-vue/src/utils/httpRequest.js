// 导入Vue、axios、路由、qs、merge以及清除登录信息的工具函数
import Vue from 'vue'
import axios from 'axios'
import router from '@/router'
import qs from 'qs'
import merge from 'lodash/merge'
import {clearLoginInfo} from '@/utils'

/**
 * 创建一个axios的http实例，配置请求的默认设置。
 *
 * 参数无。
 *
 * 返回值为配置好的axios实例。
 */
const http = axios.create({
  timeout: 1000 * 30, // 设置请求超时时间为30秒
  withCredentials: true, // 同步跨域请求时携带cookie
  headers: {
    'Content-Type': 'application/json; charset=utf-8' // 设置请求头的默认值为JSON格式，编码为utf-8
  }
})

/**
 * 请求拦截器：在http请求发送前拦截，统一配置请求头
 * @param {Object} config - 发送请求前的配置对象，可用于修改请求配置
 * @returns {Promise} 如果配置修改正常，则返回修改后的config对象；如果发生错误，则返回一个被拒绝的Promise
 */
http.interceptors.request.use(config => {
  // 从cookie中获取token，并设置到请求头中
  config.headers['token'] = Vue.cookie.get('token')
  return config
}, error => {
  // 请求错误处理：将错误信息包装成Promise并拒绝
  return Promise.reject(error)
})

/**
 * 响应拦截器：在HTTP响应拦截器中添加对响应的处理。
 * 主要用于处理响应成功和失败的情况，特别地，当响应状态码为401时，会清除登录信息并重定向到登录页。
 * @param {Object} response - 成功的响应对象。
 * @param {Object} error - 失败的响应对象。
 * @returns {Promise} 对响应对象或错误对象进行处理后的Promise。
 */
http.interceptors.response.use(response => {
  // 检查响应数据中是否包含状态码401，若是则清除登录信息并跳转到登录页
  if (response.data && response.data.code === 401) {
    clearLoginInfo()
    router.push({name: 'login'})
  }
  return response
}, error => {
  // 对错误进行处理，返回一个被拒绝的Promise
  return Promise.reject(error)
})

/**
 * 请求地址处理函数
 * 该函数用于根据当前环境和配置，构造并返回一个完整的请求URL。
 * @param {string} actionName - 需要请求的接口动作名称。
 * @returns {string} 返回构造好的请求URL。
 */
http.adornUrl = (actionName) => {
  // 判断是否为非生产环境且开启代理，若是，则使用'/proxyApi/'作为接口前缀进行代理拦截；否则，使用全局配置的baseUrl。
  return (process.env.NODE_ENV !== 'production' && process.env.OPEN_PROXY ? '/proxyApi/' : window.SITE_CONFIG.baseUrl) + actionName
}

/**
 * 处理HTTP请求参数，可以添加默认参数
 * @param {Object} params - 请求的参数对象
 * @param {boolean} openDefaultParams - 是否启用默认参数
 * @returns {Object} - 处理后的参数对象
 */
http.adornParams = (params = {}, openDefaultParams = true) => {
  // 定义默认参数
  var defaults = {
    't': new Date().getTime() // 时间戳作为默认参数之一
  }
  // 根据是否启用默认参数来合并参数对象
  return openDefaultParams ? merge(defaults, params) : params
}

/**
 * 处理并装饰POST请求的数据。
 * @param {*} data 要发送的数据对象。
 * @param {*} openDefaultData 是否添加默认数据。默认为true。
 * @param {*} contentType 数据格式，可选值为'json'或'form'。默认为'json'。
 * @returns 根据contentType，返回相应格式的数据字符串。
 *  json: 'application/json; charset=utf-8'
 *  form: 'application/x-www-form-urlencoded; charset=utf-8'
 */
http.adornData = (data = {}, openDefaultData = true, contentType = 'json') => {
  // 定义默认数据，包含当前时间戳
  var defaults = {
    't': new Date().getTime()
  }
  // 根据是否添加默认数据，合并数据对象
  data = openDefaultData ? merge(defaults, data) : data
  // 根据数据格式，返回相应格式化的数据字符串
  // 若为'json'，使用JSON.stringify()将data对象转换为JSON字符串；
  // 若为'form'，使用qs.stringify()将data对象序列化为URL查询字符串形式。
  return contentType === 'json' ? JSON.stringify(data) : qs.stringify(data)
}

/**
 * 处理delete请求的数据，可以添加默认参数
 * @param {*} data 提交的数据对象
 * @param {*} openDefaultData 是否启用默认数据附加，默认为true
 * @returns 返回经过JSON.stringify处理后的数据字符串
 */
http.adornDelete = (data = {}, openDefaultData = true) => {
  // 定义默认参数，包含当前时间戳
  var defaults = {
    't': new Date().getTime()
  }
  // 根据openDefaultData的值决定是否合并默认数据
  data = openDefaultData ? merge(defaults, data) : data
  // 将处理后的数据转换成JSON字符串
  return JSON.stringify(data)
}

/**
 * 处理put请求的数据，可以添加默认参数
 * @param {*} data 提交的数据对象
 * @param {*} openDefaultData 是否启用默认数据附加，默认为true
 * @returns 返回经过JSON.stringify处理后的数据字符串
 */
http.adornPut = (data = {}, openDefaultData = true) => {
  // 定义默认参数
  var defaults = {
    't': new Date().getTime() // 时间戳作为默认参数
  }
  // 根据是否启用默认数据，合并数据对象
  data = openDefaultData ? merge(defaults, data) : data
  // 将处理后的数据对象转换成JSON字符串
  return JSON.stringify(data)
}

export default http
