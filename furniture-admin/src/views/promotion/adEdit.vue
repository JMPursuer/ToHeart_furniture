<template>
  <div class="app-container">
    <el-form
      ref="dataForm"
      :rules="rules"
      :model="dataForm"
      status-icon
      label-position="left"
      label-width="100px"
    >
      <el-form-item label="广告标题" prop="name">
        <el-input v-model="dataForm.name" />
      </el-form-item>
      <el-form-item label="广告内容" prop="content">
        <el-input v-model="dataForm.content" />
      </el-form-item>
      <el-form-item label="广告图片" prop="url">
        <el-upload
          :headers="headers"
          :action="uploadPath"
          :show-file-list="false"
          :on-success="uploadUrl"
          :before-upload="checkFileSize"
          class="avatar-uploader"
          accept=".jpg,.jpeg,.png,.gif"
        >
          <img v-if="dataForm.url" :src="dataForm.url" class="avatar">
          <i v-else class="el-icon-plus avatar-uploader-icon" />
          <div slot="tip" class="el-upload__tip">
            只能上传jpg/png文件，且不超过1024kb
          </div>
        </el-upload>
      </el-form-item>
      <el-form-item label="广告位置" prop="position">
        <el-select v-model="dataForm.position" placeholder="请选择">
          <el-option :value="1" label="首页" />
        </el-select>
      </el-form-item>
      <el-form-item label="是否启用" prop="enabled">
        <el-select v-model="dataForm.enabled" placeholder="请选择">
          <el-option :value="true" label="启用" />
          <el-option :value="false" label="不启用" />
        </el-select>
      </el-form-item>
      <el-form-item label="广告内容" prop="content">
        <editor v-model="dataForm.content" :init="editorInit" />
      </el-form-item>
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogFormVisible = false">取消</el-button>
      <el-button type="primary" @click="createData">确定</el-button>
    </div>
  </div>
</template>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #20a0ff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}
.avatar {
  width: 145px;
  height: 145px;
  display: block;
}
</style>

<script>
import { createAd, updateAd, readAd } from '@/api/ad';
import { uploadPath, createStorage } from '@/api/storage';
import { getToken } from '@/utils/auth';
import Editor from '@tinymce/tinymce-vue';

export default {
  name: 'Ad',
  components: { Editor },
  data() {
    return {
      uploadPath,
      editorInit: {
        language: 'zh_CN',
        height: '400px',
        convert_urls: false,
        plugins: [
          'advlist anchor autolink autosave code codesample colorpicker colorpicker contextmenu directionality emoticons fullscreen hr image imagetools importcss insertdatetime link lists media nonbreaking noneditable pagebreak paste preview print save searchreplace spellchecker tabfocus table template textcolor textpattern visualblocks visualchars wordcount'
        ],
        toolbar: [
          'searchreplace bold italic underline strikethrough alignleft aligncenter alignright outdent indent  blockquote undo redo removeformat subscript superscript code codesample',
          'hr bullist numlist link image charmap preview anchor pagebreak insertdatetime media table emoticons forecolor backcolor fullscreen'
        ],
        images_upload_handler: function(blobInfo, success, failure) {
          const formData = new FormData();
          formData.append('file', blobInfo.blob());
          createStorage(formData)
            .then(res => {
              success(res.data.data.url);
            })
            .catch(() => {
              failure('上传失败，请重新上传');
            });
        }
      },
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        name: undefined,
        content: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      dataForm: {
        id: undefined,
        name: undefined,
        content: undefined,
        url: undefined,
        link: undefined,
        position: 1,
        enabled: true
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        name: [
          { required: true, message: '广告标题不能为空', trigger: 'blur' }
        ],
        content: [
          { required: true, message: '广告内容不能为空', trigger: 'blur' }
        ],
        url: [{ required: true, message: '广告链接不能为空', trigger: 'blur' }]
      },
      downloadLoading: false
    };
  },
  computed: {
    headers() {
      return {
        'X-Litemall-Admin-Token': getToken()
      };
    }
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      if (this.$route.query.id < 1) {
        return;
      }
      readAd({ id: this.$route.query.id }).then(({ data }) => {
        this.dataForm = data.data;
        console.log(this.dataForm);
      });
    },
    resetForm() {
      this.dataForm = {
        id: undefined,
        name: undefined,
        content: undefined,
        url: undefined,
        position: 1,
        enabled: true
      };
    },
    handleCreate() {
      this.resetForm();
      this.dialogStatus = 'create';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    uploadUrl: function(response) {
      this.dataForm.url = response.data.url;
    },
    checkFileSize: function(file) {
      if (file.size > 1048576) {
        this.$message.error(
          `${file.name}文件大于1024KB，请选择小于1024KB大小的图片`
        );
        return false;
      }
      return true;
    },
    createData() {
      if (this.dataForm.id > 0) {
        this.updateData();
        return;
      }
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          createAd(this.dataForm)
            .then(response => {
              this.list.unshift(response.data.data);
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '创建成功'
              });
              this.$router.push({ path: `/promotion/ad` });
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              });
            });
        }
      });
    },
    updateData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          updateAd(this.dataForm)
            .then(() => {
              for (const v of this.list) {
                if (v.id === this.dataForm.id) {
                  const index = this.list.indexOf(v);
                  this.list.splice(index, 1, this.dataForm);
                  break;
                }
              }
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '更新广告成功'
              });
              this.$router.push({ path: `/promotion/ad` });
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              });
            });
        }
      });
    }
  }
};
</script>
