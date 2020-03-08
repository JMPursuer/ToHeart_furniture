<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.idCard"
        class="filter-item"
        clearable
        placeholder="请输入身份证号码"
        style="width: 200px;"
      />
      <el-input
        v-model="listQuery.mobile"
        class="filter-item"
        clearable
        placeholder="请输入手机号"
        style="width: 200px;"
      />
      <el-button
        class="filter-item"
        icon="el-icon-search"
        type="primary"
        @click="handleFilter"
      >查找</el-button>
      <el-button
        v-permission="['POST /admin/user/add']"
        class="filter-item"
        icon="el-icon-edit"
        type="primary"
        @click="handleCreate"
      >添加</el-button>
      <el-button
        v-permission="['POST /admin/user/all/list']"
        class="filter-item"
        icon="el-icon-s-custom"
        type="primary"
        @click="type === 1 ? (type = 2) : (type = 1)"
      >{{ type === 1 ? '查看所有用户' : '我的用户' }}</el-button>
      <!-- <el-button
        :loading="downloadLoading"
        class="filter-item"
        icon="el-icon-download"
        type="primary"
        @click="handleDownload"
        >导出</el-button
      > -->
    </div>

    <!-- 查询结果 -->
    <el-table
      v-loading="listLoading"
      :data="list"
      border
      element-loading-text="正在查询中。。。"
      fit
      highlight-current-row
    >
      <el-table-column
        align="center"
        label="用户ID"
        prop="userId"
        sortable
        width="100px"
      />
      <el-table-column align="center" label="用户姓名" prop="nickname">
        <template slot-scope="scope">
          <span>{{
            scope.row.type === 1 ? scope.row.nickname : scope.row.company
          }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="手机号码" prop="mobile" />
      <el-table-column align="center" label="证件号" prop="idCard" />
      <el-table-column align="center" label="折扣" prop="discount">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.discount" type="success">
            {{ showDiscount(scope.row.discount) }}折
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="账号类型" prop="type">
        <template slot-scope="scope">
          <el-tag>
            {{ scope.row.type === 1 ? '普通用户' : '公司用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" prop="status">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 0 ? 'success' : 'danger'">{{
            statusDic[scope.row.status]
          }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column
        align="center"
        class-name="small-padding fixed-width"
        label="操作"
        width="240"
      >
        <template v-if="type === 1" slot-scope="scope">
          <el-button
            v-permission="['POST /admin/user/update']"
            size="small"
            type="primary"
            @click="handleUpdate(scope.row)"
          >编辑</el-button>
          <el-button
            v-permission="['POST /admin/user/update/status']"
            size="small"
            type="warning"
            @click="handleDelete(scope.row, 1)"
          >{{ scope.row.status === 0 ? '停用' : '可用' }}</el-button>
          <el-button
            v-permission="['POST /admin/user/update/status']"
            size="small"
            type="danger"
            @click="handleDelete(scope.row, 2)"
          >重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :limit.sync="listQuery.pageSize"
      :page.sync="listQuery.pageIndex"
      :total="total"
      @pagination="getList"
    />
    <!-- 添加或修改对话框 -->
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form
        ref="dataForm"
        :model="dataForm"
        :rules="rules"
        label-position="left"
        label-width="100px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="用户类型" prop="type">
          <el-radio-group v-model="dataForm.type">
            <el-radio :label="1">普通用户</el-radio>
            <el-radio :label="2">公司用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          v-if="dataForm.type === 1"
          label="用户姓名"
          prop="nickname"
        >
          <el-input v-model="dataForm.nickname" />
        </el-form-item>
        <el-form-item v-if="dataForm.type === 1" label="性别" prop="gender">
          <el-radio-group v-model="dataForm.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="dataForm.type === 2">
          <el-form-item label="公司名称" prop="company">
            <el-input v-model="dataForm.company" />
          </el-form-item>
          <el-form-item label="公司地址" prop="companyAddress">
            <el-input v-model="dataForm.companyAddress" />
          </el-form-item>
        </template>
        <el-form-item label="手机号码" prop="mobile">
          <el-input v-model="dataForm.mobile" />
        </el-form-item>
        <el-form-item
          :label="dataForm.type === 1 ? '身份证号码' : '法人身份证'"
          prop="idCard"
        >
          <el-input v-model="dataForm.idCard" />
        </el-form-item>
        <el-form-item label="客户折扣" prop="discount" max="10" min="1">
          <el-input
            v-model="dataForm.discount"
            type="number"
            placeholder="优惠折扣"
          >
            <template slot="append">折</template>
          </el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button
          v-if="dialogStatus == 'create'"
          type="primary"
          @click="createData"
        >确定</el-button>
        <el-button v-else type="primary" @click="updateData">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  fetchList,
  allList,
  addUser,
  updateUser,
  changeUser
} from '@/api/user';
import Pagination from '@/components/Pagination'; // Secondary package based on el-pagination

export default {
  name: 'User',
  components: { Pagination },
  data() {
    return {
      type: 1,
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        password: [
          { required: true, message: '账号密码不能为空', trigger: 'blur' }
        ],
        type: [
          { required: true, message: '账号类型不能为空', trigger: 'blur' }
        ],
        gender: [{ required: true, message: '请选择性别', trigger: 'blur' }],
        nickname: [
          { required: true, message: '用户姓名不能为空', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '登录账号不能为空', trigger: 'blur' }
        ],
        discount: [
          { required: true, message: '客户折扣不能为空', trigger: 'blur' }
        ],
        mobile: [
          { required: true, message: '请输入手机号码', trigger: 'blur' },
          { min: 11, max: 11, message: '请输入11位手机号码', trigger: 'blur' },
          {
            pattern: /^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[0135678]|18[0-9]|19[89])\d{8}$/,
            message: '请输入正确的手机号码'
          }
        ],
        idCard: [
          { required: true, message: '身份证号码不能为空', trigger: 'blur' }
        ],
        company: [
          { required: true, message: '公司名称不能为空', trigger: 'blur' }
        ],
        companyAddress: [
          { required: true, message: '公司地址不能为空', trigger: 'blur' }
        ]
      },
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        pageIndex: 1,
        pageSize: 20,
        idCard: '',
        mobile: ''
      },
      downloadLoading: false,
      genderDic: ['未知', '男', '女'],
      levelDic: ['普通用户', 'VIP用户', '高级VIP用户'],
      statusDic: ['可用', '禁用', '注销', '未修改登录密码'],
      dialogStatus: '',
      dialogFormVisible: false,
      dataForm: {}
    };
  },
  watch: {
    type() {
      this.listQuery.pageIndex = 1;
      this.getList();
    }
  },
  created() {
    this.getList();
  },
  methods: {
    showDiscount(discount) {
      return discount * 10;
    },
    resetForm() {
      this.dataForm = {
        company: undefined,
        companyAddress: undefined,
        idCard: undefined,
        mobile: undefined,
        password: undefined,
        discount: undefined,
        type: 1,
        gender: undefined,
        nickname: undefined
      };
    },
    handleUpdate(row) {
      this.dataForm = Object.assign({}, row, {
        discount: row.discount * 10
      });
      this.dialogStatus = 'update';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    handleDelete(row, type) {
      const message =
        type === 1
          ? status === 0
            ? '是否停用用户？'
            : '是否启用用户？'
          : '是否为用户重置密码？';
      this.$confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        if (type === 2) {
          changeUser({ status: 3, userId: row.userId })
            .then(response => {
              row.status = status;
              this.$notify.success({
                title: '成功',
                message: '已为用户重置密码'
              });
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.msg
              });
            });
          return;
        }
        const status = row.status === 1 ? 0 : 1;
        changeUser({ status, userId: row.userId })
          .then(response => {
            row.status = status;
            this.$notify.success({
              title: '成功',
              message: status === 0 ? '用户已停止使用' : '用户已激活继续使用'
            });
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.msg
            });
          });
      });
    },
    createData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.dataForm.discount = Number(this.dataForm.discount) / 10;
          addUser(this.dataForm)
            .then(response => {
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '添加用户成功'
              });
              this.getList();
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.msg
              });
            });
        }
      });
    },
    updateData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          const data = {
            userId: this.dataForm.userId,
            company: this.dataForm.company,
            companyAddress: this.dataForm.companyAddress,
            idCard: this.dataForm.idCard,
            mobile: this.dataForm.mobile,
            password: this.dataForm.password,
            type: this.dataForm.type,
            gender: this.dataForm.gender,
            nickname: this.dataForm.nickname,
            discount: Number(this.dataForm.discount) / 10
          };
          updateUser(data)
            .then(() => {
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '更新管理员成功'
              });
              this.getList();
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.msg
              });
            });
        }
      });
    },
    handleCreate() {
      this.resetForm();
      this.dialogStatus = 'create';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    getList() {
      this.listLoading = true;
      if (this.type === 1) {
        fetchList(this.listQuery)
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
      } else {
        allList(this.listQuery)
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
      }
    },
    handleFilter() {
      this.listQuery.pageIndex = 1;
      this.getList();
    },
    handleDownload() {
      this.downloadLoading = true;
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = ['用户名', '手机号码', '性别', '状态'];
        const filterVal = ['username', 'mobile', 'gender', 'status'];
        excel.export_json_to_excel2(tHeader, this.list, filterVal, '用户信息');
        this.downloadLoading = false;
      });
    }
  }
};
</script>
