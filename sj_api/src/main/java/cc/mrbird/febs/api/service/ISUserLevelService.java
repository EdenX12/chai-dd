package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserLevel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserLevelService extends IService<SUserLevel> {

    /**
     * 根据用户等级查询
     * @param userLevelType Integer
     * @return SUserLevel
     */
    SUserLevel findByLevelType(Integer userLevelType);
}
