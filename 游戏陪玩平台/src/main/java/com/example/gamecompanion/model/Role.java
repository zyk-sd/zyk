package com.example.gamecompanion.model;

/**
 * 系统角色枚举。
 * ADMIN      —— 管理员（管理陪玩账号、处理结算、查看全部数据）
 * PLAYER     —— 玩家（下单、评价）
 * COMPANION  —— 陪玩师（接单、完成订单、查看收入）
 */
public enum Role {

    ADMIN("管理员"),
    PLAYER("玩家"),
    COMPANION("陪玩师");

    private final String label;

    Role(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
