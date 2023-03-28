<template>
  <div class="page" 
    v-loading="loading"
    element-loading-text="等待响应中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)"
  >
    <div style="width: 50%;left:0;right:0;margin:0 auto;margin-top: 50px;">
      <el-input placeholder="请输入问题" v-model="question">
      </el-input>
    </div>
    <div style="width: 70%;left:0;right:0;margin:0 auto;margin-top: 40px;">回答:</div>
    <!-- <div style="width: 70%;left:0;right:0;margin:0 auto;margin-top: 10px;border: 1px solid #ccc">
      <Toolbar style="border-bottom: 1px solid #ccc" :editor="editor" :defaultConfig="toolbarConfig" :mode="mode" />
      <Editor style="height: 500px; overflow-y: hidden;" v-model="answer" :defaultConfig="editorConfig" :mode="mode"
        @onCreated="onCreated" />
    </div> -->

    <div class="markdown"  style="width: 80%;left:0;right:0;margin:0 auto;margin-top: 10px;border: 1px solid #ccc">
      <mavon-editor v-model="answer" style="min-height: 70vh; max-height: 70vh;" :navigation=true />
    </div>
    <div style="width: 80%;left:0;right:0;margin:0 auto;margin-top: 10px;">
      <el-button style="float: right;" type="primary" @click="save()">保存</el-button>
    </div>

  </div>
</template>
<script>
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import api from '@/api/chatApi'
export default {
  components: { Editor, Toolbar },
  data() {
    return {
      editor: null,
      answer: '',
      toolbarConfig: {},
      editorConfig: { placeholder: '请输入内容...' },
      mode: 'default', // or 'simple'
      question: '',
      loading:false
    }
  },
  methods: {
    onCreated(editor) {
      this.editor = Object.seal(editor) // 一定要用 Object.seal() ，否则会报错
    },
    save() {
      debugger
      if (this.question.trim() == '') {
        this.$message({
          message: '请输入问题',
          type: 'warn'
        });
        return
      }
      if (this.answer.trim() == '') {
        this.$message({
          message: '请输入回答',
          type: 'warn'
        });
        return
      }
      this.loading = true
      api.saveQuestion({
        question: this.question,
        answer:this.answer
      }).then(() => {
        this.$message({
          message: '保存成功',
          type: 'success'
        });
        this.question = ""
        this.answer = ""
      }).finally(()=>{
        this.loading = false
      })
    }
  },
  mounted() {

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

}
</style>
