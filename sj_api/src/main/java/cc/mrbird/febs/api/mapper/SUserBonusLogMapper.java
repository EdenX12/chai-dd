package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SUserBonusLogMapper extends BaseMapper<SUserBonusLog> {

    BigDecimal findUserBonusRewardSum(@Param("userId") String userId);

    BigDecimal findUserBonusRewardTodaySum(@Param("userId") String userId);
}
