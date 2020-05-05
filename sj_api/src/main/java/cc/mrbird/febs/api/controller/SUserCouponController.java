package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SShopCoupon;
import cc.mrbird.febs.api.entity.STaskCoupon;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserCoupon;
import cc.mrbird.febs.api.service.ISShopCouponService;
import cc.mrbird.febs.api.service.ISTaskCouponService;
import cc.mrbird.febs.api.service.ISUserCouponService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * @author MrBird
 */
@RestController
@RequestMapping("/api/s-user-coupon")
public class SUserCouponController extends BaseController {

    @Autowired
    private ISUserCouponService userCouponService;

    @Autowired
    private ISShopCouponService shopCouponService;

    @Autowired
    private ISTaskCouponService taskCouponService;

    /**
     * 取得用户的优惠券列表
     * @return List<SUserCoupon>
     */
    @PostMapping("/getUserCoupon")
    @Limit(key = "getUserCoupon", period = 60, count = 20, name = "检索用户优惠券接口", prefix = "limit")
    public FebsResponse getUserCoupon(List<String> productIds, Integer couponType, Integer couponStatus)  {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        List<SUserCoupon> userCouponList = new ArrayList();

        for (String productId : productIds) {
            List<SUserCoupon> userCouponProductList = this.userCouponService.findUserCouponList(
                    user.getId(), productId, couponStatus, couponType);
            for (SUserCoupon userCouponProduct : userCouponProductList) {
                userCouponList.add(userCouponProduct);
            }
        }

        List<Map> couponList = new ArrayList();
        for (SUserCoupon userCoupon : userCouponList) {

            Map<String, Object> couponMap = new HashMap<>();

            // 券类型 0-任务金 1-商铺券
            if (userCoupon.getCouponType() == 0) {
                STaskCoupon taskCoupon = this.taskCouponService.getById(userCoupon.getCouponId());

                // 用户券ID
                couponMap.put("userCouponId", userCoupon.getId());

                // 券类型 0-任务金
                couponMap.put("couponType", userCoupon.getCouponType());
                // 券名称
                couponMap.put("couponName", taskCoupon.getCouponName());
                // 券面值
                couponMap.put("couponAmount", taskCoupon.getCouponAmount());
                // 券开始日期
                couponMap.put("startDate", taskCoupon.getStartDate());
                // 券截止日期
                couponMap.put("endDate", taskCoupon.getEndDate());
                // 使用条件 1-固定金额券 2-超级抵扣券 3-购买任务返任务金优惠券
                couponMap.put("useCon", taskCoupon.getUseCon());

                couponList.add(couponMap);

            } else {

                SShopCoupon shopCoupon = this.shopCouponService.getById(userCoupon.getCouponId());

                // 用户券ID
                couponMap.put("userCouponId", userCoupon.getId());

                // 券类型 1-商铺券
                couponMap.put("couponType", userCoupon.getCouponType());
                // 券名称
                couponMap.put("couponName", shopCoupon.getCouponName());
                // 券面值
                couponMap.put("couponAmount", shopCoupon.getCouponAmount());
                // 券开始日期
                couponMap.put("startDate", shopCoupon.getStartDate());
                // 券截止日期
                couponMap.put("endDate", shopCoupon.getEndDate());
                // 使用条件 0-立减 1-满减
                couponMap.put("useCon", shopCoupon.getUseCon());
                // 满减最低消费金额
                couponMap.put("minConsumeAmount", shopCoupon.getMinConsumeAmount());

                couponList.add(couponMap);
            }
        }

        response.put("code", 0);
        response.data(couponList);

        return response;
    }

}
