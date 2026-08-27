package com.logistics.repository;

import com.logistics.entity.SysUserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, Long> {

    /**
     * 查询指定用户的角色关联
     */
    List<SysUserRole> findByUserId(Long userId);

    /**
     * 查询指定角色的用户关联
     */
    List<SysUserRole> findByRoleId(Long roleId);
}
