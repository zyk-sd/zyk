package com.example.gamecompanion.service;

import com.example.gamecompanion.model.Companion;
import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.OrderStatus;
import com.example.gamecompanion.model.Review;
import com.example.gamecompanion.model.Role;
import com.example.gamecompanion.model.Settlement;
import com.example.gamecompanion.model.User;
import com.example.gamecompanion.repository.CompanionRepository;
import com.example.gamecompanion.repository.GameOrderRepository;
import com.example.gamecompanion.repository.ReviewRepository;
import com.example.gamecompanion.repository.SettlementRepository;
import com.example.gamecompanion.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 示例数据初始化器。
 * 项目第一次启动（数据库为空）时自动创建账号、陪玩账号、订单、评价、结算等演示数据；
 * 之后重启不会重复插入。
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanionRepository companionRepository;
    private final GameOrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final SettlementRepository settlementRepository;

    public DataInitializer(UserRepository userRepository,
                           CompanionRepository companionRepository,
                           GameOrderRepository orderRepository,
                           ReviewRepository reviewRepository,
                           SettlementRepository settlementRepository) {
        this.userRepository = userRepository;
        this.companionRepository = companionRepository;
        this.orderRepository = orderRepository;
        this.reviewRepository = reviewRepository;
        this.settlementRepository = settlementRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // 已有数据，无需初始化
        }

        // ============ 1. 创建登录账号（密码统一为 123456，仅用于教学演示） ============
        User admin = userRepository.save(new User("admin", "123456", "平台管理员", Role.ADMIN, "13800000001"));
        User player1 = userRepository.save(new User("player01", "123456", "小星", Role.PLAYER, "13800000002"));
        User player2 = userRepository.save(new User("player02", "123456", "阿豪", Role.PLAYER, "13800000003"));
        User c1User = userRepository.save(new User("companion01", "123456", "夜风", Role.COMPANION, "13800000004"));
        User c2User = userRepository.save(new User("companion02", "123456", "小鹿", Role.COMPANION, "13800000005"));
        User c3User = userRepository.save(new User("companion03", "123456", "阿凯", Role.COMPANION, "13800000006"));
        User c4User = userRepository.save(new User("companion04", "123456", "糖糖", Role.COMPANION, "13800000007"));

        // ============ 2. 创建陪玩账号 ============
        Companion nightWind = saveCompanion("夜风", "王者荣耀", "微信区", "荣耀王者",
                "45.00", true, "国服打野，节奏带飞，全程语音指挥。", c1User);
        Companion deer = saveCompanion("小鹿", "英雄联盟", "艾欧尼亚", "钻石 I",
                "38.00", true, "温柔辅助专精，可玩任何位置，会聊天不冷场。", c2User);
        Companion akai = saveCompanion("阿凯", "和平精英", "微信区", "无敌战神",
                "40.00", false, "四指操作，刚枪教学，擅长带妹上分。", c3User);
        Companion sugar = saveCompanion("糖糖", "永劫无间", "官方服", "铂金",
                "26.00", true, "新人陪玩，声音甜美，欢迎体验。", c4User);

        LocalDateTime now = LocalDateTime.now();

        // ============ 3. 创建历史订单 ============
        // 订单 1：3 天前完成，已评价，已结算
        GameOrder o1 = saveOrder("001", player1, nightWind, 3, OrderStatus.COMPLETED,
                now.minusDays(3), now.minusDays(3).plusMinutes(10), now.minusDays(3).plusHours(3),
                "打排位，希望稳定一些。");
        // 订单 2：2 小时前下单，等待接单
        GameOrder o2 = saveOrder("002", player2, deer, 2, OrderStatus.PENDING,
                now.minusHours(2), null, null, "晚上八点开始，玩匹配。");
        // 订单 3：1 天前接单，服务中
        GameOrder o3 = saveOrder("003", player1, deer, 4, OrderStatus.ACCEPTED,
                now.minusDays(1), now.minusDays(1).plusMinutes(5), null, "想学打野思路。");
        // 订单 4：1 天前完成，已评价，未结算（用于演示结算）
        GameOrder o4 = saveOrder("004", player2, akai, 2, OrderStatus.COMPLETED,
                now.minusDays(1), now.minusDays(1).plusMinutes(15), now.minusDays(1).plusHours(2),
                "教一下压枪技巧。");
        // 订单 5：5 小时前完成，未评价（用于演示评价）
        GameOrder o5 = saveOrder("005", player1, nightWind, 2, OrderStatus.COMPLETED,
                now.minusHours(5), now.minusHours(5).plusMinutes(8), now.minusHours(3),
                "随便玩玩，开心就好。");
        // 订单 6：30 分钟前下单，等待接单
        GameOrder o6 = saveOrder("006", player2, sugar, 1, OrderStatus.PENDING,
                now.minusMinutes(30), null, null, "第一次玩永劫，求带。");
        // 订单 7：4 天前取消
        GameOrder o7 = saveOrder("007", player1, akai, 1, OrderStatus.CANCELLED,
                now.minusDays(4), null, null, "临时有事，取消订单。");

        // ============ 4. 创建评价（订单 1 五星、订单 4 四星） ============
        reviewRepository.save(buildReview(o1, player1, 5, "打野节奏太强了，三局两胜直接上分，下次还找他！"));
        reviewRepository.save(buildReview(o4, player2, 4, "压枪教学很有耐心，就是麦克风有点小，整体不错。"));

        // ============ 5. 创建结算单（订单 1 已结算） ============
        BigDecimal gross = o1.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal commission = gross.multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
        Settlement settlement = new Settlement();
        settlement.setOrder(o1);
        settlement.setCompanion(nightWind);
        settlement.setGrossAmount(gross);
        settlement.setCommission(commission);
        settlement.setNetAmount(gross.subtract(commission));
        settlement.setSettleTime(now.minusDays(2));
        settlementRepository.save(settlement);

        System.out.println("示例数据初始化完成：账号 7 个，陪玩账号 4 个，订单 7 条，评价 2 条，结算 1 条。");
    }

    private Companion saveCompanion(String name, String game, String server, String rank,
                                    String price, boolean online, String intro, User user) {
        Companion c = new Companion();
        c.setName(name);
        c.setGameName(game);
        c.setServerName(server);
        c.setRankLevel(rank);
        c.setPricePerHour(new BigDecimal(price));
        c.setOnline(online);
        c.setIntroduction(intro);
        c.setUser(user);
        return companionRepository.save(c);
    }

    private GameOrder saveOrder(String seq, User player, Companion companion, int hours,
                                OrderStatus status, LocalDateTime createTime,
                                LocalDateTime acceptTime, LocalDateTime finishTime, String remark) {
        GameOrder order = new GameOrder();
        order.setOrderNo("GP" + createTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + seq);
        order.setPlayer(player);
        order.setCompanion(companion);
        order.setHours(hours);
        order.setUnitPrice(companion.getPricePerHour());
        order.setAmount(companion.getPricePerHour()
                .multiply(BigDecimal.valueOf(hours))
                .setScale(2, RoundingMode.HALF_UP));
        order.setStatus(status);
        order.setRemark(remark);
        order.setCreateTime(createTime);
        order.setAcceptTime(acceptTime);
        order.setFinishTime(finishTime);
        return orderRepository.save(order);
    }

    private Review buildReview(GameOrder order, User player, int rating, String content) {
        Review review = new Review();
        review.setOrder(order);
        review.setPlayer(player);
        review.setCompanion(order.getCompanion());
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(order.getFinishTime() == null ? LocalDateTime.now() : order.getFinishTime().plusMinutes(20));
        return review;
    }
}
