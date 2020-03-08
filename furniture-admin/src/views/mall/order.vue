<template>
  <div class="app-container">
    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input
        v-model="listQuery.nickname"
        class="filter-item"
        clearable
        placeholder="请输入用户名字"
        style="width: 200px;"
      />
      <el-input
        v-model="listQuery.orderSn"
        class="filter-item"
        clearable
        placeholder="请输入订单编号"
        style="width: 200px;"
      />
      <el-select
        v-model="listQuery.orderStatus"
        class="filter-item"
        placeholder="请选择订单状态"
        style="width: 200px"
      >
        <el-option
          v-for="(key, value) in statusMap"
          :key="key"
          :label="key"
          :value="value"
        />
      </el-select>
      <el-button
        v-permission="['POST /admin/order/list', 'POST /admin/order/all/list']"
        class="filter-item"
        icon="el-icon-search"
        type="primary"
        @click="handleFilter"
      >查找</el-button>
      <el-button
        v-permission="['GET /admin/order/all/list']"
        class="filter-item"
        icon="el-icon-s-custom"
        type="primary"
        @click="type === 1 ? (type = 2) : (type = 1)"
      >{{ type === 1 ? '查看所有订单数据' : '我的订单数据' }}</el-button>
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
        label="订单编号"
        min-width="100"
        prop="orderSn"
      />

      <!-- <el-table-column align="center" label="用户ID" prop="userId" /> -->
      <el-table-column align="center" label="用户名称" prop="nickname" />

      <el-table-column align="center" label="订单状态" prop="orderStatus">
        <template slot-scope="scope">
          <el-tag>{{ scope.row.orderStatus | orderStatusFilter }}</el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="订单金额" prop="orderPrice" />

      <el-table-column align="center" label="支付金额" prop="actualPrice" />

      <el-table-column align="center" label="支付时间" prop="payTime" />

      <el-table-column align="center" label="物流单号" prop="shipSn" />

      <el-table-column align="center" label="物流渠道" prop="shipChannel" />

      <el-table-column
        align="left"
        class-name="small-padding fixed-width"
        label="操作"
        width="400"
      >
        <template slot-scope="scope">
          <el-button
            v-permission="['GET /admin/order/detail']"
            size="small"
            type="primary"
            @click="handleDetail(scope.row)"
          >详情</el-button>
          <el-button
            v-if="scope.row.orderStatus == 201"
            v-permission="['POST /admin/order/delivery/update']"
            size="small"
            type="primary"
            @click="deliveryUpdate(scope.row)"
          >修改交付日期</el-button>
          <template v-if="type === 1">
            <el-button
              v-if="scope.row.orderStatus == 203"
              v-permission="['POST /admin/order/ship']"
              size="small"
              type="primary"
              @click="handleShip(scope.row)"
            >发货</el-button>
            <el-button
              v-if="scope.row.orderStatus == 201"
              v-permission="['POST /admin/order/finalpay']"
              size="small"
              type="primary"
              @click="finalPay(scope.row)"
            >确认订单</el-button>
            <el-button
              v-if="
                scope.row.orderStatus == 202 || scope.row.orderStatus == 206
              "
              v-permission="['POST /admin/order/owe/finalpay/update']"
              size="small"
              type="danger"
              @click="finalPayShip(scope.row)"
            >欠尾款</el-button>
            <el-button
              v-if="
                scope.row.orderStatus == 203 ||
                  scope.row.orderStatus == 201 ||
                  scope.row.orderStatus == 202 ||
                  scope.row.orderStatus == 204 ||
                  scope.row.orderStatus == 206
              "
              v-permission="['POST /admin/order/refund']"
              size="small"
              type="danger"
              @click="handleRefund(scope.row)"
            >退款</el-button>
          </template>
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

    <!-- 订单详情对话框 -->
    <el-dialog :visible.sync="orderDialogVisible" title="订单详情" width="800">
      <section ref="print">
        <el-form
          class="detail"
          :data="orderDetail"
          label-position="left"
          label-width="100px"
        >
          <el-form-item label="订单编号">
            <span>{{ orderDetail.order.orderSn }}</span>
          </el-form-item>
          <el-form-item label="订单状态">
            <el-tag>
              {{ orderDetail.order.orderStatus | orderStatusFilter }}
            </el-tag>
          </el-form-item>
          <el-form-item label="订单用户">
            <span>{{ orderDetail.user.nickname }}</span>
          </el-form-item>
          <el-form-item label="用户留言">
            <span>{{ orderDetail.order.message }}</span>
          </el-form-item>
          <el-form-item label="收货信息">
            <div><el-tag>收货人:</el-tag>{{ orderDetail.order.consignee }}</div>
            <div><el-tag>手机号：</el-tag>{{ orderDetail.order.mobile }}</div>
            <div><el-tag>地址：</el-tag>{{ orderDetail.order.address }}</div>
          </el-form-item>
          <el-form-item label="商品信息">
            <el-table
              :data="orderDetail.orderGoods"
              border
              fit
              highlight-current-row
            >
              <el-table-column
                align="center"
                label="商品名称"
                prop="goodsName"
              />
              <el-table-column align="center" label="商品编号" prop="goodsSn" />
              <el-table-column
                align="center"
                label="货品规格"
                prop="specifications"
              />
              <el-table-column align="center" label="货品价格" prop="price" />
              <el-table-column align="center" label="货品数量" prop="number" />
              <el-table-column align="center" label="货品图片" prop="picUrl">
                <template slot-scope="scope">
                  <img :src="scope.row.picUrl" width="40">
                </template>
              </el-table-column>
            </el-table>
          </el-form-item>
          <el-form-item label="费用信息">
            <div class="free">
              <div>
                <el-tag>商品总价:</el-tag>¥{{ orderDetail.order.goodsPrice }}元
              </div>
              <div>
                <el-tag>实际金额:</el-tag>¥{{ orderDetail.order.actualPrice }}元
              </div>
              <div>
                <el-tag>首款金额:</el-tag>¥{{ orderDetail.order.prepayPrice }}元
              </div>
              <div>
                <el-tag>尾款金额:</el-tag>¥{{
                  orderDetail.order.finalpayPrice
                }}元
              </div>
              <template v-if="orderDetail.order.orderStatus === 206">
                <div>
                  <el-tag>已支付尾款金额:</el-tag>¥{{
                    orderDetail.order.paidFinalpay
                  }}元
                </div>
                <div>
                  <el-tag>当前要付的尾款金额:</el-tag>¥{{
                    orderDetail.order.oweFinalpay
                  }}元
                </div>
                <div>
                  <el-tag>还欠尾款金额:</el-tag>¥{{
                    orderDetail.order.dueFinalpay
                  }}元
                </div>
              </template>
            </div>
          </el-form-item>
          <el-form-item label="支付信息">
            <!-- <span>（支付渠道）微信支付</span> -->
            <div><el-tag>支付时间:</el-tag>{{ orderDetail.order.payTime }}</div>
            <div>
              <el-tag>最后交付日期:</el-tag>{{ orderDetail.order.deliveryDate | getTime }}
            </div>
          </el-form-item>
          <el-form-item label="快递信息">
            <div>
              <el-tag>快递公司:</el-tag>{{ orderDetail.order.shipChannel }}
            </div>
            <div><el-tag>快递单号:</el-tag>{{ orderDetail.order.shipSn }}</div>
            <div>
              <el-tag>发货时间:</el-tag>{{ orderDetail.order.shipTime }}
            </div>
          </el-form-item>
          <el-form-item
            v-if="orderDetail.order.refundAmount > 0"
            label="退款信息"
          >
            <div>
              <el-tag>退款金额:</el-tag>{{ orderDetail.order.refundAmount }}元
            </div>
            <div>
              <el-tag>退款类型:</el-tag>{{ orderDetail.order.refundType }}
            </div>
            <div>
              <el-tag>退款备注:</el-tag>{{ orderDetail.order.refundContent }}
            </div>
            <div>
              <el-tag>退款时间:</el-tag>{{ orderDetail.order.refundTime }}
            </div>
          </el-form-item>
          <el-form-item v-if="orderDetail.order.confirmTime" label="收货信息">
            <div>
              <el-tag>确认收货时间:</el-tag>{{ orderDetail.order.confirmTime }}
            </div>
          </el-form-item>
        </el-form>
      </section>
      <span slot="footer" class="dialog-footer">
        <el-button @click="orderDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="printOrder">打 印</el-button>
      </span>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog :visible.sync="shipDialogVisible" title="发货">
      <el-form
        ref="shipForm"
        :model="shipForm"
        label-position="left"
        label-width="100px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="快递公司" prop="shipChannel">
          <el-select v-model="shipForm.shipChannel" placeholder="请选择">
            <el-option
              v-for="item in channels"
              :key="item.code"
              :label="item.name"
              :value="item.code"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="快递编号" prop="shipSn">
          <el-input v-model="shipForm.shipSn" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmShip">确定</el-button>
      </div>
    </el-dialog>

    <!-- 退款对话框 -->
    <el-dialog :visible.sync="refundDialogVisible" title="退款">
      <el-form
        ref="refundForm"
        :model="refundForm"
        label-position="left"
        label-width="100px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="退款金额" prop="refundMoney">
          <el-input v-model="refundForm.refundMoney" :disabled="true" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="refundDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund">确定</el-button>
      </div>
    </el-dialog>

    <!-- 欠付尾款弹窗 -->
    <el-dialog :visible.sync="finalPayDialogVisible" title="退款">
      <el-form
        ref="finalForm"
        :model="finalPayItem"
        label-position="left"
        label-width="200px"
        status-icon
        style="width: 400px; margin-left:50px;"
      >
        <el-form-item label="当前需付尾款金额">
          <span>¥{{ fmoney(finalPayItem.finalpayPrice, 2) }}</span>
        </el-form-item>
        <el-form-item
          label="当前支付尾款金额"
          prop="dueFinalpay"
          :rules="rulesPackage.dueFinalpay"
        >
          <el-input v-model="finalPayItem.dueFinalpay" type="number" />
        </el-form-item>
        <el-form-item label="最后支付尾款金额">
          <span>¥{{
            finalPayItem.finalpayPrice - finalPayItem.dueFinalpay > 0
              ? fmoney(
                finalPayItem.finalpayPrice - finalPayItem.dueFinalpay,
                2
              )
              : 0
          }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="finalPayDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmFinal">确定</el-button>
      </div>
    </el-dialog>
    <!-- 修改交付日期 -->
    <el-dialog title="修改交付日期" :visible.sync="delivery.dialog">
      <el-form
        ref="deliveryForm"
        :model="delivery.row"
        label-width="160px"
        :rules="rule"
      >
        <el-form-item label="选择交付日期" prop="deliveryDate">
          <el-date-picker
            v-model="delivery.row.deliveryDate"
            format="yyyy 年 MM 月 dd 日"
            value-format="timestamp"
            type="date"
            :picker-options="pickerOptions"
            placeholder="选择日期"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="delivery.dialog = false">取 消</el-button>
        <el-button type="primary" @click="deliverySure">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  detailOrder,
  listOrder,
  listChannel,
  refundOrder,
  shipOrder,
  finalPayApi,
  AllListOrder,
  finalpayUpdate,
  deliveryUpdate
} from '@/api/order';
import Pagination from '@/components/Pagination'; // Secondary package based on el-pagination
import checkPermission from '@/utils/permission'; // 权限判断函数
// eslint-disable-next-line no-unused-vars
import { fmoney } from '@/utils';
import moment from 'moment';
const statusMap = {
  101: '未付款',
  102: '用户取消',
  103: '系统取消',
  104: '管理员取消',
  201: '已支付订金',
  202: '待付尾款',
  203: '支付完成',
  204: '订单取消，退款中',
  205: '已退款',
  206: '欠尾款',
  301: '已发货',
  401: '用户收货',
  402: '系统收货'
};

