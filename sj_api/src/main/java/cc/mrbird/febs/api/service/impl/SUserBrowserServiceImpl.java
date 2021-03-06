package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskLine;
import cc.mrbird.febs.api.entity.SUserBrowser;
import cc.mrbird.febs.api.mapper.SUserBrowserMapper;
import cc.mrbird.febs.api.service.ISUserBrowserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserBrowserServiceImpl extends ServiceImpl<SUserBrowserMapper, SUserBrowser> implements ISUserBrowserService {

    @Override
    public SUserBrowser createUserBrowser(SUserBrowser userBrowser) {

        userBrowser.setUpdateTime(new Date());
        userBrowser.setCreateTime(new Date());

        this.baseMapper.insert(userBrowser);

        return userBrowser;
    }

    @Override
    public SUserBrowser findUserBrowser(SUserBrowser userBrowser) {

        LambdaQueryWrapper<SUserBrowser> queryWrapper = new LambdaQueryWrapper<SUserBrowser>();

        // 用户ID
        if (userBrowser.getUserId() != null) {
            queryWrapper.eq(SUserBrowser::getUserId, userBrowser.getUserId());
        }

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

        List<SUserBrowser> list = this.baseMapper.selectList(queryWrapper);

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

}
