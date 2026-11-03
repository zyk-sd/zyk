package com.example.gamecompanion.controller;

import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.Settlement;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.SettlementRepository;
import com.example.gamecompanion.service.SettlementService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 结算管理控制器。
 * 管理员：可对“已完成且未结算”的订单发起结算，并查看全部结算单；
 * 陪玩师：查看自己的结算单与累计收入；
 * 玩家：查看自己订单的结算情况。
 */
@Controller
public class SettlementController {

    private final SettlementRepository settlementRepository;
    private final GameOrderRepository orderRepository;
    private final CompanionRepository companionRepository;
    private final SettlementService settlementService;

    public SettlementController(SettlementRepository settlementRepository,
                                GameOrderRepository orderRepository,
                                CompanionRepository companionRepository,
                                SettlementService settlementService) {
        this.settlementRepository = settlementRepository;
        this.orderRepository = orderRepository;
        this.companionRepository = companionRepository;
        this.settlementService = settlementService;
    }

    /** 结算列表 */
    @GetMapping("/settlements")
    public String list(HttpSession session, Model model) {
        User me = (User) session.getAttribute("loginUser");
        List<Settlement> settlements;
        List<GameOrder> unsettledOrders = List.of();

        if (me.getRole() == Role.ADMIN) {
            settlements = settlementRepository.findAllByOrderBySettleTimeDesc();
            unsettledOrders = orderRepository.findAllByOrderByCreateTimeDesc().stream()
                    .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                    .filter(o -> !settlementRepository.existsByOrderId(o.getId()))
                    .toList();
        } else if (me.getRole() == Role.COMPANION) {
            Companion companion = companionRepository.findByUserId(me.getId()).orElse(null);
            settlements = companion == null
                    ? List.of()
                    : settlementRepository.findByCompanionIdOrderBySettleTimeDesc(companion.getId());
        } else {
            // 玩家：只看自己订单产生的结算单
            settlements = settlementRepository.findAllByOrderBySettleTimeDesc().stream()
                    .filter(s -> s.getOrder().getPlayer().getId().equals(me.getId()))
                    .toList();
        }

        BigDecimal grossSum = settlements.stream().map(Settlement::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal commissionSum = settlements.stream().map(Settlement::getCommission)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSum = settlements.stream().map(Settlement::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);

        // 待结算订单的“预计陪玩收入”，供页面显示
        Map<Long, BigDecimal> expectedNetMap = new HashMap<>();
        for (GameOrder order : unsettledOrders) {
            BigDecimal expected = order.getAmount()
                    .multiply(BigDecimal.ONE.subtract(SettlementService.COMMISSION_RATE))
                    .setScale(2, RoundingMode.HALF_UP);
            expectedNetMap.put(order.getId(), expected);
        }

        model.addAttribute("expectedNetMap", expectedNetMap);

        model.addAttribute("active", "settlements");
        model.addAttribute("settlements", settlements);
        model.addAttribute("unsettledOrders", unsettledOrders);
        model.addAttribute("isAdmin", me.getRole() == Role.ADMIN);
        model.addAttribute("isCompanion", me.getRole() == Role.COMPANION);
        model.addAttribute("isPlayer", me.getRole() == Role.PLAYER);
        model.addAttribute("grossSum", grossSum);
        model.addAttribute("commissionSum", commissionSum);
        model.addAttribute("netSum", netSum);
        return "settlements";
    }

    /** 发起结算（仅管理员，订单必须已完成且未结算） */
    @PostMapping("/settlements/create/{orderId}")
    public String create(@PathVariable Long orderId, HttpSession session, RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        if (me.getRole() != Role.ADMIN) {
            ra.addFlashAttribute("error", "只有管理员可以发起结算");
            return "redirect:/settlements";
        }
        GameOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "订单不存在");
            return "redirect:/settlements";
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            ra.addFlashAttribute("error", "只有已完成的订单才能结算");
            return "redirect:/settlements";
        }
        if (settlementRepository.existsByOrderId(orderId)) {
            ra.addFlashAttribute("error", "该订单已经结算过，不能重复结算");
            return "redirect:/settlements";
        }

        Settlement settlement = settlementService.settle(order);
        ra.addFlashAttribute("msg", "订单 " + order.getOrderNo() + " 结算完成，陪玩收入 ¥ "
                + settlement.getNetAmount() + "（平台抽成 ¥ " + settlement.getCommission() + "）");
        return "redirect:/settlements";
    }
}
