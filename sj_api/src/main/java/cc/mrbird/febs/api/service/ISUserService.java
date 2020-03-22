package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author MrBird
 */
public interface ISUserService extends IService<SUser> {

	/**
	 * 通过OPENID查找用户
	 *
	 * @param openId openId
	 * @return SUser
	 */
	SUser findByOpenId(String openId);

	/**
	 * 新增用户
	 *
	 * @param user user
	 */
	SUser createUser(SUser user) throws Exception;

	/**
	 * 根据上级ID查询下级所有用户
	 *
	 * @param parentIds List
	 */
	List<SUser> findByParentId(List parentIds);
}
