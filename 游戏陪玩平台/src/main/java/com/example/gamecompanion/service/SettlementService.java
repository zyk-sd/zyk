package com.example.gamecompanion.service;

import com.example.gamecompanion.model.GameOrder;
import com.example.gamecompanion.model.Settlement;
import com.example.gamecompanion.repository.SettlementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 结算服务：封装“平台抽成 10%”的计算规则与结算入库逻辑。
 */
@Service
public class SettlementService {

    /** 平台抽成比例：10% */
    public static final BigDecimal COMMISSION_RATE = new BigDecimal("0.10");

    private final SettlementRepository settlementRepository;

    public SettlementService(SettlementRepository settlementRepository) {
        this.settlementRepository = settlementRepository;
    }

    /**
     * 为订单生成结算单。
     * 金额规则：
     *   grossAmount（订单总额） = 订单金额
     *   commission（平台抽成）  = 订单金额 × 10%
     *   netAmount（陪玩收入）   = 订单金额 - 平台抽成
     */
    @Transactional
    public Settlement settle(GameOrder order) {
        BigDecimal gross = order.getAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal commission = gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(commission).setScale(2, RoundingMode.HALF_UP);

        Settlement settlement = new Settlement();
        settlement.setOrder(order);
        settlement.setCompanion(order.getCompanion());
        settlement.setGrossAmount(gross);
        settlement.setCommission(commission);
        settlement.setNetAmount(net);
        settlement.setSettleTime(LocalDateTime.now());
        return settlementRepository.save(settlement);
    }
}
