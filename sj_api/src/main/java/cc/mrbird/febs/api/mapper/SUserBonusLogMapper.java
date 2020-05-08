package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * @author MrBird
 */
public interface SUserBonusLogMapper extends BaseMapper<SUserBonusLog> {

    BigDecimal findUserBonusOrgRewardSum(@Param("userId") String userId, @Param("productId") String productId);

    BigDecimal findUserBonusOrgRewardTodaySum(@Param("userId") String userId);

    BigDecimal findUserBonusTaskRewardSum(@Param("userId") String userId, @Param("productId") String productId);
}
