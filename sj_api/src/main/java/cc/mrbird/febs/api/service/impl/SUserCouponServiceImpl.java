package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserCoupon;
import cc.mrbird.febs.api.mapper.SUserCouponMapper;
import cc.mrbird.febs.api.service.ISUserCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserCouponServiceImpl extends ServiceImpl<SUserCouponMapper, SUserCoupon> implements ISUserCouponService {

    @Override
    public List<SUserCoupon> findUserCouponList(String userId, String productId, Integer couponStatus, Integer couponType) {

        LambdaQueryWrapper<SUserCoupon> queryWrapper = new LambdaQueryWrapper();

        // 用户ID
        queryWrapper.eq(SUserCoupon::getUserId, userId);

        // 商品ID （或者ISNULL 针对全部商品的优惠券）
        if (productId != null) {
            queryWrapper.and(wrapper -> wrapper.eq(SUserCoupon::getProductId, productId).or().isNull(SUserCoupon::getProductId));
        }

        // 券类型 0-任务金 1-商铺券
        if (couponType != null) {
            queryWrapper.eq(SUserCoupon::getCouponType, couponType);
        }

        // 0-未使用 ；1-已使用；2-过期
        queryWrapper.eq(SUserCoupon::getCouponStatus, couponStatus);

        // 顺序
        queryWrapper.orderByAsc(SUserCoupon::getCreateTime);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public SUserCoupon findUserCoupon(String userId, String userCouponId) {

        LambdaQueryWrapper<SUserCoupon> queryWrapper = new LambdaQueryWrapper();

        // 用户优惠券ID
        queryWrapper.eq(SUserCoupon::getId, userCouponId);

        // 用户ID
        queryWrapper.eq(SUserCoupon::getUserId, userId);

        // 0-未使用 ；1-已使用；2-过期
        queryWrapper.eq(SUserCoupon::getCouponStatus, 0);

        return this.baseMapper.selectOne(queryWrapper);
    }


}
