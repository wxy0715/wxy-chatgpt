<template>
  <div class="page" v-loading="loading" element-loading-text="等待响应中" element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)">
    <!-- 录入问题 -->
    <div style="width: 50%;left:0;right:0;margin:0 auto;margin-top: 50px;">
      <el-input class="input-with-select" placeholder="请输入问题" v-model="question" @keyup.enter.native="submit()">
        <el-button :disabled="disabled" slot="append" icon="el-icon-search" @click="submit()">查询</el-button>
        <el-button style="border-left: 1px solid #ccc;margin-left: 20px;" slot="append" @click="setup()">FAQ录入</el-button>
      </el-input>
    </div>

    <!-- 答案区域 -->
    <el-tabs style="width: 70%;left:0;right:0;margin:0 auto;margin-top: 70px;" type="card" v-model="editableTabsValue">
      <!-- 个人库 -->
      <el-tab-pane key="faq" label="FAQ" name="faq">
        <el-collapse v-model="activeName" accordion>
          <el-collapse-item :key="index" v-for="(item, index) in milvusResult" :title="item.question" :name="index">
            <Editor  v-if="item.answerTypeValue == 'editor'" style="height: 200px; overflow-y: hidden;" :value="item.answer" :defaultConfig="editorConfig"
              :mode="mode" />
            <mavon-editor v-if="item.answerTypeValue == 'markdown'" :editable="false"  :toolbarsFlag="false" :subfield="false" defaultOpen="preview"  :value="item.answer" style="min-height: 30vh; max-height: 70vh;"  :navigation="true" />
          </el-collapse-item>
        </el-collapse>
      </el-tab-pane>

      <!-- chatgpt -->
      <!-- <el-tab-pane key="chatgpt" label="chatgpt" name="chatgpt">
        <div id="main">
          <mavon-editor :editable="false" :toolbarsFlag="false" :subfield="false" defaultOpen="preview"  :value="chatgpt" style="min-height: 30vh; max-height: 70vh;" :navigation="true" />
        </div>
      </el-tab-pane> -->
    </el-tabs>
  </div>
</template>

<script>
import api from '@/api/chatApi'
import { Editor } from '@wangeditor/editor-for-vue'
export default {
  name: 'App',
  components: { Editor},
  data() {
    return {
      editor: null,
      answer: '',
      toolbarConfig: {},
      editorConfig: { placeholder: '请输入内容...' },
      mode: 'default', // or 'simple'
      editableTabsValue: "faq",
      question: '',
      chatgpt: "等待问题...",
      milvus: "",
      activeName: '1',
      disabled: false,
      milvusResult: [],
      loading:false,
    }
  },
  methods: {
    submit() {
      this.disabled = true;
      this.loading = true;
      api.question({
        question:this.question,
        editableTabsValue:this.editableTabsValue
      }).then(resp => {
        if (resp) {
          this.milvusResult = resp.data
        }
      }).finally(() => {
        this.disabled = false;
        this.loading = false;
      })
    },
    setup() {
      this.$router.push("/save");
    },
    onCreated(editor) {
      this.editor = Object.seal(editor) // 一定要用 Object.seal() ，否则会报错
    },
  },
  beforeDestroy() {
    const editor = this.editor
    if (editor == null) return
    editor.destroy() // 组件销毁时，及时销毁编辑器
  }
}
</script>

<style lang="less" scoped>
.page {
  margin: 0;
  padding: 0px;
  height: 100vh;

  .el-input--mini .el-input__inner {
    height: 40px;
    line-height: 40px;
  }
}
</style>
