<template>
  <!-- <el-tree>: 表示使用Element UI的树形组件。
          :data="menus": 绑定一个名为menus的数据属性，作为树状结构的数据源。
          :props="defaultProps": 绑定一个名为defaultProps的数据属性，用来设置树形组件的默认属性，例如节点标签、子节点等。
          @node-click="handleNodeClick": 绑定一个名为handleNodeClick的事件处理函数，当点击树节点时会触发该函数。 -->
  <el-tree
    :data="menus"
    :props="defaultProps"
    :expand-on-click-node="false"
    show-checkbox
    node-key="catId"
    :default-expanded-keys="expendedKey"
  >
    <span class="custom-tree-node" slot-scope="{ node, data }">
      <span>{{ node.label }}</span>
      <span>
        <el-button
          v-if="node.level <= 2"
          type="text"
          size="mini"
          @click="() => append(data)"
        >Append</el-button
        >
        <el-button
          v-if="node.childNodes.length === 0"
          type="text"
          size="mini"
          @click="() => remove(node, data)"
        >Delete</el-button
        >
      </span>
    </span>
  </el-tree>
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
      expendedKey: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
    };
  },
  //  计算属性 类似于 data 概念
  computed: {},
  //  监控 data 中的数据变化
  watch: {},
  //  方法集合
  methods: {
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/category/list/tree"),
        method: "get",
      })
        .then(({data}) => {
          console.log("成功获取到菜单数据...", data.data);
          this.menus = data.data;
        });
    },
    append(data) {
      console.log("append", data);
    },
    remove(node, data) {
      // 准备要删除的分类ID列表
      let ids = [data.catId];

      this.$confirm(`是否删除【${data.name}】菜单?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          // 发起DELETE请求删除分类
          this.$http({
            url: this.$http.adornUrl('/product/category/delete'), // 构造请求URL
            method: 'delete', // 使用DELETE方法
            data: this.$http.adornDeleteData(ids, false) // 装饰请求数据
          })
            .then(({data}) => {
              // 删除成功
              this.$message({
                type: 'success',
                message: '菜单删除成功!'
              });
              // 请求成功后的处理，打印删除成功的消息和返回的数据
              console.log('删除成功', data);
              // 重新获取菜单数据，以更新显示
              this.getMenus();
              // 设置默认需要展开的菜单
              this.expendedKey = [node.parent.data.catId];
            });
        })
        .catch(() => {
          // 删除失败
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });

      // 打印删除操作的相关信息，用于调试或日志记录
      console.log("remove", node, data);
    },
  },
  //  生命周期 - 创建完成（可以访问当前 this 实例）
  created() {
    // 获取菜单列表
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
