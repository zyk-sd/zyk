package com.example.gamecompanion.service;

import com.example.gamecompanion.dto.CompanionCard;
import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.Review;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 陪玩账号统计服务：
 * 把陪玩账号 + 平均评分 + 评价数量 + 订单数量组装成页面展示对象。
 */
@Service
public class CompanionStatsService {

    private final CompanionRepository companionRepository;
    private final ReviewRepository reviewRepository;
    private final GameOrderRepository orderRepository;

    public CompanionStatsService(CompanionRepository companionRepository,
                                 ReviewRepository reviewRepository,
                                 GameOrderRepository orderRepository) {
        this.companionRepository = companionRepository;
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
    }

    /** 查询全部陪玩账号（附带统计数据），按 id 升序 */
    public List<CompanionCard> loadAllCards() {
        List<Review> allReviews = reviewRepository.findAllByOrderByCreateTimeDesc();
        List<Companion> companions = companionRepository.findAllByOrderByIdAsc();

        List<CompanionCard> cards = new ArrayList<>();
        for (Companion companion : companions) {
            List<Review> reviews = allReviews.stream()
                    .filter(r -> r.getCompanion().getId().equals(companion.getId()))
                    .toList();
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            long orderCount = orderRepository.findByCompanionIdOrderByCreateTimeDesc(companion.getId()).size();
            cards.add(new CompanionCard(companion, avg, reviews.size(), orderCount));
        }
        return cards;
    }

    /** 查询单个陪玩账号的展示对象 */
    public CompanionCard loadCard(Companion companion) {
        List<Review> reviews = reviewRepository.findByCompanionIdOrderByCreateTimeDesc(companion.getId());
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        long orderCount = orderRepository.findByCompanionIdOrderByCreateTimeDesc(companion.getId()).size();
        return new CompanionCard(companion, avg, reviews.size(), orderCount);
    }

    /** 评分排行榜：按平均评分倒序，只取前 limit 名（没有评价的陪玩排在后面） */
    public List<CompanionCard> loadTopCards(int limit) {
        List<CompanionCard> cards = loadAllCards();
        cards.sort(Comparator.comparingDouble(CompanionCard::getAvgRating).reversed()
                .thenComparing(c -> c.getCompanion().getId()));
        return cards.stream().limit(limit).toList();
    }

    /** 未使用但保留：按某个字段排序（演示 Spring Data Sort 写法） */
    public List<Companion> loadAllSortedByPrice() {
        return companionRepository.findAll(Sort.by(Sort.Direction.ASC, "pricePerHour"));
    }
}
