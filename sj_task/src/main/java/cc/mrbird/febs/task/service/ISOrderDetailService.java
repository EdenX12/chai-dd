package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SOrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISOrderDetailService extends IService<SOrderDetail> {

    /**
     * 检索订单
     *
     * @param orderDetail SOrderDetail
     */
    List<SOrderDetail> findOrderDetailList(SOrderDetail orderDetail);
}
