package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


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

}
