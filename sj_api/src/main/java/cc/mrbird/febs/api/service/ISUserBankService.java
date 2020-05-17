package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUserBank;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ISUserBankService extends IService<SUserBank> {

    List<SUserBank> findUserBankList(String userId);
}
