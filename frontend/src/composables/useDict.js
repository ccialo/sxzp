import { onMounted, ref } from 'vue'
import { useDictStore } from '@/stores/dict'

/**
 * 字典加载组合式函数
 */
export function useDict(typeCode, options = {}) {
  const dictStore = useDictStore()
  const list = ref([])

  async function load(force = false) {
    list.value = await dictStore.load(typeCode, force)
    return list.value
  }

  onMounted(() => {
    if (options.immediate !== false) {
      load()
    }
  })

  return {
    list,
    load,
    label: (code) => dictStore.label(typeCode, code),
  }
}
