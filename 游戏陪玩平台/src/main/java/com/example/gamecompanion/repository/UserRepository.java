package com.example.gamecompanion.repository;

import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户账号数据访问接口。
 * 继承 JpaRepository 后，Spring Data JPA 会自动生成增删改查的 SQL 实现。
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** 根据登录名查找账号（登录功能使用） */
    Optional<User> findByUsername(String username);

    /** 判断登录名是否已存在 */
    boolean existsByUsername(String username);

    /** 按角色查询账号（新增陪玩账号时，可选择尚未绑定的陪玩师账号） */
    List<User> findByRoleOrderByIdAsc(Role role);
}
