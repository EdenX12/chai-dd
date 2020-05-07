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
import java.util.List;
import java.util.UUID;

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
    public SUserAddress updateUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        queryWrapper.eq(SUserAddress::getId, userAddress.getId());

        userAddress.setUpdateTime(new Date());

        // 如果修改为默认的话，以前默认去掉
        if ("1".equals(userAddress.getIsDefault())) {

            LambdaQueryWrapper<SUserAddress> queryDefaultWrapper = new LambdaQueryWrapper<SUserAddress>();

            // 用户ID
            queryDefaultWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

            // 默认
            queryDefaultWrapper.eq(SUserAddress::getIsDefault, "1");

            SUserAddress userAddressDefault = this.baseMapper.selectOne(queryDefaultWrapper);

            if (userAddressDefault != null) {
                userAddressDefault.setIsDefault("0");
                this.baseMapper.updateById(userAddressDefault);
            }
        }

        this.baseMapper.update(userAddress, queryWrapper);

        return userAddress;
    }

    @Override
    @Transactional
    public void deleteUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        queryWrapper.eq(SUserAddress::getId, userAddress.getId());

        SUserAddress userAddressOne = this.baseMapper.selectOne(queryWrapper);
        String isDefault = userAddressOne.getIsDefault();

        this.baseMapper.delete(queryWrapper);

        // 如果删除是默认数据，如存在数据 再增加一条默认数据
        if ("1".equals(isDefault)) {

            LambdaQueryWrapper<SUserAddress> queryWrapperDefault = new LambdaQueryWrapper<SUserAddress>();

            // 用户ID
            queryWrapperDefault.eq(SUserAddress::getUserId, userAddress.getUserId());

            queryWrapperDefault.orderByDesc(SUserAddress::getUpdateTime);

            List<SUserAddress> list = this.baseMapper.selectList(queryWrapperDefault);
            if (list != null && list.size() > 0) {
                SUserAddress userAddressDefault = list.get(0);
                userAddressDefault.setIsDefault("1");
                this.baseMapper.updateById(userAddressDefault);
            }
        }

    }

    @Override
    @Transactional
    public SUserAddress addUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 只有一条数据的话 初始设为默认
        List<SUserAddress> userAddressList = this.baseMapper.selectList(queryWrapper);
        if (userAddressList == null || userAddressList.size() == 0) {
            userAddress.setIsDefault("1");
        }

        userAddress.setCreateTime(new Date());
        userAddress.setUpdateTime(new Date());
        this.baseMapper.insert(userAddress);
        return userAddress;
    }

    @Override
    public SUserAddress findUserAddress(SUserAddress userAddress) {

        LambdaQueryWrapper<SUserAddress> queryWrapper = new LambdaQueryWrapper<SUserAddress>();

        // 用户ID
        queryWrapper.eq(SUserAddress::getUserId, userAddress.getUserId());

        // 用户地址ID
        if (userAddress.getId() != null) {
            queryWrapper.eq(SUserAddress::getId, userAddress.getId());
        }

        queryWrapper.orderByDesc(SUserAddress::getIsDefault);

        List<SUserAddress> list = this.baseMapper.selectList(queryWrapper);
        if (list != null && list.size() > 0) {
        	return list.get(0);
        } else {
        	return null;
        }
    }
}
