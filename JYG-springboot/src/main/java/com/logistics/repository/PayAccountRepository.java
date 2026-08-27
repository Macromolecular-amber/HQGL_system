package com.logistics.repository;

import com.logistics.entity.PayAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PayAccountRepository extends JpaRepository<PayAccount, Long> {

    /**
     * 按用户查询未删除的账户
     */
    Optional<PayAccount> findByUserIdAndIsDeleted(Long userId, Boolean isDeleted);
}
