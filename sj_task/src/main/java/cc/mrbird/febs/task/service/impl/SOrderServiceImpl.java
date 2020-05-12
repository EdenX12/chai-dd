package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.SOrder;
import cc.mrbird.febs.task.mapper.SOrderMapper;
import cc.mrbird.febs.task.service.ISOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SOrderServiceImpl extends ServiceImpl<SOrderMapper, SOrder> implements ISOrderService {

    @Override
    public List<SOrder> findOrderPaySuccessList() {

        return this.baseMapper.queryOrderPaySuccessList();
    }
}
