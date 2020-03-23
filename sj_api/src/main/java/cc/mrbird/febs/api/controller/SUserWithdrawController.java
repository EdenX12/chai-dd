package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserWithdraw;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.api.service.ISUserWithdrawService;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-withdraw")
public class SUserWithdrawController extends BaseController {

    private String message;

    @Autowired
    private ISUserWithdrawService userWithdrawService;

    @Autowired
    private ISUserService userService;

    /**
     * 新增用户提现
     */
    @Log("新增用户提现")
    @PostMapping("/addUserWithdraw")
    public FebsResponse addUserWithdraw(@Valid SUserWithdraw userWithdraw) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();
            userWithdraw.setUserId(user.getId());

            // 最低提现金额10元
            if (userWithdraw.getAmount().compareTo(BigDecimal.valueOf(10)) < 0) {
                message = "提现金额最低10元起！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            // 用户提现金额大于余额
            if (userWithdraw.getAmount().compareTo(user.getTotalAmount()) > 0) {

                message = "提现金额不能超过余额！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            userWithdraw.setCreateTime(new Date());
            userWithdraw.setStatus(0);

            this.userWithdrawService.save(userWithdraw);

            // 用户冻结金额追加
            user.setLockAmount(user.getLockAmount().add(userWithdraw.getAmount()));
            user.setTotalAmount(user.getTotalAmount().subtract(userWithdraw.getAmount()));
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "新增用户提现";
            response.put("code", 1);
            response.message(message);
            log.error(message, e);
        }

        return response;
    }

}
