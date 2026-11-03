package com.example.gamecompanion.repository;

import com.example.gamecompanion.model.Companion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 陪玩账号数据访问接口。
 */
@Repository
public interface CompanionRepository extends JpaRepository<Companion, Long> {

    /** 全部陪玩账号，按 id 升序 */
    List<Companion> findAllByOrderByIdAsc();

    /** 根据登录账号 id 找到对应的陪玩资料（陪玩师登录后使用） */
    Optional<Companion> findByUserId(Long userId);

    /** 判断某个登录账号是否已绑定陪玩资料 */
    boolean existsByUserId(Long userId);

    /** 查询所有在线的陪玩账号（下单页只显示在线陪玩） */
    List<Companion> findByOnlineTrueOrderByIdAsc();
}
