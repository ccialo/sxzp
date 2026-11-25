/**
 * 企业端路由
 */
export default [
  {
    path: 'profile',
    name: 'CompanyProfile',
    component: () => import('@/views/company/CompanyProfile.vue'),
    meta: { title: '企业档案' },
  },
  {
    path: 'jobs',
    name: 'CompanyJobList',
    component: () => import('@/views/company/JobList.vue'),
    meta: { title: '我的岗位' },
  },
  {
    path: 'jobs/edit',
    name: 'CompanyJobEdit',
    component: () => import('@/views/company/JobEdit.vue'),
    meta: { title: '发布/修改岗位' },
  },
  {
    path: 'applies',
    name: 'CompanyApplyReceived',
    component: () => import('@/views/company/ApplyReceived.vue'),
    meta: { title: '收到的投递' },
  },
  {
    path: 'interviews',
    name: 'CompanyInterviewSent',
    component: () => import('@/views/company/InterviewSent.vue'),
    meta: { title: '已发出面试' },
  },
  {
    path: 'agreements',
    name: 'CompanyAgreementSent',
    component: () => import('@/views/company/AgreementSent.vue'),
    meta: { title: '已发出协议' },
  },
  {
    path: 'weekly',
    name: 'CompanyWeeklyReceived',
    component: () => import('@/views/company/WeeklyReceived.vue'),
    meta: { title: '收到的周报' },
  },
  {
    path: 'notices',
    name: 'CompanyNoticeList',
    component: () => import('@/views/company/NoticeList.vue'),
    meta: { title: '消息通知' },
  },
]
