package com.example.gamecompanion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 系统登录账号实体。
 * 一个账号对应一个角色；陪玩师账号还会绑定一条“陪玩账号”资料（Companion）。
 */
@Entity
@Table(name = "sys_user")
public class User {

    /** 主键 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名，唯一 */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** 登录密码（教学演示项目，直接明文保存；真实项目应使用 BCrypt 加密） */
    @Column(nullable = false, length = 100)
    private String password;

    /** 真实姓名 / 昵称 */
    @Column(nullable = false, length = 50)
    private String realName;

    /** 角色：管理员 / 玩家 / 陪玩师 */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** 手机号 */
    @Column(length = 20)
    private String phone;

    /** 账号状态：true 正常，false 禁用 */
    @Column(nullable = false)
    private Boolean enabled = true;

    public User() {
    }

    public User(String username, String password, String realName, Role role, String phone) {
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
        this.phone = phone;
        this.enabled = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
