package cc.mrbird.febs.api.controller;


import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISUserAmountLogService;
import cc.mrbird.febs.api.service.ISUserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author pq
 *
 */
@Component
public class ScheduledTask {
    org.slf4j.Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    @Autowired
    ISUserService userService;

    @Autowired
    ISProductService productService;

    @Autowired
    ISUserAmountLogService userAmountLogService;
    /**
     * user 等级更新定时任务
     */
    @Scheduled(cron="0 0 7,14,20,22 * * ?")
    @Transactional
    public void updateUserLevelType(){
       // 1.  根据SUser表中的 reward_bean 值  看这个值在 s_user_lever中的 min_number 和 max_number 两个值之间 找到对应的level_type
        //2.  把上面找到的这个level_type 设定到 s_user表中的 user_level_type 字段上

        userService.updateForUserLevel();
    }

    /**
     * 定时任务更新商品上已拆人数和已满人数
     */
    @Scheduled(cron="0 0 0 * * ?")
    //@Scheduled(cron="0 * * * * ?")
    @Transactional
    public void updateProductCountTask(){

        List<Map<String,Object>> overList = productService.getOverCount();
        if(overList != null && overList.size() > 0){
            productService.updateForOverBatch(overList);
            logger.info("定时任务更新product已满人数："+overList.size()+"条！");
        }
        List<Map<String,Object>> userCountList =  productService.getUserCountForProduct();
        if(userCountList != null && userCountList.size() > 0 ){
            this.productService.updateForUserCountBatch(userCountList);
            logger.info("定时任务更新product已拆人数："+userCountList.size()+"条！");
        }

    }

    /**
     * 根据任务线更新用户账户余额
     */
    @Scheduled(cron="0 10 0 * * ?")
    @Transactional
    public void batchUpdateUserAmtLog(){
        userAmountLogService.batchInsertLog();
        userAmountLogService.batchUpdateBalance();
        userAmountLogService.batchUpdateStatus();
        logger.info("定时任务更新用户余额、状态");
    }
}
