package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SFuncModule;
import cc.mrbird.febs.api.entity.SUserBank;
import cc.mrbird.febs.api.mapper.SUserBankMapper;
import cc.mrbird.febs.api.service.ISUserBankService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SUserBankServiceImpl extends ServiceImpl<SUserBankMapper, SUserBank> implements ISUserBankService {

    @Override
    public List<SUserBank> findUserBankList(String userId) {

        LambdaQueryWrapper<SUserBank> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SUserBank::getUserId, userId);
        queryWrapper.eq(SUserBank::getStatus, 0);

        return this.baseMapper.selectList(queryWrapper);
    }
}
