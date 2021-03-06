package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.STaskOrder;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * @author MrBird
 */
public interface ISTaskOrderService extends IService<STaskOrder> {

    /**
     * 新增任务转让
     *
     * @param taskOrder STaskOrder
     */
    STaskOrder createTaskOrder(STaskOrder taskOrder);

    /**
     * 查询转让任务
     *
     * @param taskOrder STaskOrder
     * @return STaskOrder
     */
    STaskOrder findTaskOrder(STaskOrder taskOrder);

    /**
     * 查询转让任务列表
     *
     * @param taskOrder STaskOrder
     * @return List
     */
    List<STaskOrder> findTaskOrderList(STaskOrder taskOrder);

    /**
     * 查询任务详情列表，包括关注数量
     *
     * @param taskOrder STaskOrder
     * @param queryRequest queryRequest
     * @return IPage
     */
    IPage<Map> findTaskOrderList(STaskOrder taskOrder, QueryRequest queryRequest);

    /**
     * 查询任务详情，包括关注数量
     *
     * @param taskOrder STaskOrder
     * @return STaskOrder
     */
    Map findTaskOrderDetail(STaskOrder taskOrder);
}
