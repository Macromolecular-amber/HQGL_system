package com.logistics.repository;

import com.logistics.entity.StMealReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StMealReservationRepository extends JpaRepository<StMealReservation, Long>, JpaSpecificationExecutor<StMealReservation> {

    /**
     * 查询某用户某日期某餐次未取消的预约（幂等校验）
     */
    List<StMealReservation> findByUserIdAndMealDateAndMealTypeAndIsCancelled(Long userId, LocalDate mealDate, String mealType, Boolean isCancelled);

    /**
     * 查询某日期未取消的所有预约
     */
    List<StMealReservation> findByMealDateAndIsCancelled(LocalDate mealDate, Boolean isCancelled);
}
