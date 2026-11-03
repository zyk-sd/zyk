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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算单实体。
 * 订单完成后由管理员发起结算：
 *   平台抽成 = 订单金额 × 10%
 *   陪玩收入 = 订单金额 - 平台抽成
 * 每个订单只能结算一次（order_id 唯一）。
 */
@Entity
@Table(name = "settlement")
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联订单，一个订单只能结算一次 */
    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private GameOrder order;

    /** 获得收入的陪玩账号 */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "companion_id")
    private Companion companion;

    /** 订单总额 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal grossAmount;

    /** 平台抽成（10%） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal commission;

    /** 陪玩实际收入 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal netAmount;

    /** 结算时间 */
    @Column(nullable = false)
    private LocalDateTime settleTime;

    public Settlement() {
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

    public Companion getCompanion() {
        return companion;
    }

    public void setCompanion(Companion companion) {
        this.companion = companion;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public LocalDateTime getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(LocalDateTime settleTime) {
        this.settleTime = settleTime;
    }
}
