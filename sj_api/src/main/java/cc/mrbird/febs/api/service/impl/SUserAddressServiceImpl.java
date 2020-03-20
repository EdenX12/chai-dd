package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.api.mapper.SUserAddressMapper;
import cc.mrbird.febs.api.service.ISUserAddressService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author MrBird
 */
@Service
public class SUserAddressServiceImpl extends ServiceImpl<SUserAddressMapper, SUserAddress> implements ISUserAddressService {

    @Override
    public IPage<SUserAddress> findUserAddressList(QueryRequest request, SUserAddress userAddress) {
        try {
            Page<SUserAddress> page = new Page<>();

            LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper();

            // 用户ID
            queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

            SortUtil.handlePageSort(request, page, "update_time", FebsConstant.ORDER_DESC, false);

            return this.baseMapper.selectPage(page, queryWrapper);
        } catch (Exception e) {
            log.error("查询用户收货地址异常", e);
            return null;
        }
    }

    @Override
    @Transactional
    public void updateUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        queryWrapper.eq(SUserAddress::getId, userAddress.getId());

        userAddress.setUpdateTime(new Date());

        this.baseMapper.update(userAddress, queryWrapper);
    }

    @Override
    @Transactional
    public void deleteUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        queryWrapper.eq(SUserAddress::getId, userAddress.getId());

        this.baseMapper.delete(queryWrapper);
    }

    @Override
    @Transactional
    public void addUserAddress(SUserAddress userAddress) {

        userAddress.setCreateTime(new Date());

        this.baseMapper.insert(userAddress);
    }

    @Override
    public SUserAddress findUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        if(userAddress.getId()!=null) {
        queryWrapper.eq(SUserAddress::getId, userAddress.getId());
        }else {
        	//如果是空 说明是查默认地址
        	queryWrapper.eq(SUserAddress::getIsDefault, 1);
        }

        return this.baseMapper.selectOne(queryWrapper);
    }
}
