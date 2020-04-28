package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskCoupon;
import cc.mrbird.febs.api.mapper.STaskCouponMapper;
import cc.mrbird.febs.api.service.ISTaskCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class STaskCouponServiceImpl extends ServiceImpl<STaskCouponMapper, STaskCoupon> implements ISTaskCouponService {


    @Override
    public STaskCoupon findReturnTaskCoupon() {

        LambdaQueryWrapper<STaskCoupon> queryWrapper = new LambdaQueryWrapper();

        // 使用条件 1-固定金额券 2-超级抵扣券 3-购买任务返任务金优惠券
        queryWrapper.eq(STaskCoupon::getUseCon, 3);

        // 状态 0-创建 1-发布 2-下架 3-删除
        queryWrapper.eq(STaskCoupon::getCouponStatus, 1);

        // 顺序
        queryWrapper.orderByAsc(STaskCoupon::getCreateTime);

        return this.baseMapper.selectOne(queryWrapper);
    }
}
