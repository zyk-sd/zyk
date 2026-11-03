package com.example.gamecompanion.repository;

import com.example.gamecompanion.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 结算单数据访问接口。
 */
@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /** 全部结算单，按结算时间倒序 */
    List<Settlement> findAllByOrderBySettleTimeDesc();

    /** 某个陪玩账号的结算单 */
    List<Settlement> findByCompanionIdOrderBySettleTimeDesc(Long companionId);

    /** 某订单是否已经结算（防止重复结算） */
    boolean existsByOrderId(Long orderId);
}
