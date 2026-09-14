/**
 * 前端权限工具：统一判断当前用户角色是否拥有指定菜单/路由权限
 * 规则与后端 @RequiresRoles 一致：ADMIN 拥有全部权限
 */

/** 从 localStorage 读取当前用户角色列表 */
export const getUserRoles = () => {
  try {
    const info = JSON.parse(localStorage.getItem('userInfo') || 'null')
    return (info && info.roles) || []
  } catch (e) {
    return []
  }
}

/**
 * 判断角色集合是否满足所需权限
 * @param {string[]} requiredRoles 所需角色（空/未声明视为放行）
 * @param {string[]} userRoles 用户角色，默认从登录态读取
 */
export const hasRole = (requiredRoles, userRoles) => {
  if (!requiredRoles || requiredRoles.length === 0) return true
  const roles = userRoles || getUserRoles()
  return roles.includes('ADMIN') || requiredRoles.some((r) => roles.includes(r))
}
