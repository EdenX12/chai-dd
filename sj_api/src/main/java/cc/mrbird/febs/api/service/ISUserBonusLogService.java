package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserBonusLogService extends IService<SUserBonusLog> {

    /**
     * 查询用户奖励金额暂放数据
     *
     * @param userBonusLog SUserBonusLog
     * @return List
     */
    List<SUserBonusLog> findUserBonusList(SUserBonusLog userBonusLog);

    List<SUserBonusLog> findUserBonus( String userId,  String productId,Integer isToday);


    BigDecimal getSettlementAmt(String userId);
/*
    *//**
     * 战队今日累计收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusOrgRewardTodaySum(@Param("userId") String userId);

    *//**
     * 任务躺赢收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusTaskRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    *//**
     * 战队累计收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusOrgRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    *//**
     * 买家返收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusBuyerRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    *//**
     * 任务躺赢收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusTaskRewardSum1(@Param("userId") String userId, @Param("productId") String productId);

    *//**
     * 战队累计收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusOrgRewardSum1(@Param("userId") String userId, @Param("productId") String productId);

    *//**
     * 买家返收益
     *
     * @param userId String
     * @return BigDecimal
     *//*
    BigDecimal findUserBonusBuyerRewardSum1(@Param("userId") String userId, @Param("productId") String productId);*/
}
