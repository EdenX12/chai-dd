package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * @author MrBird
 */
public interface SOrderMapper extends BaseMapper<SOrder> {

    IPage<SOrder> findOrderDetail(Page page, @Param("sOrder") SOrder sOrder);

    SOrder findOrderDetail(@Param("sOrder") SOrder sOrder);

    /**
     * 查询我的订单列表
     * @param page
     * @param status
     * @param userId
     * @return
     */
    IPage <Map> queryPage(Page page, @Param("status")String status, @Param("userId")String userId);
}
