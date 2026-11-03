package com.example.gamecompanion.controller;

import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Review;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import com.example.gamecompanion.repository.SettlementRepository;
import com.example.gamecompanion.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 简易 REST API：以 JSON 形式查看平台数据（可用于接口测试或二次开发）。
 * 访问示例：
 *   http://localhost:8080/api/stats
 *   http://localhost:8080/api/companions
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserRepository userRepository;
    private final CompanionRepository companionRepository;
    private final GameOrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final SettlementRepository settlementRepository;

    public ApiController(UserRepository userRepository,
                         CompanionRepository companionRepository,
                         GameOrderRepository orderRepository,
                         ReviewRepository reviewRepository,
                         SettlementRepository settlementRepository) {
        this.userRepository = userRepository;
        this.companionRepository = companionRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.settlementRepository = settlementRepository;
    }

    /** 平台统计数据 */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userRepository.count());
        data.put("companionCount", companionRepository.count());
        data.put("onlineCompanionCount", companionRepository.findByOnlineTrueOrderByIdAsc().size());
        data.put("orderCount", orderRepository.count());
        data.put("pendingOrders", orderRepository.countByStatus(OrderStatus.PENDING));
        data.put("acceptedOrders", orderRepository.countByStatus(OrderStatus.ACCEPTED));
        data.put("completedOrders", orderRepository.countByStatus(OrderStatus.COMPLETED));
        data.put("cancelledOrders", orderRepository.countByStatus(OrderStatus.CANCELLED));
        data.put("reviewCount", reviewRepository.count());
        data.put("settlementCount", settlementRepository.count());
        double avg = reviewRepository.findAll().stream().mapToInt(Review::getRating).average().orElse(0.0);
        data.put("avgRating", Math.round(avg * 10) / 10.0);
        return data;
    }

    /** 陪玩账号列表 */
    @GetMapping("/companions")
    public List<Map<String, Object>> companions() {
        return companionRepository.findAllByOrderByIdAsc().stream().map(this::toCompanionMap).toList();
    }

    /** 订单列表（含是否已评价、已结算） */
    @GetMapping("/orders")
    public List<Map<String, Object>> orders() {
        return orderRepository.findAllByOrderByCreateTimeDesc().stream().map(o -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", o.getId());
            map.put("orderNo", o.getOrderNo());
            map.put("player", o.getPlayer().getRealName());
            map.put("companion", o.getCompanion().getName());
            map.put("gameName", o.getCompanion().getGameName());
            map.put("hours", o.getHours());
            map.put("amount", o.getAmount());
            map.put("status", o.getStatus().name());
            map.put("reviewed", reviewRepository.existsByOrderId(o.getId()));
            map.put("settled", settlementRepository.existsByOrderId(o.getId()));
            map.put("createTime", o.getCreateTime().toString());
            return map;
        }).toList();
    }

    private Map<String, Object> toCompanionMap(Companion c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("name", c.getName());
        map.put("gameName", c.getGameName());
        map.put("serverName", c.getServerName());
        map.put("rankLevel", c.getRankLevel());
        map.put("pricePerHour", c.getPricePerHour());
        map.put("online", c.getOnline());
        map.put("introduction", c.getIntroduction());
        return map;
    }
}
