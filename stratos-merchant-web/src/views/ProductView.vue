<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { NCascader, useMessage } from 'naive-ui'
import { categoryTree, deleteProduct, productList, saveProduct, updateProductStatus, uploadImage } from '@/api'
import { useAuthStore } from '@/stores/auth'
import FormField from '@/components/FormField.vue'
import MerchantPage from '@/components/MerchantPage.vue'
import ProductImagePicker from '@/components/ProductImagePicker.vue'
import { hasProductImage, imageUrl, parseAlbum } from '@/utils/image'
import { firstError, inputClass, maxLen, minLen, numberRange, required } from '@/utils/validate'

const auth = useAuthStore()
const message = useMessage()
const records = ref<any[]>([])
const tree = ref<any[]>([])
const rowInput = ref<HTMLInputElement>()
const pendingRow = ref<any>(null)
const form = reactive({
  id: null as number | string | null,
  title: '',
  spuName: '',
  categoryId: null as number | string | null,
  mainImage: '',
  album: [] as string[],
  status: 1,
  shopId: auth.shopId,
  skuList: [{ skuName: '', price: 99, stock: 100 }],
})

function toOptions(nodes: any[] = []): any[] {
  return nodes.map((item) => ({
    label: item.categoryName,
    value: item.id,
    children: item.children?.length ? toOptions(item.children) : undefined,
  }))
}

const options = computed(() => toOptions(tree.value))
const editing = computed(() => Boolean(form.id))
const errors = reactive({
  title: '',
  categoryId: '',
  price: '',
  stock: '',
})

async function load() {
  if (!auth.shopId) return
  const page = await productList({ pageNum: 1, pageSize: 50, status: -1, shopId: auth.shopId })
  records.value = page.records || []
  tree.value = (await categoryTree()) || []
  if (!form.categoryId) {
    form.categoryId = findFirstLeaf(tree.value)
  }
}

function findFirstLeaf(nodes: any[] = []): number | string | null {
  for (const item of nodes) {
    if (!item.children?.length) return item.id
    const child = findFirstLeaf(item.children)
    if (child) return child
  }
  return nodes[0]?.id ?? null
}

function resetForm() {
  form.id = null
  form.title = ''
  form.spuName = ''
  form.mainImage = ''
  form.album = []
  form.status = 1
  form.skuList = [{ skuName: '', price: 99, stock: 100 }]
}

function edit(item: any) {
  form.id = item.id
  form.title = item.title || item.spuName || ''
  form.spuName = item.spuName || item.title || ''
  form.categoryId = item.categoryId
  form.mainImage = hasProductImage(item.mainImage) ? item.mainImage : ''
  form.album = parseAlbum(item.imageList, item.mainImage)
  form.status = item.status ?? 1
}

function validate() {
  errors.title = required(form.title, '商品标题') || minLen(form.title, 2, '商品标题') || maxLen(form.title, 60, '商品标题')
  errors.categoryId = form.categoryId ? '' : '请选择商品分类'
  if (!editing.value) {
    errors.price = numberRange(form.skuList[0].price, 0.01, 999999, '售价')
    errors.stock = numberRange(form.skuList[0].stock, 0, 999999, '库存', true)
  } else {
    errors.price = ''
    errors.stock = ''
  }
  const tip = firstError(errors)
  if (tip) message.error(tip)
  return !tip
}

async function save() {
  if (!validate()) return
  await saveProduct({
    id: form.id || undefined,
    shopId: auth.shopId,
    title: form.title,
    spuName: form.spuName || form.title,
    categoryId: form.categoryId,
    mainImage: form.mainImage || '',
    imageList: form.album.join(','),
    status: form.status,
    skuList: form.id ? undefined : form.skuList,
  })
  message.success(form.id ? '商品已更新' : '已保存，商城可以看到上架商品')
  resetForm()
  await load()
}

function pickRowImage(item: any) {
  pendingRow.value = item
  rowInput.value?.click()
}

async function onRowFile(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  ;(event.target as HTMLInputElement).value = ''
  const item = pendingRow.value
  pendingRow.value = null
  if (!file || !item) return
  if (file.size > 5 * 1024 * 1024) {
    message.error('图片不能超过 5MB')
    return
  }
  const data = await uploadImage(file)
  if (!data?.url) return
  await saveProduct({
    id: item.id,
    shopId: auth.shopId,
    title: item.title || item.spuName,
    spuName: item.spuName || item.title,
    categoryId: item.categoryId,
    mainImage: data.url,
    imageList: item.imageList,
    status: item.status,
  })
  message.success(hasProductImage(item.mainImage) ? '商品图片已更换' : '商品图片已上传')
  if (form.id === item.id) {
    form.mainImage = data.url
  }
  await load()
}

async function addAlbumImage(file: File) {
  if (form.album.length >= 5) {
    message.error('最多再加 5 张附图')
    return
  }
  const data = await uploadImage(file)
  if (data?.url) form.album.push(data.url)
}

function onAlbumChange(event: Event) {
  const file = (event.target as HTMLInputElement).files?.[0]
  ;(event.target as HTMLInputElement).value = ''
  if (file) addAlbumImage(file)
}

