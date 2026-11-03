package com.example.gamecompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * 陪玩账号实体（平台上的“陪玩商品”）。
 * 包含游戏、区服、段位、单价、在线状态、个人介绍等信息，
 * 并通过 userId 关联一个角色为 COMPANION 的登录账号。
 */
@Entity
@Table(name = "companion_account")
public class Companion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 陪玩昵称 */
    @Column(nullable = false, length = 50)
    private String name;

    /** 游戏名称，例如：王者荣耀、英雄联盟 */
    @Column(nullable = false, length = 50)
    private String gameName;

    /** 区服，例如：微信区、艾欧尼亚 */
    @Column(length = 50)
    private String serverName;

    /** 段位水平 */
    @Column(length = 50)
    private String rankLevel;

    /** 每小时单价（元） */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    /** 是否在线接单 */
    @Column(nullable = false)
    private Boolean online = true;

    /** 个人介绍 */
    @Column(length = 300)
    private String introduction;

    /** 绑定的登录账号（可为空表示暂未绑定） */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public Companion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getRankLevel() {
        return rankLevel;
    }

    public void setRankLevel(String rankLevel) {
        this.rankLevel = rankLevel;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
