package com.example.gamecompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 陪玩订单实体。
 * 下单时会把陪玩账号的单价快照到 unitPrice 中并计算总金额，
 * 避免陪玩账号后续改价影响历史订单。
 */
@Entity
@Table(name = "game_order")
public class GameOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 订单编号，例如 GP202610310001 */
    @Column(nullable = false, unique = true, length = 30)
    private String orderNo;

    /** 下单玩家 */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "player_id")
    private User player;

    /** 被预约的陪玩账号 */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "companion_id")
    private Companion companion;

    /** 服务时长（小时） */
    @Column(nullable = false)
    private Integer hours;

    /** 下单时的单价快照 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /** 订单总金额 = 单价 × 时长 */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** 订单状态 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    /** 玩家备注 */
    @Column(length = 300)
    private String remark;

    /** 下单时间 */
    @Column(nullable = false)
    private LocalDateTime createTime;

    /** 接单时间 */
    private LocalDateTime acceptTime;

    /** 完成时间 */
    private LocalDateTime finishTime;

    public GameOrder() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
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

    public Integer getHours() {
        return hours;
    }

    public void setHours(Integer hours) {
        this.hours = hours;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getAcceptTime() {
        return acceptTime;
    }

    public void setAcceptTime(LocalDateTime acceptTime) {
        this.acceptTime = acceptTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }
}
