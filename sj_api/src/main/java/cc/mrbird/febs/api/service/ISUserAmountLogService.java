package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserAmountLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserAmountLogService extends IService<SUserAmountLog> {

    /**
     *插入流水
     */
    void batchInsertLog();

    /**
     *变动余额
     */
    void batchUpdateBalance();

    /**
     *改变状态
     *
     */
    void batchUpdateStatus();

}
