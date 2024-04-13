<template>
  <!-- <el-tree>: 表示使用Element UI的树形组件。
          :data="menus": 绑定一个名为menus的数据属性，作为树状结构的数据源。
          :props="defaultProps": 绑定一个名为defaultProps的数据属性，用来设置树形组件的默认属性，例如节点标签、子节点等。
          @node-click="handleNodeClick": 绑定一个名为handleNodeClick的事件处理函数，当点击树节点时会触发该函数。 -->
  <el-tree
    :data="menus"
    :props="defaultProps"
    @node-click="handleNodeClick"
  />
</template>

<script>
//  这里可以导入其他文件（比如： 组件， 工具 js， 第三方插件 js， json文件， 图片文件等等）
//  例如： import 组件名称 from '组件路径 ';

export default {
  //  import 引入的组件需要注入到对象中才能使用
  components: {},
  props: {},
  data() {
    //  这里存放数据
    return {
      menus: [],
      defaultProps: {
        children: 'children',
        label: 'name'
      }
    };
  },
  //  计算属性 类似于 data 概念
  computed: {},
  //  监控 data 中的数据变化
  watch: {},
  //  方法集合
  methods: {
    handleNodeClick(data) {
      console.log(data);
    },
    getMenus() {
      this.$http({
        url: this.$http.adornUrl('/product/category/list/tree'),
        method: 'get'
      }).then(({data}) => {
        console.log("成功获取到菜单数据...", data.data)
        this.menus = data.data;
      })
    }
  },
  //  生命周期 - 创建完成（可以访问当前 this 实例）
  created() {
    this.getMenus();
  },
  //  生命周期 - 挂载完成（可以访问 DOM 元素）
  mounted() {
  },
  beforeCreate() {
  }, //  生命周期 - 创建之前
  beforeMount() {
  }, //  生命周期 - 挂载之前
  beforeUpdate() {
  }, //  生命周期 - 更新之前
  updated() {
  }, //  生命周期 - 更新之后
  beforeDestroy() {
  }, //  生命周期 - 销毁之前
  destroyed() {
  }, //  生命周期 - 销毁完成
  activated() {
  }, //  如果页面有 keep-alive 缓存功能， 这个函数会触发
};
</script>
<style lang='scss' scoped>
//  @import url(); 引入公共 css 类
</style>
