package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SOrderMapper extends BaseMapper<SOrder> {

    /**
     * 查询我的订单列表
     * @param page
     * @param status
     * @param userId
     * @return
     */
    IPage <Map> queryPage(Page page, @Param("status")String status, @Param("userId")String userId);

    /**
     * 根据商品订单表id查询商品
     * @param orderDetailId
     * @return
     */
    List<Map> queryProductDetailId(@Param("orderDetailId")String orderDetailId);

    /**
     * 根据订单id查商品详情
     * @param orderDetailId
     */
    Map<String, Object> queryOrderDetail(@Param("orderDetailId")String orderDetailId);

    /**
     * 支付成功时间超过5分钟的订单处理
     * @return
     */
    List<SOrder> queryOrderPaySuccessList();

    /**
     * 剩余自动收货和取消订单得时间
     * @param orderDetailId
     * @return
     */
    Map<String, Object> getReaminTime(String orderDetailId);
}
