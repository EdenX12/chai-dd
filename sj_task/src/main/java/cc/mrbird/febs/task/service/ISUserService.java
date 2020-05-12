package cc.mrbird.febs.task.service;

import cc.mrbird.febs.task.entity.SUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author MrBird
 */
public interface ISUserService extends IService<SUser> {

	/**
	 * 批量更新userLevel
	 */
	void updateForUserLevel();
}
