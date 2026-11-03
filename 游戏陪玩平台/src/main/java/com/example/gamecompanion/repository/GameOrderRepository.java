package com.example.gamecompanion.repository;

import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 订单数据访问接口。
 */
@Repository
public interface GameOrderRepository extends JpaRepository<GameOrder, Long> {

    /** 全部订单，按下单时间倒序（最新的在最前面） */
    List<GameOrder> findAllByOrderByCreateTimeDesc();

    /** 某个玩家的订单 */
    List<GameOrder> findByPlayerIdOrderByCreateTimeDesc(Long playerId);

    /** 某个陪玩账号的订单 */
    List<GameOrder> findByCompanionIdOrderByCreateTimeDesc(Long companionId);

    /** 按状态查询订单（实现状态筛选） */
    List<GameOrder> findByStatusOrderByCreateTimeDesc(OrderStatus status);

    /** 某个玩家某个状态的订单 */
    List<GameOrder> findByPlayerIdAndStatusOrderByCreateTimeDesc(Long playerId, OrderStatus status);

    /** 统计某状态的订单数量（看板统计使用） */
    long countByStatus(OrderStatus status);

    /** 判断某个陪玩账号是否已有订单（删除陪玩账号前校验） */
    boolean existsByCompanionId(Long companionId);
}
