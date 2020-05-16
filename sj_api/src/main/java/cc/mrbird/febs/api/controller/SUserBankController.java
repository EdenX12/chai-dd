package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SBank;
import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserAddress;
import cc.mrbird.febs.api.entity.SUserBank;
import cc.mrbird.febs.api.service.ISBankService;
import cc.mrbird.febs.api.service.ISUserBankService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
     *   银行卡列表
     */
    @GetMapping("/getBankList")
    @Limit(key = "getBankList", period = 60, count = 20, name = "银行卡列表接口", prefix = "limit")
    public FebsResponse getBankList() {

        FebsResponse response = new FebsResponse();
        List<SBank> bankList = bankService.list();
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
        sUserBank.setStatus("1");
        userBankService.save(sUserBank);
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
        if(userBankId == null){
            response.put("code",1);
            response.message("id不可为空");
            return response;
        }
        SUserBank userBank = userBankService.getById(userBankId);
        if(userBank == null){
            response.put("code",1);
            response.message("该银行卡不存在");
            return response;
        }
        userBank.setStatus("0");
        userBankService.updateById(userBank);
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
        List<SUserBank> userBanks = userBankService.findUserBankList(user.getId());
        response.put("code", 0);
        response.data(userBanks);
        return response;
    }

}
