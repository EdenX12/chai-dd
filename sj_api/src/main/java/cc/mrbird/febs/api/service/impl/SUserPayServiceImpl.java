package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.entity.SUserPay;
import cc.mrbird.febs.api.mapper.SUserPayMapper;
import cc.mrbird.febs.api.service.ISUserPayService;
import cc.mrbird.febs.common.utils.FebsUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author MrBird
 */
@Service
public class SUserPayServiceImpl extends ServiceImpl<SUserPayMapper, SUserPay> implements ISUserPayService {

}
