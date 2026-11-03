package com.example.gamecompanion.controller;

import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Review;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评价管理控制器。
 * 玩家可以对“已完成且尚未评价”的订单打 1~5 星并填写评价内容；
 * 管理员可查看全部评价，陪玩师可查看自己收到的评价。
 */
@Controller
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final GameOrderRepository orderRepository;
    private final CompanionRepository companionRepository;

    public ReviewController(ReviewRepository reviewRepository,
                            GameOrderRepository orderRepository,
                            CompanionRepository companionRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.companionRepository = companionRepository;
    }

    /** 评价列表 + 待评价订单（玩家） */
    @GetMapping("/reviews")
    public String list(HttpSession session, Model model) {
        User me = (User) session.getAttribute("loginUser");

        List<Review> reviews = visibleReviews(me);

        // 玩家：已完成但还没有评价的订单
        List<GameOrder> pendingReviewOrders = List.of();
        if (me.getRole() == Role.PLAYER) {
            pendingReviewOrders = orderRepository
                    .findByPlayerIdAndStatusOrderByCreateTimeDesc(me.getId(), OrderStatus.COMPLETED)
                    .stream()
                    .filter(o -> !reviewRepository.existsByOrderId(o.getId()))
                    .toList();
        }

        // 评分统计（基于当前可见的评价）
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        int[] counts = new int[6]; // 下标 1~5 保存各星级数量
        for (Review review : reviews) {
            if (review.getRating() >= 1 && review.getRating() <= 5) {
                counts[review.getRating()]++;
            }
        }

        model.addAttribute("active", "reviews");
        model.addAttribute("reviews", reviews);
        model.addAttribute("pendingReviewOrders", pendingReviewOrders);
        model.addAttribute("isPlayer", me.getRole() == Role.PLAYER);
        model.addAttribute("avgRating", String.format("%.1f", avg));
        model.addAttribute("reviewTotal", reviews.size());
        model.addAttribute("r5", counts[5]);
        model.addAttribute("r4", counts[4]);
        model.addAttribute("r3", counts[3]);
        model.addAttribute("r2", counts[2]);
        model.addAttribute("r1", counts[1]);
        return "reviews";
    }

    /** 提交评价 */
    @PostMapping("/reviews/create")
    public String create(@RequestParam("orderId") Long orderId,
                         @RequestParam("rating") Integer rating,
                         @RequestParam(value = "content", required = false) String content,
                         HttpSession session,
                         RedirectAttributes ra) {
        User me = (User) session.getAttribute("loginUser");
        if (me.getRole() != Role.PLAYER) {
            ra.addFlashAttribute("error", "只有玩家可以提交评价");
            return "redirect:/reviews";
        }
        if (rating == null || rating < 1 || rating > 5) {
            ra.addFlashAttribute("error", "评分必须在 1~5 星之间");
            return "redirect:/reviews";
        }
        GameOrder order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            ra.addFlashAttribute("error", "订单不存在");
            return "redirect:/reviews";
        }
        if (!order.getPlayer().getId().equals(me.getId())) {
            ra.addFlashAttribute("error", "只能评价自己的订单");
            return "redirect:/reviews";
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            ra.addFlashAttribute("error", "订单完成后才能评价");
            return "redirect:/reviews";
        }
        if (reviewRepository.existsByOrderId(orderId)) {
            ra.addFlashAttribute("error", "该订单已经评价过了，不能重复评价");
            return "redirect:/reviews";
        }

        Review review = new Review();
        review.setOrder(order);
        review.setPlayer(me);
        review.setCompanion(order.getCompanion());
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(LocalDateTime.now());
        reviewRepository.save(review);

        ra.addFlashAttribute("msg", "评价提交成功，感谢你的反馈");
        return "redirect:/reviews";
    }

    /** 按角色查询评价：管理员全部、玩家自己写的、陪玩师收到的 */
    private List<Review> visibleReviews(User me) {
        if (me.getRole() == Role.ADMIN) {
            return reviewRepository.findAllByOrderByCreateTimeDesc();
        }
        if (me.getRole() == Role.PLAYER) {
            return reviewRepository.findByPlayerIdOrderByCreateTimeDesc(me.getId());
        }
        Companion companion = companionRepository.findByUserId(me.getId()).orElse(null);
        if (companion == null) {
            return List.of();
        }
        return reviewRepository.findByCompanionIdOrderByCreateTimeDesc(companion.getId());
    }
}
