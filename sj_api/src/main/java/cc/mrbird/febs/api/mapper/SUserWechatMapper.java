package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserWechat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * @author MrBird
 */
@Repository
public interface SUserWechatMapper extends BaseMapper<SUserWechat> {

    BigDecimal getWithdrawingAmount(@Param("userId") String userId);

    BigDecimal getWithdrawSuccessAmount(@Param("userId") String userId);
}
