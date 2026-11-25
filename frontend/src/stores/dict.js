import { defineStore } from 'pinia'
import { ref } from 'vue'
import { listByType } from '@/api/dict'

export const useDictStore = defineStore('dict', () => {
  // map: { [typeCode]: DictValue[] }
  const map = ref({})
  // loading: { [typeCode]: true/false }
  const loading = ref({})

  /**
   * 加载字典；已缓存则直接返回
   */
  async function load(typeCode, force = false) {
    if (!force && map.value[typeCode]) {
      return map.value[typeCode]
    }
    if (loading.value[typeCode]) {
      // 已有进行中的请求，等待结果
      await new Promise((resolve) => {
        const timer = setInterval(() => {
          if (!loading.value[typeCode]) {
            clearInterval(timer)
            resolve()
          }
        }, 100)
      })
      return map.value[typeCode]
    }
    loading.value[typeCode] = true
    try {
      const list = await listByType(typeCode)
      map.value[typeCode] = list || []
    } finally {
      loading.value[typeCode] = false
    }
    return map.value[typeCode]
  }

  function get(typeCode) {
    return map.value[typeCode] || []
  }

  function label(typeCode, code) {
    const list = map.value[typeCode] || []
    const item = list.find((d) => d.dictCode === code)
    return item?.dictLabel || code || '-'
  }

  return { map, load, get, label }
})
