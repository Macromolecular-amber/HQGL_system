<script>
import { getToken } from '@/utils/auth'

export default {
  onLaunch: function () {
    console.log('App Launch')
  },
  onShow: function () {
    // 未登录时强制回登录页
    if (!getToken()) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  },
  onHide: function () {
    console.log('App Hide')
  }
}
</script>

<style lang="scss">
/* uview-plus 全局基础样式 */
@import "uview-plus/index.scss";

/* 页面公共样式 */
page {
  background-color: #f5f7fa;
  font-size: 28rpx;
  color: #333;
}
</style>