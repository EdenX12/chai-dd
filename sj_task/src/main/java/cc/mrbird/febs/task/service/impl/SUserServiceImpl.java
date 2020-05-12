package cc.mrbird.febs.task.service.impl;

import cc.mrbird.febs.task.entity.SUser;
import cc.mrbird.febs.task.mapper.SUserMapper;
import cc.mrbird.febs.task.service.ISUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @author MrBird
 */
@Service
public class SUserServiceImpl extends ServiceImpl<SUserMapper, SUser> implements ISUserService {

	@Override
	public void updateForUserLevel() {
		this.baseMapper.updateForUserLevel();
	}
}
