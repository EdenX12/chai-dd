package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserBrowser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserBrowserService extends IService<SUserBrowser> {

    SUserBrowser createUserBrowser(SUserBrowser userBrowser);

    SUserBrowser findUserBrowser(SUserBrowser userBrowser);
}
