package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.api.service.ISUserAddressService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-address")
public class SUserAddressController extends BaseController {

    private String message;

    @Autowired
    private ISUserAddressService userAddressService;

    /**
     * 取得用户收货地址列表信息
     * @return List<SUserAddress>
     */
    @PostMapping("/getUserAddressList")
    @Limit(key = "getUserAddressList", period = 60, count = 2000, name = "检索用户收货地址接口", prefix = "limit")
    public FebsResponse getUserAddressList(QueryRequest queryRequest, SUserAddress userAddress) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        userAddress.setUserId(user.getId());

        Map<String, Object> userAddressPageList = getDataTable(userAddressService.findUserAddressList(queryRequest, userAddress));

        response.put("code", 0);
        response.data(userAddressPageList);

        return response;
    }

    /**
     * 新增用户收货地址
     */
    @Log("新增用户收货地址")
    @PostMapping("/addUserAddress")
    @Limit(key = "addUserAddress", period = 60, count = 2000, name = "新增用户收货地址接口", prefix = "limit")
    public FebsResponse addUserAddress(@Valid SUserAddress userAddress) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userAddress.setUserId(user.getId());

            this.userAddressService.addUserAddress(userAddress);
        } catch (Exception e) {
            message = "新增用户收货地址失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage(),e);
        }

        return response;
    }

    /**
     * 删除用户收货地址
     */
    @Log("删除用户收货地址")
    @PostMapping("/deleteUserAddress")
    public FebsResponse deleteUserAddress(@Valid SUserAddress userAddress) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userAddress.setUserId(user.getId());

            this.userAddressService.deleteUserAddress(userAddress);
        } catch (Exception e) {
            message = "删除用户收货地址失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage(),e);
        }

        return response;
    }

    /**
     * 修改用户收货地址
     */
    @Log("修改用户收货地址")
    @PostMapping("/updateUserAddress")
    public FebsResponse updateUserAddress(@Valid SUserAddress userAddress) throws FebsException {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userAddress.setUserId(user.getId());

            this.userAddressService.updateUserAddress(userAddress);
        } catch (Exception e) {
            message = "修改用户收货地址失败";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage(),e);
        }

        return response;
    }

    /**
     * 查询用户收货地址详情信息
     * @return SUserAddress
     */
    @PostMapping("/getUserAddress")
    @Limit(key = "getUserAddress", period = 60, count = 2000, name = "检索用户收货地址详情接口", prefix = "limit")
    public FebsResponse getUserAddress(SUserAddress userAddress) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        userAddress.setUserId(user.getId());

        SUserAddress userAddressDetail = userAddressService.findUserAddress(userAddress);

        response.put("code", 0);
        response.data(userAddressDetail);

        return response;
    }
}
