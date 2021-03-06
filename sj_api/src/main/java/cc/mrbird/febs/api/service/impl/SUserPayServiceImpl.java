package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserPay;
import cc.mrbird.febs.api.mapper.SUserPayMapper;
import cc.mrbird.febs.api.service.ISUserPayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserPayServiceImpl extends ServiceImpl<SUserPayMapper, SUserPay> implements ISUserPayService {

    @Override
    public void updateTaskLineForPay(String orderId) {
        this.baseMapper.updateTaskLineForPay(orderId);
    }

    @Override
    public void updateUserTaskLineForPay(String orderId) {
        this.baseMapper.updateUserTaskLineForPay(orderId);
    }
}
