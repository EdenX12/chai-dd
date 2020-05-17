package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SBank;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserBank;
import cc.mrbird.febs.api.service.ISBankService;
import cc.mrbird.febs.api.service.ISUserBankService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pq
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-bank")
public class SUserBankController {

    @Autowired
    private ISBankService bankService;

    @Autowired
    private ISUserBankService userBankService;

    /**
     *   银行列表
     */
    @GetMapping("/getBankList")
    @Limit(key = "getBankList", period = 60, count = 20, name = "银行卡列表接口", prefix = "limit")
    public FebsResponse getBankList() {

        FebsResponse response = new FebsResponse();
        List<SBank> bankList = this.bankService.list();
        response.put("code", 0);
        response.data(bankList);

        return response;
    }

    /**
     *  绑定银行卡
     */
    @PostMapping("/bindBankCard")
    @Limit(key = "bindBankCard", period = 60, count = 20, name = "绑定银行卡接口", prefix = "limit")
    public FebsResponse bindBankCard(@RequestBody SUserBank sUserBank) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();
        sUserBank.setUserId(user.getId());

        sUserBank.setStatus(0);
        this.userBankService.save(sUserBank);

        response.put("code", 0);
        return response;
    }

    /**
     *  解绑银行卡
     */
    @PostMapping("/unboundBankCard")
    @Limit(key = "unboundBankCard", period = 60, count = 20, name = "检索解绑银行卡接口", prefix = "limit")
    public FebsResponse unboundBankCard(Integer userBankId) {

        FebsResponse response = new FebsResponse();

        SUser user = FebsUtil.getCurrentUser();

        if (userBankId == null) {
            response.put("code",1);
            response.message("id不可为空");
            return response;
        }

        SUserBank userBank = this.userBankService.getById(userBankId);

        if (userBank == null) {
            response.put("code",1);
            response.message("该银行卡不存在");
            return response;
        }

        if (!userBank.getUserId().equals(user.getId())) {
            response.put("code", 1);
            response.message("此用户不允许解绑");
            return response;
        }

        userBank.setStatus(1);
        this.userBankService.updateById(userBank);

        response.put("code", 0);
        return response;
    }

    /**
     * 我的银行卡列表
     */
    @PostMapping("/myBankCarList")
    @Limit(key = "myBankCarList", period = 60, count = 20, name = "检索我的银行卡列表接口", prefix = "limit")
    public FebsResponse myBankCarList() {

        FebsResponse response = new FebsResponse();
        SUser user = FebsUtil.getCurrentUser();

        Map<String, Object> resultMap = new HashMap<>();
        List<Map> resultList = new ArrayList();

        List<SUserBank> userBanksList = this.userBankService.findUserBankList(user.getId());

        for (SUserBank userBank : userBanksList) {

            resultMap.put("id", userBank.getId());
            resultMap.put("bankId", userBank.getBankId());

            String cardNo = userBank.getCardNum().substring(userBank.getCardNum().length() - 4);
            cardNo = "**** **** **** " + cardNo;
            resultMap.put("cardNum", cardNo);

            resultMap.put("realName", userBank.getRealName());

            SBank bank = this.bankService.getById(userBank.getBankId());

            resultMap.put("bankName", bank.getBankName());
            resultMap.put("bankIcon", bank.getBankIcon());

            resultList.add(resultMap);
        }

        response.put("code", 0);
        response.data(resultList);
        return response;
    }

}
