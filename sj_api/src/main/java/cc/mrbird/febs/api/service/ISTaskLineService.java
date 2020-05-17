package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.STaskLine;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISTaskLineService extends IService<STaskLine> {

    /**
     * 查询任务线上是否有足够任务
     * @param productId
     * @param userId 
     * @return
     */
    Integer queryTaskLineCount(String productId, String userId);

    /**
     *  查询产品的任务线未满的最小顺序
     * @param productId
     * @return
     */
    Integer queryMinLineOrder( String productId);

    /**
     * 查询排序查询任务线Id
     * @param productId
     * @return
     */
    String queryIdByLineOrder(String productId, Integer lineOrder);

    /**
     * 根据条件检索任务线
     * @param taskLine STaskLine
     * @return List<STaskLine>
     */
    List<STaskLine> findTaskLineList(STaskLine taskLine);

    /**
     * 按顺序查询待结算的任务线
     * @param productId
     * @param
     * @return
     */
    STaskLine findTaskLineForSettle(String productId);

    /**
     * 批量更新用户任务线
     * @param list
     */
    void updateUserTaskLineForSettle(List<String> list);
}
