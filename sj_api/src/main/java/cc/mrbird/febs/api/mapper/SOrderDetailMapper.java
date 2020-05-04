package cc.mrbird.febs.api.mapper;

import cc.mrbird.febs.api.entity.SOrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface SOrderDetailMapper extends BaseMapper<SOrderDetail> {
    List<Map<String,Object>> queryProductByOrder(@Param("orderId")String orderId);

}
