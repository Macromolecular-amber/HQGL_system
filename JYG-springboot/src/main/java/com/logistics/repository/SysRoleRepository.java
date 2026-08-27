package com.logistics.repository;

import com.logistics.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, Long> {

    /**
     * 按角色编码查询角色
     */
    Optional<SysRole> findByRoleCode(String roleCode);
}
