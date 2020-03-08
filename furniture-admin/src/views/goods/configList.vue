<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input
        v-model="listQuery.code"
        clearable
        class="filter-item"
        style="width: 160px;"
        placeholder="编码"
      />
      <el-select
        v-model="listQuery.status"
        class="filter-item"
        clearable
        placeholder="请选择状态"
      >
        <el-option label="已使用(Y)" :value="1" />
        <el-option label="未使用(N)" :value="0" />
      </el-select>
      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >查找</el-button>
      <!-- <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-edit"
        @click="handleCreate"
      >添加</el-button> -->
      <!-- <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button> -->
    </div>
    <el-tabs v-model="activeName">
      <el-tab-pane label="使用中的配置" name="1">
        <el-table
          v-loading="listLoading"
          :data="Uselist"
          element-loading-text="正在查询中。。。"
          border
          fit
          highlight-current-row
        >
          <el-table-column align="center" label="id" prop="id" />
          <el-table-column align="center" label="编码" prop="code" />
          <el-table-column align="center" label="图片" prop="image">
            <template slot-scope="scope">
              <img :src="scope.row.image" width="40">
            </template>
          </el-table-column>
          <el-table-column align="center" label="面料" prop="material" />
          <el-table-column align="center" label="价格" prop="price" />
          <el-table-column align="center" label="状态" prop="status">
            <template slot-scope="scope">
              {{ scope.row.status === 1 ? '已使用（Y）' : '未使用（N）' }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="类型" prop="type" />
          <el-table-column align="center" label="备注" prop="desc" />
          <el-table-column
            align="center"
            label="操作"
            width="300"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope">
              <el-button
                v-permission="['POST /admin/goods/option/price/update']"
                type="primary"
                @click="changeGood(scope.row, 1)"
              >修改价格</el-button>
              <el-button
                v-permission="['POST /admin/goods/option/update']"
                type="danger"
                @click="changeGood(scope.row, 2)"
              >修改配置</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="配置表操作" name="0">
        <div class="filter-container">
          <el-upload
            v-if="!useLoading"
            v-permission="['POST /admin/goods/option/upload']"
            class="filter-item"
            action=""
            accept=".xls,.xlsx"
            :show-file-list="false"
            multiple
            :limit="1"
            :http-request="configUpload"
          >
            <el-button type="primary">上传配置表</el-button>
          </el-upload>
          <template v-else>
            <el-button
              v-permission="['POST /admin/goods/option/use']"
              type="primary"
              class="filter-item"
              @click="handleUpdate(1)"
            >使用该次配置</el-button>
            <el-button
              v-permission="['POST /admin/goods/option/delete']"
              type="danger"
              class="filter-item"
              @click="handleUpdate(2)"
            >不使用该次配置</el-button>
          </template>
          <el-button
            v-permission="['POST /admin/goods/option/download']"
            class="filter-item"
            type="primary"
            style="float:right;"
            icon="el-icon-edit"
            :loading="downloadLoading"
            @click="handleDownload"
          >下载配置</el-button>
          <!-- <el-button :loading="downloadLoading" class="filter-item" type="primary" icon="el-icon-download" @click="handleDownload">导出</el-button> -->
        </div>

        <!-- 查询结果 -->
        <h3 v-if="list.length > 0">存在未确认的配置信息</h3>
        <el-table
          v-if="list.length > 0"
          v-loading="listLoading"
          :data="list"
          element-loading-text="正在查询中。。。"
          border
          fit
          highlight-current-row
        >
          <el-table-column align="center" label="id" prop="id" />
          <el-table-column align="center" label="编码" prop="code" />
          <el-table-column align="center" label="图片" prop="image">
            <template slot-scope="scope">
              <img :src="scope.row.image" width="40">
            </template>
          </el-table-column>
          <el-table-column align="center" label="面料" prop="material" />
          <el-table-column align="center" label="价格" prop="price" />
          <el-table-column align="center" label="状态" prop="status">
            <template slot-scope="scope">
              {{ scope.row.status === 1 ? '已使用（Y）' : '未使用（N）' }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="类型" prop="type" />
          <el-table-column align="center" label="备注" prop="desc" />
          <el-table-column
            align="center"
            label="操作"
            width="300"
            class-name="small-padding fixed-width"
          >
            <template slot-scope="scope">
              <el-button
                v-permission="['POST /admin/goods/option/price/update']"
                type="primary"
                @click="changeGood(scope.row, 1)"
              >修改价格</el-button>
              <el-button
                v-permission="['POST /admin/goods/option/update']"
                type="danger"
                @click="changeGood(scope.row, 2)"
              >修改配置</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog :visible.sync="priceDialogVisible" title="修改价格">
      <el-form
        ref="priceForm"
        :model="priceForm"
        label-position="left"
        label-width="100px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="编码" prop="code">
          <span>{{ priceForm.code }}</span>
        </el-form-item>
        <el-form-item label="产品名称" prop="price">
          <span>{{ priceForm.desc }} {{ type }}</span>
        </el-form-item>
        <el-form-item v-if="type === 1" label="金额" prop="price">
          <el-input v-model="priceForm.price" />
        </el-form-item>
        <template v-else>
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="priceForm.status">
              <el-radio :label="1">有库存</el-radio>
              <el-radio :label="0">无库存</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="图片">
            <el-upload
              :headers="headers"
              :action="uploadPath"
              :show-file-list="false"
              :on-success="uploadPicUrl"
              class="avatar-uploader"
              accept=".jpg,.jpeg,.png,.gif"
            >
              <img
                v-if="priceForm.image"
                :src="priceForm.image"
                class="avatar"
              >
              <i v-else class="el-icon-plus avatar-uploader-icon" />
            </el-upload>
          </el-form-item>
        </template>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="priceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="changeGoods">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<style>
.table-expand {
  font-size: 0;
}
.table-expand label {
  width: 100px;
  color: #99a9bf;
}
.table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
}
.avatar {
  width: 145px;
  height: 145px;
}
.gallery {
  width: 80px;
  margin-right: 10px;
}
.goods-detail-box img {
  width: 100%;
}
</style>

<script>
import {
  optionList,
  optionDownLoad,
  optionUpload,
  optionUse,
  optionDel,
  priceUpdate,
  configUpdate
} from '@/api/goods';

import { getToken } from '@/utils/auth';
import { uploadPath } from '@/api/storage';
export default {
  name: 'GoodsList',
  components: {},
  data() {
    return {
      listQuery: {
        status: undefined,
        code: undefined
      },
      uploadPath,
      priceDialogVisible: false,
      type: 1,
      priceForm: {
        id: undefined,
        type: undefined,
        material: undefined,
        code: undefined,
        image: undefined,
        desc: undefined,
        status: undefined,
        price: undefined,
        isUse: undefined
      },
      list: [],
      Uselist: [],
      activeName: '1',
      listLoading: false,
      useLoading: false,
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
  watch: {
    activeName(value) {
      this.getList();
    }
  },
  created() {
    this.getList();
  },
  methods: {
    uploadPicUrl(response) {
      this.priceForm.image = response.data.url;
    },
    changeGoods() {
      const message =
        Number(this.activeName) === 1
          ? '本次修改会直接更改系统参数,是否确认修改?'
          : '本次修改会在使用配置后更改,是否确认修改？';
      this.$confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (this.type === 1) {
          priceUpdate({
            id: this.priceForm.id,
            price: Number(this.priceForm.price)
          }).then(res => {
            this.$notify.success({
              title: '成功',
              message: '修改成功'
            });
            this.priceDialogVisible = false;
            this.getList();
          });
          return;
        }
        configUpdate({
          id: this.priceForm.id,
          image: this.priceForm.image,
          status: this.priceForm.status
        }).then(res => {
          this.$notify.success({
            title: '成功',
            message: '修改成功'
          });
          this.priceDialogVisible = false;
          this.getList();
        });
      });
    },
    changeGood(item, type) {
      this.priceForm = Object.assign({}, item);
      this.type = type;
      this.priceDialogVisible = true;
    },
    getList() {
      this.listLoading = true;
      optionList({ isUse: Number(this.activeName) })
        .then(res => {
          this.listLoading = false;
          if (Number(this.activeName) === 1) {
            this.Uselist = res.data.data;
            this.Uselist = this.Uselist.filter(res => {
              if (this.listQuery.status >= 0) {
                if (this.listQuery.status === res.status) {
                  return true;
                } else {
                  return false;
                }
              } else {
                return true;
              }
            });
            this.Uselist = this.Uselist.filter(res => {
              if (this.listQuery.code) {
                return res.code.indexOf(this.listQuery.code) !== -1;
              } else {
                return true;
              }
            });
            return;
          }
          this.list = res.data.data;
          this.list = this.list.filter(res => {
            if (this.listQuery.status >= 0) {
              if (this.listQuery.status === res.status) {
                return true;
              } else {
                return false;
              }
            } else {
              return true;
            }
          });
          this.list = this.list.filter(res => {
            if (this.listQuery.code) {
              return res.code.indexOf(this.listQuery.code) !== -1;
            } else {
              return true;
            }
          });
          if (res.data.data.length > 0) {
            this.useLoading = true;
          } else {
            this.useLoading = false;
          }
        })
        .catch(() => {
          this.list = [];
          this.listLoading = false;
          this.useLoading = false;
        });
    },
    configUpload(item) {
      const fileObj = item.file;
      // FormData 对象
      const form = new FormData();
      // 文件对象
      form.append('file', fileObj);
      optionUpload(form)
        .then(res => {
          this.getList();
        })
        .catch(e => {
          this.$notify.error({
            title: '失败',
            message: e.data.msg
          });
        });
    },
    handleUpdate(type) {
      if (type === 1) {
        optionUse().then(res => {
          this.$notify.success({
            title: '成功',
            message: '已使用该次配置'
          });
          this.getList();
        });
      } else {
        optionDel().then(res => {
          this.$notify.success({
            title: '成功',
            message: '已放弃此次配置'
          });
          this.getList();
        });
      }
    },
    handleFilter() {
      this.getList();
    },
    handleDownload() {
      this.downloadLoading = true;
      optionDownLoad()
        .then(res => {
          const fileName = '产品配置表';
          const blob = new Blob([res]);
          const link = document.createElement('a');

          link.href = window.URL.createObjectURL(blob);

          link.download = fileName + `.xls`;

          link.click();
          this.downloadLoading = false;
          this.$notify.success({
            title: '成功',
            message: '下载成功'
          });
        })
        .catch(e => {
          this.downloadLoading = false;
        });
    }
  }
};
</script>
