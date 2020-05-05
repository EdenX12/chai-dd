package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserCoupon;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserCouponService extends IService<SUserCoupon> {

    List<SUserCoupon> findUserCouponList(String userId, String productId, Integer couponStatus, Integer couponType);

    SUserCoupon findUserCoupon(String userId, String userCouponId);

}
