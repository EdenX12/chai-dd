package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUser;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @author MrBird
 */
public interface SUserMapper extends BaseMapper<SUser> {
    void updateForUserLevel();

}