onMounted(load)
</script>

<template>
  <MerchantPage title="商品" extra="发布后会出现在商城首页和分类页，无图商品可以在这里补图或改图">
    <form class="admin-card grid grid-cols-2 gap-5 p-6" @submit.prevent="save">
      <div class="col-span-2 flex flex-wrap items-start gap-4">
        <ProductImagePicker v-model="form.mainImage" :label="editing ? '修改主图' : '商品主图'" />
        <div class="min-w-[220px] flex-1 space-y-2">
          <div class="text-sm font-medium">附图</div>
          <p class="text-xs text-mute">最多 5 张，点击已选图片可移除</p>
          <div class="flex flex-wrap items-center gap-2">
            <button
              v-for="(url, index) in form.album"
              :key="`${url}-${index}`"
              type="button"
              class="relative h-16 w-16 overflow-hidden rounded-xl bg-slate-100"
              title="点击移除"
              @click="form.album.splice(index, 1)"
            >
              <img :src="imageUrl(url)" alt="" class="h-full w-full object-cover" />
            </button>
            <label class="flex h-16 w-16 cursor-pointer items-center justify-center rounded-xl border border-dashed border-slate-200 text-xs text-mute">
              添加
              <input type="file" accept="image/*" class="hidden" @change="onAlbumChange" />
            </label>
          </div>
        </div>
      </div>
      <FormField label="商品标题" required hint="会显示在商城列表和详情页" :error="errors.title">
        <input v-model="form.title" :class="inputClass(errors.title)" placeholder="例如：Stratos 演示智能手机" maxlength="60" />
      </FormField>
      <FormField label="商品分类" required :error="errors.categoryId">
        <NCascader
          v-model:value="form.categoryId"
          :options="options"
          placeholder="选择分类"
          check-strategy="all"
          :show-path="true"
          class="w-full"
          :status="errors.categoryId ? 'error' : undefined"
        />
      </FormField>
      <template v-if="!editing">
        <FormField label="规格名称" hint="不填则用商品标题，例如：黑色 128G">
          <input v-model="form.skuList[0].skuName" class="admin-input" maxlength="30" placeholder="例如：黑色 128G" />
        </FormField>
        <FormField label="售价" required hint="发布后可在库存页查看" :error="errors.price">
          <input v-model.number="form.skuList[0].price" type="number" min="0.01" step="0.01" :class="inputClass(errors.price)" />
        </FormField>
        <FormField label="库存" required :error="errors.stock">
          <input v-model.number="form.skuList[0].stock" type="number" min="0" step="1" :class="inputClass(errors.stock)" />
        </FormField>
      </template>
      <div class="col-span-2 flex gap-2">
        <button type="submit" class="h-11 rounded-full bg-teal-600 px-6 text-white">
          {{ editing ? '保存修改' : '发布商品' }}
        </button>
        <button v-if="editing" type="button" class="admin-btn h-11" @click="resetForm">取消编辑</button>
      </div>
    </form>
    <div class="admin-card">
      <table class="admin-table">
        <thead>
          <tr>
            <th>商品</th>
            <th>价格</th>
            <th>状态</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in records" :key="item.id">
            <td>
              <div class="flex items-center gap-3">
                <div class="h-14 w-14 shrink-0 overflow-hidden rounded-xl bg-slate-100">
                  <img v-if="hasProductImage(item.mainImage)" :src="imageUrl(item.mainImage)" alt="" class="h-full w-full object-cover" />
                  <div v-else class="flex h-full w-full items-center justify-center text-[11px] text-mute">无图</div>
                </div>
                <div>
                  <div class="font-medium">{{ item.title || item.spuName }}</div>
                  <div class="text-xs text-mute">ID {{ item.id }}</div>
                </div>
              </div>
            </td>
            <td>{{ item.minPrice != null ? `¥${Number(item.minPrice).toFixed(2)}` : '-' }}</td>
            <td>
              <span :class="item.status === 1 ? 'admin-badge admin-badge-ok' : 'admin-badge admin-badge-mute'">
                {{ item.status === 1 ? '上架' : '下架' }}
              </span>
            </td>
            <td class="space-x-2 whitespace-nowrap">
              <button class="admin-btn" @click="pickRowImage(item)">
                {{ hasProductImage(item.mainImage) ? '更换图片' : '上传图片' }}
              </button>
              <button class="admin-btn" @click="edit(item)">编辑</button>
              <button class="admin-btn" @click="updateProductStatus(item.id, item.status === 1 ? 0 : 1, auth.shopId!).then(load)">
                {{ item.status === 1 ? '下架' : '上架' }}
              </button>
              <button
                class="admin-btn-danger"
                @click="window.confirm('确定删除这件商品？商城将不再展示。') && deleteProduct(item.id, auth.shopId!).then(load)"
              >
                删除
              </button>
            </td>
          </tr>
          <tr v-if="!records.length">
            <td colspan="4" class="py-10 text-center text-mute">还没有本店商品</td>
          </tr>
        </tbody>
      </table>
      <input ref="rowInput" type="file" accept="image/*" class="hidden" @change="onRowFile" />
    </div>
  </MerchantPage>
</template>
