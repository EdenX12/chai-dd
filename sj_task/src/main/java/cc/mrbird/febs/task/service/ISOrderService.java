package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SOrder;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISOrderService extends IService<SOrder> {

    /**
     * 检索已付款未结算订单
     *
     * @return  List<SOrder>
     */
    List<SOrder> findOrderPaySuccessList();
}
