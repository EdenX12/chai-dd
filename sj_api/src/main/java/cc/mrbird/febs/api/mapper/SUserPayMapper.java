package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserPay;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface SUserPayMapper extends BaseMapper<SUserPay> {
    void updateTaskLineForPay(@Param("orderId")String orderId);

    void updateUserTaskLineForPay(@Param("orderId")String orderId);

}
