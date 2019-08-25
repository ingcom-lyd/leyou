<template>
  <v-form v-model="valid" ref="brandForm">
    <v-text-field
      label="品牌名称"
      v-model="brand.name"
      :rules="[v => !!v || '品牌名称不能为空']"
      :counter="10"
      required
    />
    <v-text-field
      label="首字母"
      v-model="brand.letter"
      :rules="[v => v.length === 1 || '首字母只能是1位']"
      required
      mask="A"
    />
    <v-cascader url="/item/category/list" required
                v-model="brand.categories"
                multiple label="商品分类"/>
    <v-layout row>
      <v-flex xs3>
        <span style="font-size: 16px; color: #444">品牌LOGO：</span>
      </v-flex>
      <v-flex>
        <v-upload
          v-model="brand.image" url="/upload/image" :multiple="false" :pic-width="250" :pic-height="90"
        />
      </v-flex>
    </v-layout>
    <v-layout class="my-4">
      <v-btn @click="submit" color="primary">提交</v-btn>
      <v-btn @click="clear" color="warning">重置</v-btn>
    </v-layout>
  </v-form>
</template>

<script>
  import config from '@/config';
  export default {
    name: "brand-form",
    props: {
      // oldBrand: Object,
      oldBrand: {
        type: Object,
        // type: String,
      },
      isEdit: {
        type: Boolean,
        default: false
      },
      show: {
        type: Boolean,
        default: true
      }
    },
    data() {
      return {
        baseUrl: config.api,
        valid: false,
        brand: {
          name: "",
          image: "",
          letter: "",
          categories: [],
          // categories: "",
        },
        imageDialogVisible: false
      }
    },
    watch: {
      oldBrand: {
        immediate: true,
        deep: true,
        handler(val) {

          // console.log(val);
          if (val) {
            console.log(val);
            // 注意不要直接复制，否则这边的修改会影响到父组件的数据，copy属性即可
            // let p = Object.deepCopy(val);
            let p = Object.deepCopy(val);
            // console.log(p);
            this.brand = p;
            // this.brand = val;
            // this.brand.categories = [{"id":105,"name":"平板电视","parentId":104,"isParent":false,"sort":1}]
            // this.brand.categories = this.brand.categories.map(c => c.id);
            console.log(this.brand);
            // p = eval("("+JSON.stringify(val)+")")
            // console.log(p);
            // console.log(JSON.stringify(val));
          } else {
            // 为空，初始化brand
            this.brand = {
              name: '',
              letter: '',
              image: '',
              categories: [],
            }
          }
        }
      }
      },
      methods: {
        submit() {
          // 表单校验
          if (this.$refs.brandForm.validate()) {
            this.brand.categories = this.brand.categories.map(c => c.id);

            this.brand.categories = this.brand.categories.join(',');

            this.brand.letter = this.brand.letter.toUpperCase();
            // 将数据提交到后台
            this.$http({
              method: this.isEdit ? 'put' : 'post',
              url: '/item/brand',
              data: this.$qs.stringify(this.brand)
            }).then(() => {
              // 关闭窗口
              this.$message.success("保存成功！");
              this.closeWindow();
            }).catch(() => {
              this.$message.error("保存失败！");
            });
          }
        },
        clear() {
          // 重置表单
          this.$refs.brandForm.reset();
          this.categories = [];
        },
        // 图片上传出成功后操作
        handleImageSuccess(res) {
          this.brand.image = res;
        },
        removeImage() {
          this.brand.image = "";
        },
        closeWindow() {
          this.$emit("close");
        }
      }
    }
</script>

<style scoped>

</style>
