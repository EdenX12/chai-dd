package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserWithdraw;
import cc.mrbird.febs.api.mapper.SUserWechatMapper;
import cc.mrbird.febs.api.mapper.SUserWithdrawMapper;
import cc.mrbird.febs.api.service.ISUserWithdrawService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserWithdrawServiceImpl extends ServiceImpl<SUserWithdrawMapper, SUserWithdraw> implements ISUserWithdrawService {

    @Autowired
    private SUserWechatMapper sUserWechatMapper;

    @Override
    public IPage<SUserWithdraw> FindForPage(QueryRequest request, String userId) {

        LambdaQueryWrapper<SUserWithdraw> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SUserWithdraw::getUserId, userId);
        Page<SUserWithdraw> page = new Page<>();
        SortUtil.handlePageSort(request, page, "deal_time", FebsConstant.ORDER_DESC, false);

        return this.baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public BigDecimal getWithdrawingAmount(String userId) {
        return sUserWechatMapper.getWithdrawingAmount(userId);
    }

    @Override
    public BigDecimal getWithdrawSuccessAmount(String userId) {
        return sUserWechatMapper.getWithdrawSuccessAmount(userId);
    }

}
