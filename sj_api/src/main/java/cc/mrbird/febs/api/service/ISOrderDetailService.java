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
     * 检索订单
     *
     * @param orderDetail SOrderDetail
     */
    List<SOrderDetail> findOrderDetailList(SOrderDetail orderDetail);
}
