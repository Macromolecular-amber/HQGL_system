package com.logistics.repository;

import com.logistics.entity.SysUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SysUserRepository extends JpaRepository<SysUser, Long> {

    /**
     * 按用户名查询用户
     */
    Optional<SysUser> findByUsername(String username);

    /**
     * 查询指定用户的角色编码列表（sys_user_role 关联 sys_role）
     */
    @Query("SELECT r.roleCode FROM SysRole r JOIN SysUserRole ur ON r.id = ur.roleId WHERE ur.userId = :userId")
    List<String> findRolesByUserId(@Param("userId") Long userId);
}
