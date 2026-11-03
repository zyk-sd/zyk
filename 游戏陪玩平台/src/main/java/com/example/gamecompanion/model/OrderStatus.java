package com.example.gamecompanion.model;

/**
 * 订单状态枚举。
 * PENDING    —— 待接单（玩家已下单，等待陪玩师接单）
 * ACCEPTED   —— 服务中（陪玩师已接单）
 * COMPLETED  —— 已完成（陪玩师完成服务，玩家可评价，管理员可结算）
 * CANCELLED  —— 已取消（玩家取消订单）
 */
public enum OrderStatus {

    PENDING("待接单"),
    ACCEPTED("服务中"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
