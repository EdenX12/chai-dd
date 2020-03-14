package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserAddressService extends IService<SUserAddress> {

    /**
     * 查询用户收货地址列表
     *
     * @param userAddress SUserAddress
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<SUserAddress> findUserAddressList(QueryRequest queryRequest, SUserAddress userAddress);

    /**
     * 删除用户收货地址
     *
     * @param userAddress SUserAddress
     */
    void deleteUserAddress(SUserAddress userAddress);

    /**
     * 增加用户收货地址
     *
     * @param userAddress SUserAddress
     */
    void addUserAddress(SUserAddress userAddress);

    /**
     * 修改用户收货地址
     *
     * @param userAddress SUserAddress
     */
    void updateUserAddress(SUserAddress userAddress);

    /**
     * 查询用户收货地址
     *
     * @param userAddress SUserAddress
     */
    SUserAddress findUserAddress(SUserAddress userAddress);
}
