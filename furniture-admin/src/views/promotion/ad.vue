<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.name"
        clearable
        class="filter-item"
        style="width: 200px;"
        placeholder="请输入广告标题"
      />
      <el-button
        v-permission="['POST /admin/ad/list']"
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >查找</el-button>
      <el-button
        v-permission="['POST /admin/ad/create']"
        class="filter-item"
        type="primary"
        icon="el-icon-edit"
        @click="handleCreate"
      >添加</el-button>
      <!-- <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button> -->
    </div>

    <!-- 查询结果 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      element-loading-text="正在查询中。。。"
      border
      fit
      highlight-current-row
    >
      <el-table-column align="center" label="广告ID" prop="id" sortable />

      <el-table-column align="center" label="广告标题" prop="name" />
      <el-table-column align="center" label="广告图片" prop="url">
        <template slot-scope="scope">
          <img v-if="scope.row.url" :src="scope.row.url" width="80">
        </template>
      </el-table-column>
      <el-table-column align="center" label="更新日期" prop="updateTime" />
      <!-- <el-table-column align="center" label="广告位置" prop="position" /> -->

      <!-- <el-table-column align="center" label="活动链接" prop="link" /> -->

      <el-table-column align="center" label="是否启用" prop="enabled">
        <template slot-scope="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'error'">{{
            scope.row.enabled ? '启用' : '不启用'
          }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column
        align="center"
        label="操作"
        width="200"
        class-name="small-padding fixed-width"
      >
        <template slot-scope="scope">
          <el-button
            v-permission="['POST /admin/ad/update']"
            type="primary"
            size="mini"
            @click="handleUpdate(scope.row)"
          >编辑</el-button>
          <el-button
            v-permission="['POST /admin/ad/delete']"
            type="danger"
            size="mini"
            @click="handleDelete(scope.row)"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="listQuery.pageIndex"
      :limit.sync="listQuery.pageSize"
      @pagination="getList"
    />
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
import { listAd, deleteAd } from '@/api/ad';
import { uploadPath, createStorage } from '@/api/storage';
import { getToken } from '@/utils/auth';
import Pagination from '@/components/Pagination'; // Secondary package based on el-pagination
export default {
  name: 'Ad',
  components: { Pagination },
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
        pageIndex: 1,
        pageSize: 20,
        name: ''
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
    this.getList();
  },
  methods: {
    getList() {
      this.listLoading = true;
      listAd(this.listQuery)
        .then(response => {
          this.list = response.data.data.list;
          this.total = response.data.data.total;
          this.listLoading = false;
        })
        .catch(() => {
          this.list = [];
          this.total = 0;
          this.listLoading = false;
        });
    },
    handleFilter() {
      this.listQuery.pageIndex = 1;
      this.getList();
    },
    resetForm() {
      this.dataForm = {
        id: undefined,
        name: undefined,
        content: undefined,
        url: undefined,
        link: undefined,
        position: 1,
        enabled: true
      };
    },
    handleCreate() {
      this.$router.push({ path: `/promotion/adEdit?id=0` });
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
    handleUpdate(row) {
      this.$router.push({ path: `/promotion/adEdit?id=${row.id}` });
    },
    handleDelete(row) {
      this.$confirm('是否删除此广告?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteAd(row)
          .then(response => {
            this.$notify.success({
              title: '成功',
              message: '删除成功'
            });
            const index = this.list.indexOf(row);
            this.list.splice(index, 1);
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            });
          });
      });
    }
  }
};
</script>
