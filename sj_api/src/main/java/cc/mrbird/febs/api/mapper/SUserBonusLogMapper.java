package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * @author MrBird
 */
public interface SUserBonusLogMapper extends BaseMapper<SUserBonusLog> {

    /*BigDecimal findUserBonusOrgRewardTodaySum(@Param("userId") String userId);

    BigDecimal findUserBonusOrgRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusTaskRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusBuyerRewardSum0(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusOrgRewardSum1(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusTaskRewardSum1(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusBuyerRewardSum1(@Param("userId") String userId, @Param("productId") String productId);*/

    List<SUserBonusLog> findUserBonus(@Param("userId") String userId, @Param("productId") String productId,@Param("isToday")Integer isToday);

    /**
     * 查询我的账户结算中金额
     * @return
     */
    BigDecimal getSettlementAmt(@Param("userId") String userId);


    IPage<Map> getBonusDetails(Page page, @Param("userId") String userId);
}
