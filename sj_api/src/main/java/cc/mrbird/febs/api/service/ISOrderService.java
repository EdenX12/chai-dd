package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOrder;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * @author MrBird
 */
public interface ISOrderService extends IService<SOrder> {

    /**
     * 增加用户购买批量订单
     *
     * @param order SOrder
     */
    SOrder addOrder(SOrder order);

    /**
     * 查询用户购买订单列表
     *
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> queryPage(QueryRequest queryRequest, String userId, String status);

    /**
     * 查询详情
     * @param orderDetailId
     * @return
     */
    Map<String, Object> queryOrderDetail(String orderDetailId);

    /**
     * 更新用户购买订单状态
     *
     * @param order SOrder
     */
    SOrder updateOrder(SOrder order);

}
