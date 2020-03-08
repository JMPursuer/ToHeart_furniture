<template>
  <div class="app-container">
    <div class="filter-container">
      <el-date-picker
        v-model="listQuery.time"
        class="filter-item"
        type="monthrange"
        format="yyyy 年 MM 月"
        value-format="yyyy-MM"
        range-separator="至"
        start-placeholder="开始月份"
        end-placeholder="结束月份"
      />
      <el-button
        class="filter-item"
        type="primary"
        icon="el-icon-search"
        @click="handleFilter"
      >查找</el-button>
    </div>
    <ve-line
      :extend="chartExtend"
      :data="chartData"
      :settings="chartSettings"
    />
    <el-table :data="dataList" style="width: 100%">
      <el-table-column prop="nickname" label="客户姓名" />
      <el-table-column prop="amount" label="金额">
        <template slot-scope="scope"> {{ scope.row.amount }}元 </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { buyerList, sellerSelfList } from '@/api/stat';
import VeLine from 'v-charts/lib/line';
import moment from 'moment';
export default {
  components: { VeLine },
  data() {
    return {
      chartData: {
        columns: ['month', 'amount']
      },
      dataList: [],
      listQuery: {
        time: [
          moment(new Date()).format('YYYY-MM'),
          moment(new Date()).format('YYYY-MM')
        ]
      },
      chartSettings: {
        labelMap: {
          month: '月份',
          amount: '订单总额'
        }
      },
      chartExtend: {}
    };
  },
  created() {
    this.init();
  },
  methods: {
    init() {
      buyerList({
        startTime: this.listQuery.time[0],
        endTime: this.listQuery.time[1]
      }).then(response => {
        this.dataList = response.data.data;
      });
      sellerSelfList({
        startTime: this.listQuery.time[0],
        endTime: this.listQuery.time[1]
      }).then(response => {
        this.chartData.rows = response.data.data.map(res => {
          res.month = String(moment(`${res.month}-01`).get('month') + 1);
          return res;
        });
        this.chartExtend = {
          xAxis: { boundaryGap: true }
        };
      });
    },
    handleFilter() {
      this.init();
    }
  }
};
</script>
