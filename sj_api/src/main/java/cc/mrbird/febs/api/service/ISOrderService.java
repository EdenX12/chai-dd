package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISOrderService extends IService<SOrder> {

    /**
     * 增加用户购买订单
     *
     * @param order SOrder
     */
    int addOrder(SOrder order);

    /**
     * 查询用户购买订单列表
     *
     * @param order SOrder
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<SOrder> findOrderList(SOrder order, QueryRequest queryRequest);

    /**
     * 查询用户购买订单详情
     *
     * @param order SOrder
     * @return SOrder
     */
    SOrder findOrderDetail(SOrder order);

    /**
     * 更新用户购买订单状态
     *
     * @param order SOrder
     */
    void updateOrder(SOrder order);
}
