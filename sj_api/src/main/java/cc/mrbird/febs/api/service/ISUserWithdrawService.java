package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserWithdraw;
import cc.mrbird.febs.common.domain.QueryRequest;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserWithdrawService extends IService<SUserWithdraw> {

    IPage<SUserWithdraw> FindForPage(QueryRequest request,String userId);

}
