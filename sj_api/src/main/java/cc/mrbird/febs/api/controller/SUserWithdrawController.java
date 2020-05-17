package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SBank;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserBank;
import cc.mrbird.febs.api.entity.SUserWithdraw;
import cc.mrbird.febs.api.service.ISBankService;
import cc.mrbird.febs.api.service.ISUserBankService;
import cc.mrbird.febs.api.service.ISUserService;
import cc.mrbird.febs.api.service.ISUserWithdrawService;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotEmpty;
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

    @Autowired
    private ISUserBankService userBankService;

    @Autowired
    private ISBankService bankService;

    /**
     * 新增用户提现
     */
    @Log("新增用户提现")
    @PostMapping("/addUserWithdraw")
    public FebsResponse addUserWithdraw(@NotEmpty(message="用户银行卡ID不可为空") String userBankId,
                                        @NotEmpty(message="金额不可为空") String amount ) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            SUserBank userBank = userBankService.getById(userBankId);
            if(userBank == null){
                message = "用户银行卡不存在！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            if(!user.getId().equals(userBank.getUserId())){
                message = "当前用户不能提现到该卡，请核对后再提交！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            BigDecimal realAmount = new BigDecimal(amount);
            // 最低提现金额10元
            if (realAmount.compareTo(BigDecimal.valueOf(10)) < 0) {
                message = "提现金额最低10元起！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            // 用户提现金额大于余额
            if (realAmount.compareTo(user.getTotalAmount()) > 0) {

                message = "提现金额不能超过余额！";
                response.put("code", 1);
                response.message(message);
                return response;
            }

            SBank bank = this.bankService.getById(userBank.getBankId());

            SUserWithdraw userWithdraw = new SUserWithdraw();
            userWithdraw.setUserId(user.getId());
            userWithdraw.setAmount(realAmount);
            userWithdraw.setBankName(bank.getBankName());
            userWithdraw.setBankCode(bank.getBankCode());
            userWithdraw.setRealName(userBank.getRealName());
            userWithdraw.setIdCard(userBank.getIdCard());
            userWithdraw.setCardNum(userBank.getCardNum());
            userWithdraw.setStatus(0);
            userWithdraw.setCreateTime(new Date());
            userWithdraw.setDealTime(new Date());
            this.userWithdrawService.save(userWithdraw);

            // 用户余额减少
            user.setTotalAmount(user.getTotalAmount().subtract(userWithdraw.getAmount()));
            this.userService.updateById(user);

        } catch (Exception e) {
            message = "新增用户提现";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

    /**
     * 用户提现记录
     */
    @Log("用户提现记录")
    @PostMapping("/myWithdraw")
    public FebsResponse myWithdraw(QueryRequest request) {

        FebsResponse response = new FebsResponse();
        response.put("code", 0);

        try {

            SUser user = FebsUtil.getCurrentUser();

            IPage<SUserWithdraw>  page = this.userWithdrawService.FindForPage(request,user.getId());
            response.data(page);
            response.put("code", 0);

        } catch (Exception e) {
            message = "新增用户提现";
            response.put("code", 1);
            response.message(message);
            log.error( e.getMessage());
        }

        return response;
    }

}
