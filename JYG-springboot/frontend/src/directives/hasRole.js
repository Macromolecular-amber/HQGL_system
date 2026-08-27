/**
 * 按钮级权限指令 v-hasRole
 * 用法：<el-button v-hasRole="['BIZ_ADMIN']" @click="audit">审核</el-button>
 * 说明：角色为登录 token roles 中的原始值（无 ROLE_ 前缀），ADMIN 拥有全部权限
 */
export default {
  mounted(el, binding) {
    const requiredRoles = binding.value
    if (!requiredRoles || !requiredRoles.length) return

    const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
    const roles = userInfo.roles || []
    // 超级管理员拥有全部权限
    const hasRole = roles.includes('ADMIN') || requiredRoles.some((r) => roles.includes(r))
    if (!hasRole) {
      el.style.display = 'none'
    }
  }
}
