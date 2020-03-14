package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SUserFollow;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author MrBird
 */
public interface SUserFollowMapper extends BaseMapper<SUserFollow> {

    IPage<Map> findUserFollowDetail(Page page, @Param("sUserFollow") SUserFollow sUserFollow);
}
