<template>
  <div class="page" 
    v-loading="loading"
    element-loading-text="等待响应中"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)"
  >
  <!-- 搜索区域 -->
    <div style="width: 50%;left:0;right:0;margin:0 auto;margin-top: 50px;display: flex;">
      <el-input  placeholder="请输入问题" v-model="question">
      </el-input>
      <el-select v-model="answerTypeValue">
        <el-option
          v-for="item in answerTypeList"
          :key="item.value"
          :label="item.label"
          :value="item.value">
        </el-option>
      </el-select>
    </div>
    <!-- 回答区域 -->
    <div style="width: 70%;left:0;right:0;margin:0 auto;margin-top: 40px;">回答:</div>
    <!-- 富文本格式 -->
    <div v-if="answerTypeValue == 'editor'" style="width: 70%;left:0;right:0;margin:0 auto;margin-top: 10px;border: 1px solid #ccc">
      <Toolbar style="border-bottom: 1px solid #ccc" :editor="editor" :defaultConfig="toolbarConfig" :mode="mode" />
      <Editor style="height: 500px; overflow-y: hidden;" v-model="answerEditor" :defaultConfig="editorConfig" :mode="mode"
        @onCreated="onCreated" />
    </div>
    <!-- markdown格式 -->
    <div v-if="answerTypeValue == 'markdown'" class="markdown"  style="width: 80%;left:0;right:0;margin:0 auto;margin-top: 10px;border: 1px solid #ccc">
      <mavon-editor :boxShadow="false" v-model="answerMarkdown" style="min-height: 570px; max-height: 70vh;" :navigation=true />
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
      answerMarkdown: '',
      answerEditor: '',
      toolbarConfig: {},
      editorConfig: { placeholder: '请输入内容...' },
      mode: 'default', // or 'simple'
      question: '',
      loading:false,
      answerTypeList: [{
          value: 'markdown',
          label: 'markdown'
        },{
          value: 'editor',
          label: '富文本'
        }
      ],
      answerTypeValue: 'editor'
    }
  },
  methods: {
    onCreated(editor) {
      this.editor = Object.seal(editor) // 一定要用 Object.seal() ，否则会报错
    },
    save() {
      if (this.question.trim() == '') {
        this.$message({
          message: '请输入问题',
          type: 'warn'
        });
        return
      }
      let answer = this.answerEditor
      if (this.answerTypeValue == 'markdown') {
        answer = this.answerMarkdown
      }
      if (answer.trim() == '') {
        this.$message({
          message: '请输入回答',
          type: 'warn'
        });
        return
      }
      this.loading = true
      api.saveQuestion({
        question: this.question,
        answerTypeValue: this.answerTypeValue,
        answer:answer
      }).then(() => {
        this.$message({
          message: '保存成功',
          type: 'success'
        });
      }).finally(()=>{
        this.loading = false
        this.question = ""
        this.answer = ""
        this.answerEditor = ""
        this.answerMarkdown = ""
      })
    }
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
