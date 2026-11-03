package com.example.gamecompanion.repository;

import com.example.gamecompanion.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 评价数据访问接口。
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** 全部评价，按时间倒序 */
    List<Review> findAllByOrderByCreateTimeDesc();

    /** 某个玩家写过的评价 */
    List<Review> findByPlayerIdOrderByCreateTimeDesc(Long playerId);

    /** 某个陪玩账号收到的评价 */
    List<Review> findByCompanionIdOrderByCreateTimeDesc(Long companionId);

    /** 某订单是否已经被评价（防止重复评价） */
    boolean existsByOrderId(Long orderId);
}
