package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserPay;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserPayService extends IService<SUserPay> {
    /**
     * 购买商品的订单支付成功时候更新任务线状态为结算中
     * @param orderId
     */
    void updateTaskLineForPay(String orderId);
    /**
     * 购买商品的订单支付成功时候更新用户任务任务线关联表状态为结算中
     * @param orderId
     */
    void updateUserTaskLineForPay(String orderId);

}
