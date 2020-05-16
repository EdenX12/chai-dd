package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SBank;
import cc.mrbird.febs.api.mapper.SBankMapper;
import cc.mrbird.febs.api.service.ISBankService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 *
 */
@Service
public class SBankServiceImpl extends ServiceImpl<SBankMapper, SBank> implements ISBankService {
}
