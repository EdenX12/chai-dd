package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserWithdraw;
import cc.mrbird.febs.api.mapper.SUserWithdrawMapper;
import cc.mrbird.febs.api.service.ISUserWithdrawService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * @author MrBird
 */
@Service
public class SUserWithdrawServiceImpl extends ServiceImpl<SUserWithdrawMapper, SUserWithdraw> implements ISUserWithdrawService {
    @Override
    public IPage<SUserWithdraw> FindForPage(QueryRequest request,String userId) {
        LambdaQueryWrapper<SUserWithdraw> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.eq(SUserWithdraw::getUserId, userId);
        Page<SUserWithdraw> page = new Page<>();
        SortUtil.handlePageSort(request, page, "dealTime", FebsConstant.ORDER_DESC, false);
        return this.baseMapper.selectPage(page, queryWrapper);
    }

}
