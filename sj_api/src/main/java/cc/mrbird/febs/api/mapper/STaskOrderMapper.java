package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.STaskOrder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author MrBird
 */
public interface STaskOrderMapper extends BaseMapper<STaskOrder> {

    IPage<Map> findTaskOrderDetail(Page page, @Param("sTaskOrder") STaskOrder sTaskOrder);

    Map findTaskOrderDetail(@Param("sTaskOrder") STaskOrder sTaskOrder);

}
