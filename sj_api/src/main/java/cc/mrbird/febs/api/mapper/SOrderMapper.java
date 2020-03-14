package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * @author MrBird
 */
public interface SOrderMapper extends BaseMapper<SOrder> {

    IPage<SOrder> findOrderDetail(Page page, @Param("sOrder") SOrder sOrder);

    SOrder findOrderDetail(@Param("sOrder") SOrder sOrder);
}
