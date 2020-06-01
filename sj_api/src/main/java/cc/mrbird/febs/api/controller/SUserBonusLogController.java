package cc.mrbird.febs.api.controller;


import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISUserBonusLogService;
import cc.mrbird.febs.common.annotation.Limit;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.domain.FebsResponse;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.FebsUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/api/s-user-bonus")
public class SUserBonusLogController extends BaseController {
    @Autowired
    ISUserBonusLogService userBonusLogService;

    @GetMapping("/getBonusDetails")
    @Limit(key = "getBonusDetails", period = 60, count = 2000, name = "查询余额明细接口", prefix = "limit")
    public FebsResponse  getBonusDetails(QueryRequest request){
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        SUser user = FebsUtil.getCurrentUser();
        IPage<Map> page =  userBonusLogService.getBonusDetails(request,user.getId());
        response.data( getDataTable(page));
        return response;

    }
}
