package com.example.gamecompanion.dto;

import com.example.gamecompanion.model.Companion;

/**
 * 陪玩账号展示对象。
 * 在陪玩账号实体之外，额外携带“平均评分、评价数量、订单数量”等统计信息，
 * 供页面表格和排行榜使用。
 */
public class CompanionCard {

    private final Companion companion;
    private final double avgRating;
    private final long reviewCount;
    private final long orderCount;

    public CompanionCard(Companion companion, double avgRating, long reviewCount, long orderCount) {
        this.companion = companion;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
        this.orderCount = orderCount;
    }

    public Companion getCompanion() {
        return companion;
    }

    /** 平均评分，保留一位小数（没有评价时为 0） */
    public String getAvgRatingText() {
        return String.format("%.1f", avgRating);
    }

    public double getAvgRating() {
        return avgRating;
    }

    public long getReviewCount() {
        return reviewCount;
    }

    public long getOrderCount() {
        return orderCount;
    }
}
