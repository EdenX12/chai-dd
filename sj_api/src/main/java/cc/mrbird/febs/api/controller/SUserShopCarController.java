package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserShopCar;
import cc.mrbird.febs.api.service.ISUserShopCarService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-shop-car")
public class SUserShopCarController extends BaseController {

    private String message;

    @Autowired
    private ISUserShopCarService userShopCarService;

    /**
     * 商品加入购物车
     * 用户ID 任务ID 产品ID
     */
    @Log("商品加入购物车")
    @Transactional
    @PostMapping("/addShopCar")
    public FebsResponse addFollow(@Valid SUserShopCar userShopCar) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userShopCar.setUserId(user.getId());

            this.userShopCarService.addUserShopCar(userShopCar);

        } catch (Exception e) {
            message = "新增用户关注失败";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

    /**
     * 取得我的购物车
     * @return List<SUserMsg>
     */
    @PostMapping("/getUserMsg")
    @Limit(key = "getUserMsg", period = 60, count = 20, name = "检索我的购物车接口", prefix = "limit")
    public FebsResponse getUserShopCar(QueryRequest queryRequest)  {

        FebsResponse response = new FebsResponse();

        Map<String, Object> resultMap = new HashMap<>();

        SUser user = FebsUtil.getCurrentUser();





        response.put("code", 0);
        response.data(resultMap);

        return response;
    }

}
