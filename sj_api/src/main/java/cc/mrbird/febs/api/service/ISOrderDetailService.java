package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface ISOrderDetailService extends IService<SOrderDetail> {

    /**
     * 增加用户购买订单明细
     *
     * @param orderDetail SOrderDetail
     */
    SOrderDetail addOrderDetail(SOrderDetail orderDetail);

    /**
     * 更新订单状态
     *
     * @param orderDetail SOrderDetail
     */
    void updateOrderDetail(SOrderDetail orderDetail);

    /**
     * 检索订单
     *
     * @param orderDetail SOrderDetail
     */
    List<SOrderDetail> findOrderDetailList(SOrderDetail orderDetail);

    /**
     * 根据订单号查询商品id和数量
     * @param orderId
     * @return
     */
    List<Map<String,Object>>queryProductByOrder(String orderId);

    List<String> queryIdByProductOrder(String productId,String orderId);
}
