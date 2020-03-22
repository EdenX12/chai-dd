package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserBonusLog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserBonusLogService extends IService<SUserBonusLog> {

    /**
     * 查询用户奖励金额暂放数据
     *
     * @param userBonusLog SUserBonusLog
     * @return List
     */
    List<SUserBonusLog> findUserBonusList(SUserBonusLog userBonusLog);
}