export default {
  name: 'Order',
  components: { Pagination },
  filters: {
    orderStatusFilter(status) {
      return statusMap[status];
    },
    getTime(time) {
      return moment(time).format('YYYY-MM-DD');
    }
  },
  data() {
    var checkAge = (rule, value, callback) => {
      if (value > this.finalPayItem.finalpayPrice) {
        this.finalPayItem.dueFinalpay = this.finalPayItem.finalpayPrice;
        return callback(new Error('当前付款金额不能大于需付款金额'));
      }
      return callback();
    };
    return {
      type: 1,
      delivery: {
        dialog: false,
        row: {}
      },
      pickerOptions: {
        disabledDate: time => {
          return time.getTime() < Date.now() - 24 * 60 * 60 * 1000;
        }
      },
      rulesPackage: {
        dueFinalpay: [
          {
            required: true,
            message: '当前支付尾款金额不能为空',
            trigger: 'blur'
          },
          { validator: checkAge, trigger: 'change' }
        ]
      },
      finalPayDialogVisible: false,
      finalPayItem: {
        finalpayPrice: '',
        dueFinalpay: '',
        orderId: ''
      },
      rule: {
        deliveryDate: [
          {
            type: 'date',
            required: true,
            message: '请选择时间',
            trigger: 'change'
          }
        ]
      },
      list: [],
      total: 0,
      listLoading: true,
      listQuery: {
        pageIndex: 1,
        pageSize: 20,
        id: undefined,
        nickname: undefined,
        orderStatus: ''
      },
      statusMap,
      orderDialogVisible: false,
      orderDetail: {
        order: {},
        user: {},
        orderGoods: []
      },
      shipForm: {
        orderId: undefined,
        shipChannel: undefined,
        shipSn: undefined
      },
      shipDialogVisible: false,
      refundForm: {
        orderId: undefined,
        refundMoney: undefined
      },
      refundDialogVisible: false,
      downloadLoading: false,
      channels: []
    };
  },
  watch: {
    type() {
      this.listQuery.pageIndex = 1;
      this.getList();
      this.getChannel();
    }
  },
  created() {
    this.getList();
    this.getChannel();
  },
  methods: {
    checkPermission,
    fmoney(s, n) {
      return fmoney(s, n);
    },
    confirmFinal() {
      this.$refs.finalForm.validate(valid => {
        if (valid) {
          this.finalPayItem.dueFinalpay = Number(this.finalPayItem.dueFinalpay);
          finalpayUpdate(this.finalPayItem)
            .then(res => {
              this.$notify.success({
                title: '成功',
                message: '欠尾款状态修改成功'
              });
              this.getList();
              this.finalPayDialogVisible = false;
            })
            .catch(({ data }) => {
              this.$notify.error(data.msg);
            });
        }
      });
    },
    finalPayShip(item) {
      this.finalPayItem = Object.assign({}, this.finalPayItem, {
        finalpayPrice:
          item.orderStatus === 202 ? item.finalpayPrice : item.oweFinalpay,
        dueFinalpay: '',
        orderId: item.id
      });
      this.finalPayDialogVisible = true;
    },
    getList() {
      this.listLoading = true;
      if (this.type === 1) {
        listOrder(this.listQuery)
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
        AllListOrder(this.listQuery).then(response => {
          this.list = response.data.data.list;
          this.total = response.data.data.total;
          this.listLoading = false;
        });
      }
    },
    getChannel() {
      listChannel().then(response => {
        this.channels = response.data.data;
      });
    },
    deliveryUpdate(row) {
      this.delivery = {
        dialog: true,
        row: Object.assign({}, row)
      };
    },
    delShip(item) {
      this.$confirm('是否取消该订单?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        alert('功能未完善');
        // finalPayApi({ orderId: item.id }).then(res => {
        //   this.$notify.success({
        //     title: '成功',
        //     message: '付款状态修改成功'
        //   });
        // }).catch(res => {
        //   this.$notify.error({
        //     title: '失败',
        //     message: res.data.errmsg
        //   });
        // });
      });
    },
    handleFilter() {
      this.listQuery.pageIndex = 1;
      this.getList();
    },
    deliverySure() {
      this.$refs.deliveryForm.validate(valid => {
        if (!valid) {
          return false;
        }
        deliveryUpdate({
          orderId: this.delivery.row.id,
          deliveryDate: this.delivery.row.deliveryDate
        })
          .then(res => {
            this.$notify.success({
              title: '成功',
              message: '修改成功'
            });
            this.delivery.dialog = false;
            this.getList();
            this.getChannel();
          })
          .catch(e => {
            this.$notify.error({
              title: '失败',
              message: '交付日期必须大于今天'
            });
          });
      });
    },
    handleDetail(row) {
      detailOrder(row.id).then(response => {
        this.orderDetail = response.data.data;
      });
      this.orderDialogVisible = true;
    },
    handleShip(row) {
      this.shipForm.orderId = row.id;
      this.shipForm.shipChannel = row.shipChannel;
      this.shipForm.shipSn = row.shipSn;

      this.shipDialogVisible = true;
      this.$nextTick(() => {
        this.$refs['shipForm'].clearValidate();
      });
    },
    confirmShip() {
      this.$refs['shipForm'].validate(valid => {
        if (valid) {
          shipOrder(this.shipForm)
            .then(response => {
              this.shipDialogVisible = false;
              this.$notify.success({
                title: '成功',
                message: '确认发货成功'
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
    handleRefund(row) {
      this.refundForm.orderId = row.id;
      this.refundForm.refundMoney = row.actualPrice;

      this.refundDialogVisible = true;
      this.$nextTick(() => {
        this.$refs['refundForm'].clearValidate();
      });
    },
    finalPay(item) {
      this.$confirm('是否确认已有成品，让用户付尾款?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        finalPayApi({ orderId: item.id })
          .then(res => {
            this.$notify.success({
              title: '成功',
              message: '付款状态修改成功'
            });
            this.getList();
          })
          .catch(res => {
            this.$notify.error({
              title: '失败',
              message: res.data.msg
            });
          });
      });
    },
    confirmRefund() {
      this.$refs['refundForm'].validate(valid => {
        if (valid) {
          refundOrder(this.refundForm)
            .then(response => {
              this.refundDialogVisible = false;
              this.$notify.success({
                title: '成功',
                message: '确认退款成功'
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
    handleDownload() {
      this.downloadLoading = true;
      import('@/vendor/Export2Excel').then(excel => {
        const tHeader = [
          '订单ID',
          '订单编号',
          '用户ID',
          '订单状态',
          '是否删除',
          '收货人',
          '收货联系电话',
          '收货地址'
        ];
        const filterVal = [
          'id',
          'orderSn',
          'userId',
          'orderStatus',
          'isDelete',
          'consignee',
          'mobile',
          'address'
        ];
        excel.export_json_to_excel2(tHeader, this.list, filterVal, '订单信息');
        this.downloadLoading = false;
      });
    },
    printOrder() {
      this.$print(this.$refs.print);
      this.orderDialogVisible = false;
    }
  }
};
</script>
<style lang="scss">
.free {
  span {
    margin-right: 10px;
  }
}
.detail {
  .el-tag {
    min-width: 80px;
    text-align: center;
    margin-right: 10px;
  }
}
</style>
