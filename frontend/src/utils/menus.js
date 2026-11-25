/**
 * 侧栏菜单配置：按角色分组
 * path 都是相对路径，会拼到对应角色前缀（/student、/company、/admin）下
 */
export const MENUS = {
  student: [
    { title: '我的投递', path: '/student/applies', icon: 'Position' },
    { title: '我的简历', path: '/student/resumes', icon: 'Document' },
    { title: '我的面试', path: '/student/interviews', icon: 'Calendar' },
    { title: '我的协议', path: '/student/agreements', icon: 'Files' },
    { title: '我的周报', path: '/student/weekly', icon: 'EditPen' },
    { title: '消息通知', path: '/student/notices', icon: 'Bell' },
  ],
  company: [
    { title: '企业档案', path: '/company/profile', icon: 'OfficeBuilding' },
    { title: '我的岗位', path: '/company/jobs', icon: 'Briefcase' },
    { title: '收到的投递', path: '/company/applies', icon: 'Message' },
    { title: '已发出面试', path: '/company/interviews', icon: 'Calendar' },
    { title: '已发出协议', path: '/company/agreements', icon: 'Files' },
    { title: '收到的周报', path: '/company/weekly', icon: 'EditPen' },
    { title: '消息通知', path: '/company/notices', icon: 'Bell' },
  ],
  admin: [
    { title: '仪表盘', path: '/admin/dashboard', icon: 'Odometer' },
    { title: '数据分析', path: '/admin/analysis', icon: 'DataAnalysis' },
    { title: '企业审核', path: '/admin/companies', icon: 'OfficeBuilding' },
    { title: '岗位审核', path: '/admin/jobs', icon: 'Briefcase' },
  ],
}

/**
 * 角色中文标签
 */
export const ROLE_LABELS = {
  student: '学生',
  company: '企业',
  admin: '管理员',
}
