package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.service.ISUserRelationService;
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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;


/**
 * @author pq
 */
@Slf4j
@RestController
@RequestMapping("/api/user-relation")
public class SUserRelationController  extends BaseController {

    @Autowired
    ISUserRelationService userRelationService;

    /**
     * 查询我的预备队和禁卫军列表
     * @param queryRequest
     * @param relationType 团队类型 禁卫军 1 预备队 0
     * @param level  级别  一级 二级 三级
     *  @param flag    flag  =1  列表带分佣金额  flag= 0 不带分佣金额
     * @return
     */
    @GetMapping("/getMyTeamList")
    @Limit(key = "getMyTeamList", period = 60, count = 2000, name = " 查询我的预备队和禁卫军列表", prefix = "limit")
    public FebsResponse getMyTeamList(QueryRequest queryRequest, @NotEmpty(message = "团队类型") String relationType,@NotNull(message = "级别不可为空") Integer level,@NotNull(message = "标记不可为空")Integer flag) {
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        SUser user = FebsUtil.getCurrentUser();
        String userId = user.getId();
        IPage<Map> page = null;

        try{
            if(flag == 1){
                switch (level){
                    case 1 : page = userRelationService.getFirstLeveForAmt(queryRequest,userId,relationType);break;
                    case 2 : page = userRelationService.getSecondLevelForAmt(queryRequest,userId,relationType);break;
                    case 3 : page = userRelationService.getThirdLevelForAmt(queryRequest,userId,relationType);break;
                }
            }else{
                switch (level){
                    case 1 : page = userRelationService.getFirstLevel(queryRequest,userId,relationType);break;
                    case 2 : page = userRelationService.getSecondLevel(queryRequest,userId,relationType);break;
                    case 3 : page = userRelationService.getThirdLevel(queryRequest,userId,relationType);break;
                }
            }

            response.data( getDataTable(page));
            return  response;

        }catch (Exception e){
            log.error(e.getMessage(),e);
            response.message("查询失败");
            response.put("code", 1);
            return  response;
        }

    }

    /**
     * 查询我的预备队和禁卫军总数
     * @return
     */
    @GetMapping("/getMyTeamTotal")
    @Limit(key = "getMyTeamTotal", period = 60, count = 2000, name = " 查询我的预备队和禁卫军总数", prefix = "limit")
    public FebsResponse getMyTeamList() {
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        SUser user = FebsUtil.getCurrentUser();
        String userId = user.getId();
        Map<String,Object> resultData = new HashMap<>();
        try{
            //禁卫军 1 预备队 0
            Map<String,Object> result0 =  userRelationService.getMyTeamTotal(userId,"0");
            Map<String,Object> result1 =  userRelationService.getMyTeamTotal(userId,"1");
            resultData.put("result0",result0);
            resultData.put("result1",result1);
            response.data(resultData);
            return  response;

        }catch (Exception e){
            log.error(e.getMessage(),e);
            response.message("查询失败");
            response.put("code", 1);
            return  response;
        }

    }

    /**
     * 查询我的战队今天新增
     * @param queryRequest
     * @param flag    flag  =1  列表带分佣金额  flag= 0 不带分佣金额
     * @return
     */
    @GetMapping("/getTodayNewAdd")
    @Limit(key = "getTodayNewAdd", period = 60, count = 2000, name = " 查询我的预备队和禁卫军今日新增列表", prefix = "limit")
    public FebsResponse getTodayNewAdd(QueryRequest queryRequest,Integer flag) {
        FebsResponse response = new FebsResponse();
        response.put("code", 0);
        SUser user = FebsUtil.getCurrentUser();
        String userId = user.getId();
        IPage<Map> page = null;
        try{
            if(flag == 1){
                page = userRelationService.getTodayNewAddForAmt(queryRequest,userId);
            }else{
                page = userRelationService.getTodayNewAdd(queryRequest,userId);
            }
            response.data( getDataTable(page));
            return  response;

        }catch (Exception e){
            log.error(e.getMessage(),e);
            response.message("查询失败");
            response.put("code", 1);
            return  response;
        }

    }
}
