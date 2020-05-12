package cc.mrbird.febs.task.controller;

import cc.mrbird.febs.task.service.ISUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author pq
 *
 */
@Component
public class SUserJobTask {

    @Autowired
    private ISUserService userService;
    /**
     * user 等级更新定时任务
     */
    @Scheduled(cron="0 0 7,14,20,22 * * ?")
    @Transactional
    public void updateUserLevelType(){

       // 1.  根据SUser表中的 reward_bean 值  看这个值在 s_user_lever中的 min_number 和 max_number 两个值之间 找到对应的level_type
        //2.  把上面找到的这个level_type 设定到 s_user表中的 user_level_type 字段上

        this.userService.updateForUserLevel();
    }
}
