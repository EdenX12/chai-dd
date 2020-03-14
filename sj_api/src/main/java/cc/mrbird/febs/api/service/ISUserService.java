package cc.mrbird.febs.api.service;

import cc.mrbird.febs.api.entity.SUser;
import com.baomidou.mybatisplus.extension.service.IService;

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
	void createUser(SUser user) throws Exception;

}
