package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserMsg;
import cc.mrbird.febs.api.mapper.SUserMsgMapper;
import cc.mrbird.febs.api.service.ISUserMsgService;
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
public class SUserMsgServiceImpl extends ServiceImpl<SUserMsgMapper, SUserMsg> implements ISUserMsgService {

    @Override
    public IPage<SUserMsg> findUserMsgList(SUserMsg userMsg, QueryRequest request) {

        LambdaQueryWrapper<SUserMsg> queryWrapper = new LambdaQueryWrapper();

        // 用户ID不为空的情况下
        if (userMsg.getUserId() != null) {
            queryWrapper.eq(SUserMsg::getUserId, userMsg.getUserId());
        }

        // 消息类型不为空的情况下
        if (userMsg.getMsgType() != null) {
            queryWrapper.eq(SUserMsg::getMsgType, userMsg.getMsgType());
        }

        Page<SUserMsg> page = new Page<>();
        SortUtil.handlePageSort(request, page, "msg_time", FebsConstant.ORDER_DESC, false);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public int findUserMsgNotReadCount(SUserMsg userMsg) {

        LambdaQueryWrapper<SUserMsg> queryWrapper = new LambdaQueryWrapper();

        // 用户ID不为空的情况下
        queryWrapper.eq(SUserMsg::getUserId, userMsg.getUserId());

        queryWrapper.eq(SUserMsg::getStatus, 0);

        return baseMapper.selectCount(queryWrapper);
    }

}
