package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserShopCar;
import cc.mrbird.febs.api.mapper.SUserShopCarMapper;
import cc.mrbird.febs.api.service.ISUserShopCarService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserShopCarServiceImpl extends ServiceImpl<SUserShopCarMapper, SUserShopCar> implements ISUserShopCarService {

    @Override
    public SUserShopCar addUserShopCar(SUserShopCar userShopCar) {

        this.baseMapper.insert(userShopCar);

        return userShopCar;
    }

    @Override
    public List<SUserShopCar> findUserShopCarList(SUserShopCar userShopCar) {

        LambdaQueryWrapper<SUserShopCar> queryWrapper = new LambdaQueryWrapper();

        // 用户ID
        queryWrapper.eq(SUserShopCar::getUserId, userShopCar.getUserId());

        // 顺序
        queryWrapper.orderByDesc(SUserShopCar::getCreateTime);

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public void deleteUserShopCar(SUserShopCar userShopCar) {

        LambdaQueryWrapper<SUserShopCar> queryWrapper = new LambdaQueryWrapper<SUserShopCar>();

        // 用户ID
        queryWrapper.eq(SUserShopCar::getUserId, userShopCar.getUserId());

        // 用户购物车ID
        queryWrapper.eq(SUserShopCar::getId, userShopCar.getId());

        this.baseMapper.delete(queryWrapper);
    }
}
