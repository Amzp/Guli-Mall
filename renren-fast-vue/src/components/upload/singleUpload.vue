 <template>
  <div>
    <!-- 上传组件，包含上传按钮和上传提示 -->
    <el-upload
      action="http://mzp-2404.oss-cn-beijing.aliyuncs.com"
      :data="dataObj"
      list-type="picture"
      :multiple="false" :show-file-list="showFileList"
      :file-list="fileList"
      :before-upload="beforeUpload"
      :on-remove="handleRemove"
      :on-success="handleUploadSuccess"
      :on-preview="handlePreview">
      <el-button size="small" type="primary">点击上传</el-button>
      <div slot="tip" class="el-upload__tip">只能上传jpg/png文件，且不超过10MB</div>
    </el-upload>
    <!-- 预览图片的对话框 -->
    <el-dialog :visible.sync="dialogVisible">
      <img width="100%" :src="fileList[0].url" alt="">
    </el-dialog>
  </div>
</template>
<script>
import {policy} from './policy'
import {getUUID} from '@/utils'

export default {
  name: 'singleUpload',
  props: {
    value: String // 上传成功后图片的URL
  },
  computed: {
    imageUrl () {
      return this.value
    },
    imageName () {
      // 根据URL获取文件名
      if (this.value != null && this.value !== '') {
        return this.value.substr(this.value.lastIndexOf('/') + 1)
      } else {
        return null
      }
    },
    fileList () {
      // 返回当前上传文件列表
      return [{
        name: this.imageName,
        url: this.imageUrl
      }]
    },
    showFileList: {
      get: function () {
        // 控制文件列表的显示
        return this.value !== null && this.value !== '' && this.value !== undefined
      },
      set: function (newValue) {
      }
    }
  },
  data () {
    return {
      dataObj: {
        policy: '',
        signature: '',
        key: '',
        ossaccessKeyId: '',
        dir: '',
        host: ''
        // callback:'',
      },
      dialogVisible: false // 控制预览对话框的显示状态
    }
  },
  methods: {
    emitInput (val) {
      // 触发输入事件，用于父组件监听上传结果
      this.$emit('input', val)
    },
    handleRemove (file, fileList) {
      // 文件移除事件，重置输入值
      this.emitInput('')
    },
    handlePreview (file) {
      // 文件预览事件，打开预览对话框
      this.dialogVisible = true
    },
    beforeUpload (file) {
      // 上传之前的钩子，用于设置上传参数
      let _self = this
      return new Promise((resolve, reject) => {
        policy().then(response => {
          console.log('响应的数据', response)
          _self.dataObj.policy = response.data.policy
          _self.dataObj.signature = response.data.signature
          _self.dataObj.ossaccessKeyId = response.data.accessid
          // 设置上传到OSS的文件名
          // eslint-disable-next-line no-template-curly-in-string
          _self.dataObj.key = response.data.dir + getUUID() + '_${filename}'
          _self.dataObj.dir = response.data.dir
          _self.dataObj.host = response.data.host
          console.log('响应的数据222。。。', _self.dataObj)
          resolve(true)
        }).catch(() => {
          // eslint-disable-next-line prefer-promise-reject-errors
          reject(false)
        })
      })
    },
    handleUploadSuccess (res, file) {
      // 上传成功后的处理
      console.log('上传成功...')
      this.showFileList = true
      this.fileList.pop()
      this.fileList.push({
        name: file.name,
        // eslint-disable-next-line no-template-curly-in-string
        url: this.dataObj.host + '/' + this.dataObj.key.replace('${filename}', file.name)
      })
      this.emitInput(this.fileList[0].url)
    }
  }
}
</script>
<style>

</style>
