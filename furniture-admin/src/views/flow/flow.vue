<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
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
      <el-table-column align="center" label="用户姓名" prop="nickname" />
      <el-table-column align="center" label="手机号码" prop="mobile" />
      <el-table-column align="center" label="余额(元)" prop="balance">
        <template
          slot-scope="scope"
        >{{ scope.row.balance.toFixed(2) }}元</template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding" label="操作">
        <template slot-scope="scope">
          <el-button
            v-permission="['POST /admin/wallet/flow/list']"
            type="warning"
            @click="flowListFun(scope.row)"
          >明细</el-button>
          <el-button
            v-permission="['POST /admin/wallet/topup']"
            type="primary"
            @click="handleUpdate(scope.row)"
          >充值</el-button>
          <el-button
            v-permission="['POST /admin/wallet/withdraw']"
            type="danger"
            @click="handlewithdraw(scope.row)"
          >提现</el-button>
          <el-button
            v-permission="['POST /admin/wallet/password/delete']"
            type="danger"
            @click="changePwd(scope.row)"
          >忘记支付密码</el-button>
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
        v-if="dialogStatus !== 'list'"
        ref="dataForm"
        :model="dataForm"
        :rules="rules"
        label-position="left"
        label-width="100px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="目前余额">
          <span>{{ flowQuery.balance.toFixed(2) }}元</span>
        </el-form-item>
        <el-form-item :label="textMap[dialogStatus] + '金额'" prop="amount">
          <el-input v-model="dataForm.amount" type="number">
            <template slot="append">元</template>
          </el-input>
        </el-form-item>
        <el-form-item label="备注" prop="note">
          <el-input v-model="dataForm.note" />
        </el-form-item>
      </el-form>
      <template v-else>
        <el-table
          v-loading="listLoading"
          :data="flowList"
          border
          element-loading-text="正在查询中。。。"
          fit
          highlight-current-row
        >
          <el-table-column
            align="center"
            label="当前余额"
            prop="balance"
            width="100px"
          />
          <el-table-column
            align="center"
            label="创建时间"
            prop="createTime"
            sortable
          >
            <template slot-scope="scope">{{
              getTime(scope.row.createTime)
            }}</template>
          </el-table-column>
          <el-table-column align="center" label="流水记录" prop="flow" sortable>
            <template slot-scope="scope">
              <el-tag
                :type="scope.row.flow > 0 ? '' : 'danger'"
              >{{ scope.row.flow.toFixed(2) }}元</el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作者" prop="optName" />
          <el-table-column align="center" label="描述" prop="desc" />
          <el-table-column align="center" label="备注" prop="note" />
        </el-table>

        <pagination
          v-show="flowQuery.total > 0"
          :limit.sync="flowQuery.pageSize"
          :page.sync="flowQuery.pageIndex"
          :total="flowQuery.total"
          @pagination="getFlowList"
        />
      </template>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <template>
          <el-button
            v-if="dialogStatus == 'create' && flowQuery.balance > 0"
            type="primary"
            @click="createData"
          >确定</el-button>
          <el-button
            v-else-if="dialogStatus == 'list'"
            type="primary"
            @click="dialogFormVisible = false"
          >确定</el-button>
          <el-button
            v-if="dialogStatus == 'update'"
            type="primary"
            @click="updateData"
          >确定</el-button>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { fetchList, withdraw, topup, flowList, delPassword } from '@/api/flow';
import Pagination from '@/components/Pagination'; // Secondary package based on el-pagination
import moment from 'moment';
export default {
  name: 'Flow',
  components: { Pagination },
  data() {
    return {
      textMap: {
        update: '充值',
        create: '提现',
        list: '流水明细'
      },
      rules: {
        amount: [{ required: true, message: '金额不能为空', trigger: 'blur' }],
        note: [{ required: true, message: '备注不能为空', trigger: 'blur' }]
      },
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        pageIndex: 1,
        pageSize: 20,
        mobile: ''
      },
      flowQuery: {
        pageIndex: 1,
        pageSize: 20,
        total: 0,
        userId: 0,
        balance: 0
      },
      flowList: [],
      dialogStatus: '',
      dialogFormVisible: false,
      dataForm: {
        amount: '',
        userId: 0,
        note: ''
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getTime(time) {
      return time ? moment(time).format('YYYY-MM-DD HH:mm:ss') : '';
    },
    showDiscount(discount) {
      return discount * 10;
    },
    /**
     * 流水明细
     */
    flowListFun(row) {
      this.dialogStatus = 'list';
      this.dialogFormVisible = true;
      this.flowQuery.userId = row.userId;
      this.flowQuery.balance = row.balance;
      this.getFlowList();
    },
    handleUpdate(row) {
      this.dataForm.userId = row.userId;
      this.flowQuery.balance = row.balance;
      this.dataForm.note = '';
      this.dataForm.amount = '';
      this.dialogStatus = 'update';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    handlewithdraw(row) {
      if (row.balance <= 0) {
        this.$message.error('余额不足不可提现');
        return;
      }
      this.flowQuery.balance = row.balance;
      this.dataForm.userId = row.userId;
      this.dataForm.amount = '';
      this.dataForm.note = '';
      this.dialogStatus = 'create';
      this.dialogFormVisible = true;
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate();
      });
    },
    createData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          this.dataForm.amount = Number(this.dataForm.amount);
          withdraw(this.dataForm)
            .then(response => {
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '提现成功'
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
          this.dataForm.amount = Number(this.dataForm.amount);
          topup(this.dataForm)
            .then(() => {
              this.dialogFormVisible = false;
              this.$notify.success({
                title: '成功',
                message: '充值成功'
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
    changePwd(item) {
      this.$confirm(`是否已得到用户:${item.nickname}授权？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        delPassword({ userId: item.userId })
          .then(res => {
            this.$notify.success({
              title: '成功',
              message: '已为用户重置密码'
            });
          })
          .catch(e => {
            this.$notify.error({
              title: '失败',
              message: e.data.msg
            });
          });
      });
    },
    getFlowList() {
      this.listLoading = true;
      flowList({
        pageIndex: this.flowQuery.pageIndex,
        pageSize: this.flowQuery.pageSize,
        userId: this.flowQuery.userId
      })
        .then(response => {
          this.flowList = response.data.data.list;
          this.flowQuery.total = response.data.data.total;
          this.listLoading = false;
        })
        .catch(() => {
          this.flowList = [];
          this.flowQuery.total = 0;
          this.listLoading = false;
        });
    },
    getList() {
      this.listLoading = true;
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
    },
    handleFilter() {
      this.listQuery.pageIndex = 1;
      this.getList();
    }
  }
};
</script>
