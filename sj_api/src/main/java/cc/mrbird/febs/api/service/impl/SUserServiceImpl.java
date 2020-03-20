package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUser;
import cc.mrbird.febs.api.mapper.SUserMapper;
import cc.mrbird.febs.api.service.ISUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @author MrBird
 */
@Service
public class SUserServiceImpl extends ServiceImpl<SUserMapper, SUser> implements ISUserService {

	@Override
	public SUser findByOpenId(String openId) {

		return baseMapper.selectOne(new LambdaQueryWrapper<SUser>().eq(SUser::getOpenId, openId));
	}

	@Override
	public void createUser(SUser user) throws Exception {

		// 创建用户
		user.setCreateTime(new Date());

		save(user);
	}

	@Override
	public List<SUser> findByParentId(List parentIds) {

		LambdaQueryWrapper<SUser> queryWrapper = new LambdaQueryWrapper<SUser>();

		// 上级用户ID
		if(parentIds!=null&&parentIds.size()>0)
		queryWrapper.in(SUser::getParentId, parentIds);

		return this.baseMapper.selectList(queryWrapper);
	}
}
