package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.STaskCoupon;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISTaskCouponService extends IService<STaskCoupon> {

    STaskCoupon findReturnTaskCoupon();
}
