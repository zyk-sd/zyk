package com.example.gamecompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 订单评价实体。
 * 一个订单只能评价一次（order_id 设为唯一）。
 * 评分 1~5 星，并附带文字评价。
 */
@Entity
@Table(name = "order_review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 被评价的订单，一个订单只能有一条评价 */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private GameOrder order;

    /** 评价玩家 */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "player_id")
    private User player;

    /** 被评价的陪玩账号 */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "companion_id")
    private Companion companion;

    /** 评分：1~5 星 */
    @Column(nullable = false)
    private Integer rating;

    /** 评价内容 */
    @Column(length = 300)
    private String content;

    /** 评价时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;

    public Review() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public GameOrder getOrder() {
        return order;
    }

    public void setOrder(GameOrder order) {
        this.order = order;
    }

    public User getPlayer() {
        return player;
    }

    public void setPlayer(User player) {
        this.player = player;
    }

    public Companion getCompanion() {
        return companion;
    }

    public void setCompanion(Companion companion) {
        this.companion = companion;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
