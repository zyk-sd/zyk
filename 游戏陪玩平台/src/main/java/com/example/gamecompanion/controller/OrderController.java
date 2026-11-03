package com.example.gamecompanion.controller;

import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import com.example.gamecompanion.repository.SettlementRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单中心控制器：下单、接单、完成、取消、按状态筛选。
 */
@Controller
public class OrderController {

    private final GameOrderRepository orderRepository;
    private final CompanionRepository companionRepository;
    private final ReviewRepository reviewRepository;
    private final SettlementRepository settlementRepository;

    public OrderController(GameOrderRepository orderRepository,
                           CompanionRepository companionRepository,
                           ReviewRepository reviewRepository,
                           SettlementRepository settlementRepository) {
        this.orderRepository = orderRepository;
        this.companionRepository = companionRepository;
        this.reviewRepository = reviewRepository;
        this.settlementRepository = settlementRepository;
    }

    /**
     * 订单列表：按角色过滤 + 按状态筛选。
     * 管理员看全部；玩家看自己的；陪玩师看分配给自己的。
     */
    @GetMapping("/orders")
    public String list(@RequestParam(value = "status", required = false) String status,
                       HttpSession session, Model model) {
        User me = (User) session.getAttribute("loginUser");

        List<GameOrder> orders = visibleOrders(me);
        OrderStatus filter = parseStatus(status);
        if (filter != null) {
            orders = orders.stream().filter(o -> o.getStatus() == filter).toList();
        }

        // 每条订单的“是否已评价 / 是否已结算”状态，供页面显示按钮
        Map<Long, Boolean> reviewedMap = new HashMap<>();
        Map<Long, Boolean> settledMap = new HashMap<>();
        for (GameOrder order : orders) {
            reviewedMap.put(order.getId(), reviewRepository.existsByOrderId(order.getId()));
            settledMap.put(order.getId(), settlementRepository.existsByOrderId(order.getId()));
        }

        model.addAttribute("active", "orders");
        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("currentStatus", filter == null ? "" : filter.name());
        model.addAttribute("reviewedMap", reviewedMap);
        model.addAttribute("settledMap", settledMap);
        model.addAttribute("isPlayer", me.getRole() == Role.PLAYER);
        model.addAttribute("isAdmin", me.getRole() == Role.ADMIN);
        // 下单可选的在陪玩账号（只显示在线且已绑定账号的）
        model.addAttribute("onlineCompanions", companionRepository.findByOnlineTrueOrderByIdAsc());
        return "orders";
    }

    /** 玩家下单 */
    @PostMapping("/orders/create")
    public String create(@RequestParam("companionId") Long companionId,
                         @RequestParam("hours") Integer hours,
                         @RequestParam(value = "remark", required = false) String remark,
                         HttpSession session,
                         RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        if (me.getRole() != Role.PLAYER) {
            ra.addFlashAttribute("error", "只有玩家可以下单");
            return "redirect:/orders";
        }
        if (hours == null || hours < 1 || hours > 12) {
            ra.addFlashAttribute("error", "服务时长必须在 1~12 小时之间");
            return "redirect:/orders";
        }
        Companion companion = companionRepository.findById(companionId).orElse(null);
        if (companion == null) {
            ra.addFlashAttribute("error", "陪玩账号不存在");
            return "redirect:/orders";
        }
        if (Boolean.FALSE.equals(companion.getOnline())) {
            ra.addFlashAttribute("error", "该陪玩当前不在线，暂时无法下单");
            return "redirect:/orders";
        }

        GameOrder order = new GameOrder();
        order.setOrderNo(generateOrderNo());
        order.setPlayer(me);
        order.setCompanion(companion);
        order.setHours(hours);
        order.setUnitPrice(companion.getPricePerHour());
        order.setAmount(companion.getPricePerHour()
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP));
        order.setStatus(OrderStatus.PENDING);
        order.setRemark(remark);
        order.setCreateTime(LocalDateTime.now());
        orderRepository.save(order);

        ra.addFlashAttribute("msg", "下单成功，订单号 " + order.getOrderNo() + "，等待陪玩接单");
        return "redirect:/orders";
    }

    /** 接单：陪玩师本人或管理员 */
    @PostMapping("/orders/accept/{id}")
    public String accept(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        GameOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "订单不存在");
            return "redirect:/orders";
        }
        if (!canServe(me, order)) {
            ra.addFlashAttribute("error", "只有该订单对应的陪玩师或管理员可以接单");
            return "redirect:/orders";
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            ra.addFlashAttribute("error", "当前订单状态不是“待接单”，无法接单");
            return "redirect:/orders";
        }
        order.setStatus(OrderStatus.ACCEPTED);
        order.setAcceptTime(LocalDateTime.now());
        orderRepository.save(order);
        ra.addFlashAttribute("msg", "接单成功，订单 " + order.getOrderNo() + " 进入服务中");
        return "redirect:/orders";
    }

    /** 完成服务：陪玩师本人或管理员 */
    @PostMapping("/orders/complete/{id}")
    public String complete(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        GameOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "订单不存在");
            return "redirect:/orders";
        }
        if (!canServe(me, order)) {
            ra.addFlashAttribute("error", "只有该订单对应的陪玩师或管理员可以完成订单");
            return "redirect:/orders";
        }
        if (order.getStatus() != OrderStatus.ACCEPTED) {
            ra.addFlashAttribute("error", "只有“服务中”的订单才能标记完成");
            return "redirect:/orders";
        }
        order.setStatus(OrderStatus.COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        orderRepository.save(order);
        ra.addFlashAttribute("msg", "订单 " + order.getOrderNo() + " 已完成，玩家现在可以评价了");
        return "redirect:/orders";
    }

    /** 取消订单：下单玩家本人或管理员，仅限待接单状态 */
    @PostMapping("/orders/cancel/{id}")
    public String cancel(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        GameOrder order = orderRepository.findById(id).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "订单不存在");
            return "redirect:/orders";
        }
        boolean isOwner = order.getPlayer().getId().equals(me.getId());
        if (!isOwner && me.getRole() != Role.ADMIN) {
            ra.addFlashAttribute("error", "只有下单玩家或管理员可以取消订单");
            return "redirect:/orders";
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            ra.addFlashAttribute("error", "只有“待接单”的订单可以取消");
            return "redirect:/orders";
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        ra.addFlashAttribute("msg", "订单 " + order.getOrderNo() + " 已取消");
        return "redirect:/orders";
    }

    /** 根据当前用户角色查询订单 */
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

    /** 判断当前用户是否可以操作该订单（接单 / 完成） */
    private boolean canServe(User me, GameOrder order) {
        if (me.getRole() == Role.ADMIN) {
            return true;
        }
        if (me.getRole() != Role.COMPANION) {
            return false;
        }
        User companionUser = order.getCompanion().getUser();
        return companionUser != null && companionUser.getId().equals(me.getId());
    }

    /** 解析状态筛选参数，非法值视为“全部” */
    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** 生成订单号：GP + 时间 + 随机三位数字 */
    private String generateOrderNo() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "GP" + time + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
