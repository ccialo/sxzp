/**
 * 学生端路由
 */
export default [
  {
    path: 'applies',
    name: 'StudentApplyList',
    component: () => import('@/views/student/ApplyList.vue'),
    meta: { title: '我的投递' },
  },
  {
    path: 'resumes',
    name: 'StudentResumeList',
    component: () => import('@/views/student/ResumeList.vue'),
    meta: { title: '我的简历' },
  },
  {
    path: 'resumes/edit',
    name: 'StudentResumeEdit',
    component: () => import('@/views/student/ResumeEdit.vue'),
    meta: { title: '简历编辑' },
  },
  {
    path: 'interviews',
    name: 'StudentInterviewList',
    component: () => import('@/views/student/InterviewList.vue'),
    meta: { title: '我的面试' },
  },
  {
    path: 'agreements',
    name: 'StudentAgreementList',
    component: () => import('@/views/student/AgreementList.vue'),
    meta: { title: '我的协议' },
  },
  {
    path: 'weekly',
    name: 'StudentWeeklyList',
    component: () => import('@/views/student/WeeklyList.vue'),
    meta: { title: '我的周报' },
  },
  {
    path: 'weekly/submit',
    name: 'StudentWeeklySubmit',
    component: () => import('@/views/student/WeeklySubmit.vue'),
    meta: { title: '提交周报' },
  },
  {
    path: 'notices',
    name: 'StudentNoticeList',
    component: () => import('@/views/student/NoticeList.vue'),
    meta: { title: '消息通知' },
  },
]
