package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserBrowser;
import cc.mrbird.febs.api.mapper.SUserBrowserMapper;
import cc.mrbird.febs.api.service.ISUserBrowserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserBrowserServiceImpl extends ServiceImpl<SUserBrowserMapper, SUserBrowser> implements ISUserBrowserService {

    @Override
    public SUserBrowser findUserBrowser(SUserBrowser userBrowser) {

        LambdaQueryWrapper<SUserBrowser> queryWrapper = new LambdaQueryWrapper<SUserBrowser>();

        // 用户ID
        queryWrapper.eq(SUserBrowser::getUserId, userBrowser.getUserId());

        // 分享ID
        if (userBrowser.getShareId() != null) {
            queryWrapper.eq(SUserBrowser::getShareId, userBrowser.getShareId());
        }

        // 商品ID
        if (userBrowser.getProductId() != null) {
            queryWrapper.eq(SUserBrowser::getProductId, userBrowser.getProductId());
        }

        // 微信unionid
        if (userBrowser.getUnionId() != null) {
            queryWrapper.eq(SUserBrowser::getUnionId, userBrowser.getUnionId());
        }

        queryWrapper.orderByDesc(SUserBrowser::getCreateTime);

        return this.baseMapper.selectOne(queryWrapper);
    }

}
