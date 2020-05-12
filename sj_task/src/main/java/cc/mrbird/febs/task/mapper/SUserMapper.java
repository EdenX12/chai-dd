package cc.mrbird.febs.task.mapper;

import cc.mrbird.febs.task.entity.SUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author MrBird
 */
public interface SUserMapper extends BaseMapper<SUser> {

    void updateForUserLevel();
}
