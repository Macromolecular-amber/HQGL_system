package com.logistics.controller.sys;

import com.logistics.common.BusinessException;
import com.logistics.common.Result;
import com.logistics.dto.sys.CurrentUserVO;
import com.logistics.dto.sys.DriverVO;
import com.logistics.entity.SysRole;
import com.logistics.entity.SysUnit;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysRoleRepository;
import com.logistics.repository.SysUnitRepository;
import com.logistics.repository.SysUserRepository;
import com.logistics.repository.SysUserRoleRepository;
import com.logistics.security.RequiresRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理（基础信息）
 */
@RestController
@RequestMapping("/api/sys/user")
@RequiredArgsConstructor
@RequiresRoles({"ADMIN"})
public class SysUserController {

    /** 默认用户（登录体系接入前的兜底） */
    private static final Long DEFAULT_USER_ID = 1L;

    /** 驾驶员角色编码 */
    private static final String ROLE_DRIVER = "DRIVER";

    /** 保洁员角色编码 */
    private static final String ROLE_CLEANER = "CLEANER";

    private final SysUserRepository sysUserRepository;
    private final SysUnitRepository sysUnitRepository;
    private final SysRoleRepository sysRoleRepository;
    private final SysUserRoleRepository sysUserRoleRepository;

    /**
     * 获取当前登录用户信息（含所属单位）
     */
    @GetMapping("/current")
    public Result<CurrentUserVO> current() {
        SysUser user = resolveCurrentUser();
        if (user == null) {
            throw new BusinessException("未找到当前用户");
        }
        CurrentUserVO vo = new CurrentUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setUnitId(user.getUnitId());
        vo.setUnitName(user.getUnitId() == null ? null
                : sysUnitRepository.findById(user.getUnitId()).map(SysUnit::getUnitName).orElse(null));
        return Result.success(vo);
    }

    /**
     * 获取所有驾驶员（角色包含 DRIVER）列表
     */
    @GetMapping("/drivers")
    public Result<List<DriverVO>> drivers() {
        SysRole driverRole = sysRoleRepository.findByRoleCode(ROLE_DRIVER).orElse(null);
        if (driverRole == null) {
            return Result.success(Collections.emptyList());
        }
        List<DriverVO> vos = sysUserRoleRepository.findByRoleId(driverRole.getId()).stream()
                .map(ur -> sysUserRepository.findById(ur.getUserId()).orElse(null))
                .filter(u -> u != null && !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(u -> {
                    DriverVO vo = new DriverVO();
                    vo.setId(u.getId());
                    vo.setRealName(u.getRealName());
                    vo.setPhone(u.getPhone());
                    return vo;
                })
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 获取所有保洁员（角色包含 CLEANER）列表
     */
    @GetMapping("/cleaners")
    public Result<List<DriverVO>> cleaners() {
        SysRole cleanerRole = sysRoleRepository.findByRoleCode(ROLE_CLEANER).orElse(null);
        if (cleanerRole == null) {
            return Result.success(Collections.emptyList());
        }
        List<DriverVO> vos = sysUserRoleRepository.findByRoleId(cleanerRole.getId()).stream()
                .map(ur -> sysUserRepository.findById(ur.getUserId()).orElse(null))
                .filter(u -> u != null && !Boolean.TRUE.equals(u.getIsDeleted()))
                .map(u -> {
                    DriverVO vo = new DriverVO();
                    vo.setId(u.getId());
                    vo.setRealName(u.getRealName());
                    vo.setPhone(u.getPhone());
                    return vo;
                })
                .collect(Collectors.toList());
        return Result.success(vos);
    }

    /**
     * 从 SecurityContext 获取当前登录用户对应的系统用户，未匹配时兜底默认用户
     */
    private SysUser resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof String && "anonymousUser".equals(principal))
                    && StringUtils.hasText(authentication.getName())) {
                SysUser user = sysUserRepository.findByUsername(authentication.getName()).orElse(null);
                if (user != null) {
                    return user;
                }
            }
        }
        return sysUserRepository.findById(DEFAULT_USER_ID).orElse(null);
    }
}
