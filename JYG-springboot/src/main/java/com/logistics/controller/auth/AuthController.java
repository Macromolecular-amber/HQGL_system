package com.logistics.controller.auth;

import com.logistics.annotation.Log;
import com.logistics.common.BusinessException;
import com.logistics.common.Result;
import com.logistics.dto.auth.LoginRequest;
import com.logistics.dto.auth.LoginResponse;
import com.logistics.entity.SysUser;
import com.logistics.repository.SysUserRepository;
import com.logistics.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 登录认证接口
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private SysUserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 登录：校验用户名密码并签发 JWT
     */
    @PostMapping("/login")
    @Log(module="SYS", operation="用户登录", type="LOGIN")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        SysUser user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 查询用户角色（sys_user_role 关联 sys_role）
        List<String> roles = userRepository.findRolesByUserId(user.getId());
        // 如果角色为空，默认赋予 USER 角色
        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("ROLE_USER");
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getId(), roles);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setUnitId(user.getUnitId());
        userInfo.setUnitName(user.getUnitName());
        userInfo.setRoles(roles);
        response.setUserInfo(userInfo);

        return Result.success(response);
    }
}
