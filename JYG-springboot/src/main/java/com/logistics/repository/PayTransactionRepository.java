package com.logistics.repository;

import com.logistics.entity.PayTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayTransactionRepository extends JpaRepository<PayTransaction, Long>, JpaSpecificationExecutor<PayTransaction> {

    /**
     * 按业务单号查询交易记录
     */
    List<PayTransaction> findByBizOrderNo(String bizOrderNo);
}
