package cc.mrbird.febs.task.mapper;

import cc.mrbird.febs.task.entity.SOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import java.util.List;

/**
 * @author MrBird
 */
public interface SOrderMapper extends BaseMapper<SOrder> {

    /**
     * 支付成功时间超过5分钟的订单处理
     * @return
     */
    List<SOrder> queryOrderPaySuccessList();
}
