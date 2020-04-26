package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

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
}
