import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProfile } from '@/api/company'

export const useUserStore = defineStore('user', () => {
  // 企业档案
  const company = ref(null)

  async function loadCompany(force = false) {
    if (!force && company.value) {
      return company.value
    }
    company.value = await getProfile()
    return company.value
  }

  function reset() {
    company.value = null
  }

  return { company, loadCompany, reset }
})
