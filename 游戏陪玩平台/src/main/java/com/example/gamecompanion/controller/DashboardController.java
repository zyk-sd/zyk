package com.example.gamecompanion.controller;

import com.example.gamecompanion.dto.CompanionCard;
import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Review;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.Settlement;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import com.example.gamecompanion.repository.SettlementRepository;
import com.example.gamecompanion.repository.UserRepository;
import com.example.gamecompanion.service.CompanionStatsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据看板控制器。
 * 负责汇总平台的各项统计指标，并根据登录角色展示不同的“我的数据”。
 */
@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final CompanionRepository companionRepository;
    private final GameOrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final SettlementRepository settlementRepository;
    private final CompanionStatsService companionStatsService;

    public DashboardController(UserRepository userRepository,
                               CompanionRepository companionRepository,
                               GameOrderRepository orderRepository,
                               ReviewRepository reviewRepository,
                               SettlementRepository settlementRepository,
                               CompanionStatsService companionStatsService) {
        this.userRepository = userRepository;
        this.companionRepository = companionRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.settlementRepository = settlementRepository;
        this.companionStatsService = companionStatsService;
    }

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        User me = (User) session.getAttribute("loginUser");
        model.addAttribute("active", "dashboard");
        model.addAttribute("me", me);

        // ---------- 基础统计 ----------
        List<CompanionCard> allCards = companionStatsService.loadAllCards();
        long companionCount = allCards.size();
        long onlineCount = companionRepository.findByOnlineTrueOrderByIdAsc().size();
        long playerCount = userRepository.findByRoleOrderByIdAsc(Role.PLAYER).size();

        long totalOrders = orderRepository.count();
        long pendingCount = orderRepository.countByStatus(OrderStatus.PENDING);
        long acceptedCount = orderRepository.countByStatus(OrderStatus.ACCEPTED);
        long completedCount = orderRepository.countByStatus(OrderStatus.COMPLETED);
        long cancelledCount = orderRepository.countByStatus(OrderStatus.CANCELLED);

        List<Review> allReviews = reviewRepository.findAllByOrderByCreateTimeDesc();
        double avgRating = allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        // 已完成订单的总金额（平台流水）
        BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .map(GameOrder::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 平台抽成合计 / 陪玩收入合计（来自结算单）
        BigDecimal totalCommission = settlementRepository.findAll().stream()
                .map(Settlement::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalNet = settlementRepository.findAll().stream()
                .map(Settlement::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        model.addAttribute("companionCount", companionCount);
        model.addAttribute("onlineCount", onlineCount);
        model.addAttribute("playerCount", playerCount);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("acceptedCount", acceptedCount);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("avgRating", String.format("%.1f", avgRating));
        model.addAttribute("reviewCount", allReviews.size());
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalCommission", totalCommission);
        model.addAttribute("totalNet", totalNet);

        // 状态分布条形图百分比（0~100）
        model.addAttribute("pendingPct", percent(pendingCount, totalOrders));
        model.addAttribute("acceptedPct", percent(acceptedCount, totalOrders));
        model.addAttribute("completedPct", percent(completedCount, totalOrders));
        model.addAttribute("cancelledPct", percent(cancelledCount, totalOrders));

        // ---------- 最新订单（按角色过滤） ----------
        List<GameOrder> visibleOrders = visibleOrders(me);
        model.addAttribute("recentOrders", visibleOrders.stream().limit(6).toList());
        model.addAttribute("recentOrderTotal", visibleOrders.size());

        // ---------- 陪玩评分榜（前 3 名） ----------
        model.addAttribute("ranking", companionStatsService.loadTopCards(3));

        // ---------- 我的数据（按角色不同） ----------
        model.addAttribute("personalStats", personalStats(me, visibleOrders));

        return "dashboard";
    }

    /** 根据当前用户角色，返回他能看到的订单列表 */
    private List<GameOrder> visibleOrders(User me) {
        if (me.getRole() == Role.ADMIN) {
            return orderRepository.findAllByOrderByCreateTimeDesc();
        }
        if (me.getRole() == Role.PLAYER) {
            return orderRepository.findByPlayerIdOrderByCreateTimeDesc(me.getId());
        }
        Companion companion = companionRepository.findByUserId(me.getId()).orElse(null);
        if (companion == null) {
            return List.of();
        }
        return orderRepository.findByCompanionIdOrderByCreateTimeDesc(companion.getId());
    }

    /** 构造“我的数据”卡片：不同角色展示不同指标 */
    private List<Map<String, Object>> personalStats(User me, List<GameOrder> myOrders) {
        List<Map<String, Object>> stats = new ArrayList<>();

        if (me.getRole() == Role.ADMIN) {
            long unsettled = orderRepository.findAll().stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .filter(o -> !settlementRepository.existsByOrderId(o.getId()))
                    .count();
            stats.add(item("待结算订单", unsettled + " 单"));
            stats.add(item("平台抽成合计", "¥ " + totalCommissionOfSettlements()));
            stats.add(item("结算单数量", settlementRepository.count() + " 单"));
            stats.add(item("注册用户", userRepository.count() + " 人"));
        } else if (me.getRole() == Role.PLAYER) {
            long myPending = myOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            BigDecimal mySpent = myOrders.stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .map(GameOrder::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            long myReviews = reviewRepository.findByPlayerIdOrderByCreateTimeDesc(me.getId()).size();
            stats.add(item("我的订单", myOrders.size() + " 单"));
            stats.add(item("待接单", myPending + " 单"));
            stats.add(item("累计消费", "¥ " + mySpent));
            stats.add(item("我的评价", myReviews + " 条"));
            // 额外提示：已完成但还没评价的订单数
            modelAttributeHint(me, stats);
        } else {
            long myPending = myOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();
            long myAccepted = myOrders.stream().filter(o -> o.getStatus() == OrderStatus.ACCEPTED).count();
            long myCompleted = myOrders.stream().filter(o -> o.getStatus() == OrderStatus.COMPLETED).count();
            BigDecimal myIncome = myIncome(me);
            stats.add(item("待接单", myPending + " 单"));
            stats.add(item("服务中", myAccepted + " 单"));
            stats.add(item("已完成", myCompleted + " 单"));
            stats.add(item("累计收入", "¥ " + myIncome));
        }
        return stats;
    }

    /** 陪玩师已结算收入合计 */
    private BigDecimal myIncome(User me) {
        Companion companion = companionRepository.findByUserId(me.getId()).orElse(null);
        if (companion == null) {
            return BigDecimal.ZERO;
        }
        return settlementRepository.findByCompanionIdOrderBySettleTimeDesc(companion.getId()).stream()
                .map(Settlement::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** 平台抽成合计（重复计算一次，方便 personalStats 使用） */
    private BigDecimal totalCommissionOfSettlements() {
        return settlementRepository.findAll().stream()
                .map(Settlement::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** 玩家：统计“已完成但未评价”的订单，追加为提示 */
    private void modelAttributeHint(User me, List<Map<String, Object>> stats) {
        long unreviewed = orderRepository.findByPlayerIdAndStatusOrderByCreateTimeDesc(me.getId(), OrderStatus.COMPLETED)
                .stream()
                .filter(o -> !reviewRepository.existsByOrderId(o.getId()))
                .count();
        stats.add(item("待评价", unreviewed + " 单"));
    }

    private Map<String, Object> item(String label, String value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("label", label);
        map.put("value", value);
        return map;
    }

    private int percent(long part, long total) {
        if (total <= 0) {
            return 0;
        }
        return (int) Math.round(part * 100.0 / total);
    }
}
