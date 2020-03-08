<template>
  <div class="app-container">
    <el-card class="box-card">
      <h3>商品介绍</h3>
      <el-form ref="goods" :rules="rules" :model="goods" label-width="150px">
        <!-- <el-form-item label="商品ID" prop="id">
          <el-input v-model="goods.id" disabled />
        </el-form-item> -->
        <el-form-item label="商品ID" prop="gno">
          <el-input v-model="goods.gno" />
        </el-form-item>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="goods.name" />
        </el-form-item>
        <el-form-item label="商品编号" prop="goodsSn">
          <el-input v-model="goods.goodsSn" disabled />
        </el-form-item>
        <!-- <el-form-item label="市场售价" prop="counterPrice">
          <el-input v-model="goods.counterPrice" placeholder="0.00">
            <template slot="append">元起</template>
          </el-input>
        </el-form-item> -->
        <el-form-item label="是否新品" prop="isNew">
          <el-radio-group v-model="goods.isNew">
            <el-radio :label="true">新品</el-radio>
            <el-radio :label="false">非新品</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="库存" prop="isSpot">
          <el-radio-group v-model="goods.isSpot">
            <el-radio :label="1" disabled>有库存</el-radio>
            <el-radio :label="0" disabled>无库存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否热卖" prop="isHot">
          <el-radio-group v-model="goods.isHot">
            <el-radio :label="false">普通</el-radio>
            <el-radio :label="true">热卖</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否在售" prop="isOnSale">
          <el-radio-group v-model="goods.isOnSale">
            <el-radio :label="true">在售</el-radio>
            <el-radio :label="false">未售</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="商品图片">
          <el-upload
            :headers="headers"
            :action="uploadPath"
            :show-file-list="false"
            :on-success="uploadPicUrl"
            class="avatar-uploader"
            accept=".jpg,.jpeg,.png,.gif"
          >
            <img v-if="goods.picUrl" :src="goods.picUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon" />
          </el-upload>
        </el-form-item>

        <el-form-item label="宣传画廊">
          <el-upload
            :action="uploadPath"
            :headers="headers"
            :limit="5"
            :file-list="galleryFileList"
            :on-exceed="uploadOverrun"
            :on-success="handleGalleryUrl"
            :on-remove="handleRemove"
            multiple
            accept=".jpg,.jpeg,.png,.gif"
            list-type="picture-card"
          >
            <i class="el-icon-plus" />
          </el-upload>
        </el-form-item>

        <el-form-item label="商品单位">
          <el-input v-model="goods.unit" placeholder="件 / 个 / 盒" />
        </el-form-item>

        <el-form-item label="关键字">
          <el-tag
            v-for="tag in keywords"
            :key="tag"
            closable
            type="primary"
            @close="handleClose(tag)"
          >
            {{ tag }}
          </el-tag>
          <el-input
            v-if="newKeywordVisible"
            ref="newKeywordInput"
            v-model="newKeyword"
            class="input-new-keyword"
            @keyup.enter.native="handleInputConfirm"
            @blur="handleInputConfirm"
          />
          <el-button
            v-else
            class="button-new-keyword"
            type="primary"
            @click="showInput"
          >+ 增加</el-button>
        </el-form-item>

        <el-form-item label="所属分类">
          <el-cascader
            v-model="categoryIds"
            :options="categoryList"
            clearable
            expand-trigger="hover"
            @change="handleCategoryChange"
          />
        </el-form-item>

        <!-- <el-form-item label="所属品牌商">
          <el-select v-model="goods.brandId" clearable>
            <el-option v-for="item in brandList" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item> -->

        <el-form-item label="商品简介">
          <el-input v-model="goods.brief" />
        </el-form-item>

        <el-form-item label="商品详细介绍">
          <editor v-model="goods.detail" :init="editorInit" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="box-card">
      <h3>产品列表</h3>
      <el-table :data="products">
        <el-table-column property="code" label="编码" />
        <el-table-column property="title" label="产品名称" />
        <el-table-column property="isSpot" label="库存">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isSpot === 1 ? 'primary' : 'danger'">
              {{ scope.row.isSpot === 1 ? '有库存' : '无库存' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column property="price" label="价格">
          <template slot-scope="scope"> {{ scope.row.price }}元 </template>
        </el-table-column>
        <el-table-column property="url" label="商品图片">
          <template slot-scope="scope">
            <img v-if="scope.row.url" :src="scope.row.url" width="40">
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
              type="primary"
              size="mini"
              @click="handleProductsShow(scope.row)"
            >设置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-dialog :visible.sync="productsVisible" title="修改产品">
        <el-form
          ref="specForm"
          :rules="rules"
          :model="productsEdit"
          status-icon
          label-position="left"
          label-width="100px"
          style="width: 400px; margin-left:50px;"
        >
          <el-form-item label="编码" prop="code">
            <span>{{ productsEdit.code }}</span>
          </el-form-item>
          <el-form-item label="产品名称" prop="title">
            <span>{{ productsEdit.title }}</span>
          </el-form-item>
          <el-form-item label="库存" prop="isSpot">
            <el-radio-group v-model="productsEdit.isSpot">
              <el-radio :label="1">有库存</el-radio>
              <el-radio :label="0">无库存</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="productsVisible = false">取消</el-button>
          <el-button type="primary" @click="handleProductsEdit">确定</el-button>
        </div>
      </el-dialog>
    </el-card>
    <el-card class="box-card">
      <h3>商品规格</h3>
      <div class="option-list">
        <h4>面料</h4>
        <el-table :data="option.p01List">
          <el-table-column property="code" label="编码" />
          <el-table-column property="material" label="面料">
            <template slot-scope="scope">
              {{
                scope.row.material === 'F'
                  ? '布料'
                  : scope.row.material === 'F'
                    ? '皮料'
                    : scope.row.material === 'N'
                      ? '其他类型'
                      : '缺乏面料类型'
              }}
            </template>
          </el-table-column>
          <el-table-column property="status" label="状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'primary' : 'danger'">
                {{ scope.row.status === 1 ? '有库存' : '无库存' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column property="desc" label="备注" />
        </el-table>
      </div>
      <div class="option-list">
        <h4>颜色</h4>
        <el-table :data="option.p02List">
          <el-table-column property="code" label="编码" />
          <el-table-column property="desc" label="颜色" />
          <el-table-column property="status" label="状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'primary' : 'danger'">
                {{ scope.row.status === 1 ? '有库存' : '无库存' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="option-list">
        <h4>海绵</h4>
        <el-table :data="option.p03List">
          <el-table-column property="code" label="编码" />
          <el-table-column property="desc" label="硬度" />
          <el-table-column property="status" label="状态">
            <template slot-scope="scope">
              <el-tag :type="scope.row.status === 1 ? 'primary' : 'danger'">
                {{ scope.row.status === 1 ? '有库存' : '无库存' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
    <el-card class="box-card">
      <h3>商品参数</h3>
      <el-button type="primary" @click="handleAttributeShow">添加</el-button>
      <el-table :data="attributesData">
        <el-table-column property="attribute" label="商品参数名称" />
        <el-table-column property="value" label="商品参数值" />
        <el-table-column
          align="center"
          label="操作"
          width="200"
          class-name="small-padding fixed-width"
        >
          <template slot-scope="scope">
            <el-button
              type="primary"
              size="mini"
              @click="handleAttributeShow(scope.row)"
            >设置</el-button>
            <el-button
              type="danger"
              size="mini"
              @click="handleAttributeDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog
        :visible.sync="attributeVisiable"
        :title="attributeAdd ? '添加商品参数' : '编辑商品参数'"
      >
        <el-form
          ref="attributeForm"
          :model="attributeForm"
          status-icon
          label-position="left"
          label-width="100px"
          style="width: 400px; margin-left:50px;"
        >
          <el-form-item label="商品参数名称" prop="attribute">
            <el-input v-model="attributeForm.attribute" />
          </el-form-item>
          <el-form-item label="商品参数值" prop="value">
            <el-input v-model="attributeForm.value" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button @click="attributeVisiable = false">取消</el-button>
          <el-button
            v-if="attributeAdd"
            type="primary"
            @click="handleAttributeAdd"
          >确定</el-button>
          <el-button
            v-else
            type="primary"
            @click="handleAttributeEdit"
          >确定</el-button>
        </div>
      </el-dialog>
    </el-card>

    <div class="op-container">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleEdit">更新商品</el-button>
    </div>
  </div>
</template>

<style>
.el-card {
  margin-bottom: 10px;
}
.el-tag + .el-tag {
  margin-left: 10px;
}
.input-new-keyword {
  width: 90px;
  margin-left: 10px;
  vertical-align: bottom;
}
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
.op-container {
  display: flex;
  justify-content: center;
}
.option-list {
  margin: 20px;
}
</style>

<script>
import {
  detailGoods,
  editGoods,
  listCatAndBrand,
  spotUpdate
} from '@/api/goods';
import { createStorage, uploadPath } from '@/api/storage';
import Editor from '@tinymce/tinymce-vue';
import { MessageBox } from 'element-ui';
import { getToken } from '@/utils/auth';

export default {
  name: 'GoodsEdit',
  components: { Editor },
  data() {
    return {
      uploadPath,
      productsEdit: {
        id: '',
        goodsId: '',
        price: '',
        url: '',
        code: ''
      },
      productsVisible: false,

      newKeywordVisible: false,
      newKeyword: '',
      keywords: [],
      galleryFileList: [],
      categoryList: [],
      brandList: [],
      categoryIds: [],
      goods: { gallery: [] },
      option: {
        p01List: [],
        p02List: [],
        p03List: []
      },
      productVisiable: false,
      productForm: {
        id: 0,
        price: 0.0,
        number: 0,
        url: ''
      },
      products: [],
      attributeVisiable: false,
      attributeAdd: true,
      attributeForm: { attribute: '', value: '' },
      attributes: [],
      rules: {
        name: [
          { required: true, message: '商品名称不能为空', trigger: 'blur' }
        ],
        price: [{ required: true, message: '价格不能为空', trigger: 'blur' }]
      },
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
      }
    };
  },
  computed: {
    headers() {
      return {
        'X-Litemall-Admin-Token': getToken()
      };
    },
    attributesData() {
      var attributesData = [];
      for (var i = 0; i < this.attributes.length; i++) {
        if (this.attributes[i].deleted) {
          continue;
        }
        attributesData.push(this.attributes[i]);
      }
      return attributesData;
    }
  },
  created() {
    this.init();
  },
  methods: {
    init: function() {
      if (this.$route.query.id == null) {
        return;
      }

      const goodsId = this.$route.query.id;
      detailGoods(goodsId).then(response => {
        this.goods = Object.assign({}, this.goods, response.data.data.goods);
        // 稍微调整一下前后端不一致
        if (this.goods.brandId === 0) {
          this.goods.brandId = null;
        }
        if (this.goods.keywords === '') {
          this.goods.keywords = null;
        }
        this.option = Object.assign({}, this.option, response.data.data.option);
        this.products = response.data.data.products;
        this.attributes = response.data.data.attributes;
        this.categoryIds = response.data.data.categoryIds;

        this.galleryFileList = [];
        const galleryList = this.goods.gallery;
        galleryList.forEach(res => {
          this.galleryFileList.push({
            url: res
          });
        });
        const keywords = response.data.data.goods.keywords;
        if (keywords !== null) {
          this.keywords = keywords.split(',');
        }
      });

      listCatAndBrand().then(response => {
        this.categoryList = response.data.data.categoryList;
        this.brandList = response.data.data.brandList;
      });
    },
    handleProductsShow(row) {
      this.productsEdit = Object.assign({}, row, {
        url: row.url
      });
      this.productsVisible = true;
    },
    handleProductsEdit() {
      this.products = this.products.map(res => {
        if (res.code === this.productsEdit.code) {
          res.price = Number(this.productsEdit.price);
          res.url = this.productsEdit.url;
          res.isSpot = this.productsEdit.isSpot;
        }
        return res;
      });
      spotUpdate({
        id: this.productsEdit.id,
        isSpot: this.productsEdit.isSpot
      }).then(res => {
        this.productsVisible = false;
        this.$notify.success({
          title: '成功',
          message: '修改成功'
        });
      });
    },
    handleCategoryChange(value) {
      this.goods.categoryId = value[value.length - 1];
    },
    handleCancel: function() {
      this.$router.push({ path: '/goods/list' });
    },
    handleEdit: function() {
      delete this.goods.isSpot;
      const finalGoods = {
        goods: this.goods,
        // products: this.products,
        attributes: this.attributes
      };
      editGoods(finalGoods)
        .then(response => {
          this.$notify.success({
            title: '成功',
            message: '创建成功'
          });
          this.$router.push({ path: '/goods/list' });
        })
        .catch(response => {
          MessageBox.alert('业务错误：' + response.data.errmsg, '警告', {
            confirmButtonText: '确定',
            type: 'error'
          });
        });
    },
    handleClose(tag) {
      this.keywords.splice(this.keywords.indexOf(tag), 1);
      this.goods.keywords = this.keywords.toString();
    },
    showInput() {
      this.newKeywordVisible = true;
      this.$nextTick(_ => {
        this.$refs.newKeywordInput.$refs.input.focus();
      });
    },
    handleInputConfirm() {
      const newKeyword = this.newKeyword;
      if (newKeyword) {
        this.keywords.push(newKeyword);
        this.goods.keywords = this.keywords.toString();
      }
      this.newKeywordVisible = false;
      this.newKeyword = '';
    },
    uploadPicUrl: function(response) {
      this.goods.picUrl = response.data.url;
    },
    uploadProductsUrl: function(response) {
      this.productsEdit.url = response.data.url;
    },
    uploadOverrun: function() {
      this.$message({
        type: 'error',
        message: '上传文件个数超出限制!最多上传5张图片!'
      });
    },
    handleGalleryUrl(response, file, fileList) {
      if (response.errno === 0) {
        this.goods.gallery.push(response.data.url);
      }
    },
    handleRemove: function(file, fileList) {
      for (var i = 0; i < this.goods.gallery.length; i++) {
        // 这里存在两种情况
        // 1. 如果所删除图片是刚刚上传的图片，那么图片地址是file.response.data.url
        //    此时的file.url虽然存在，但是是本机地址，而不是远程地址。
        // 2. 如果所删除图片是后台返回的已有图片，那么图片地址是file.url
        var url;
        if (file.response === undefined) {
          url = file.url;
        } else {
          url = file.response.data.url;
        }

        if (this.goods.gallery[i] === url) {
          this.goods.gallery.splice(i, 1);
        }
      }
    },
    specChanged: function(label) {
      this.option = [];
      this.products = [];
    },
    handleProductShow(row) {
      this.productForm = Object.assign({}, row);
      this.productVisiable = true;
    },
    uploadProductUrl: function(response) {
      this.productForm.url = response.data.url;
    },
    handleProductEdit() {
      this.productForm.updateTime = '';
      for (var i = 0; i < this.products.length; i++) {
        const v = this.products[i];
        if (v.id === this.productForm.id) {
          this.products.splice(i, 1, this.productForm);
          break;
        }
      }
      this.productVisiable = false;
    },
    handleAttributeShow(row) {
      if (row.id) {
        this.attributeForm = Object.assign({}, row);
        this.attributeAdd = false;
      } else {
        this.attributeForm = {};
        this.attributeAdd = true;
      }
      this.attributeVisiable = true;
    },
    handleAttributeAdd() {
      this.attributes.unshift(this.attributeForm);
      this.attributeVisiable = false;
    },
    handleAttributeEdit() {
      // 这是一个trick，设置updateTime的值为空，告诉后端这个记录已编辑需要更新。
      this.attributeForm.updateTime = '';
      for (var i = 0; i < this.attributes.length; i++) {
        const v = this.attributes[i];
        if (v.id === this.attributeForm.id) {
          this.attributes.splice(i, 1, this.attributeForm);
          break;
        }
      }
      this.attributeVisiable = false;
    },
    handleAttributeDelete(row) {
      row.deleted = true;
    }
  }
};
</script>
