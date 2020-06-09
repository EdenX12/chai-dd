package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserAmountLog;
import cc.mrbird.febs.api.mapper.SUserAmountLogMapper;
import cc.mrbird.febs.api.service.ISUserAmountLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserAmountLogServiceImpl extends ServiceImpl<SUserAmountLogMapper, SUserAmountLog> implements ISUserAmountLogService {

    @Override
    public void batchInsertLog() {
        this.baseMapper.batchInsertLog();
    }

    @Override
    public void batchUpdateBalance() {
        this.baseMapper.batchUpdateBalance();
    }

    @Override
    public void batchUpdateStatus() {
        this.baseMapper.batchUpdateStatus();
    }
}
