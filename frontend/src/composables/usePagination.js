import { reactive, ref } from 'vue'

/**
 * 分页表格通用逻辑
 * @param {Function} fetcher (params) => Promise<PageResult>
 * @param {Object} initialQuery
 */
export function usePagination(fetcher, initialQuery = {}) {
  const query = reactive({
    current: 1,
    size: 10,
    ...initialQuery,
  })
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)

  async function fetchList() {
    loading.value = true
    try {
      const data = await fetcher({ ...query })
      list.value = data?.records || []
      total.value = data?.total || 0
    } catch {
      // 错误提示已由 request.js 拦截器统一处理，避免未捕获 rejection 触发全局报错遮罩
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(page) {
    query.current = page
    fetchList()
  }

  function handleSizeChange(size) {
    query.size = size
    query.current = 1
    fetchList()
  }

  function handleSearch() {
    query.current = 1
    fetchList()
  }

  return {
    query,
    list,
    total,
    loading,
    fetchList,
    handlePageChange,
    handleSizeChange,
    handleSearch,
  }
}
