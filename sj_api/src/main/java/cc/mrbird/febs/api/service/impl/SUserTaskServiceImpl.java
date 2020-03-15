package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.api.mapper.SUserTaskMapper;
import cc.mrbird.febs.api.service.ISUserTaskService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author MrBird
 */
@Service
public class SUserTaskServiceImpl extends ServiceImpl<SUserTaskMapper, SUserTask> implements ISUserTaskService {

    @Override
    public SUserTask findUserTask(SUserTask userTask) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        // 用户任务ID
        if (userTask.getId() != null) {
            queryWrapper.eq(SUserTask::getId, userTask.getId());
        }

        // 用户ID
        if (userTask.getUserId() != null) {
            queryWrapper.eq(SUserTask::getUserId, userTask.getUserId());
        }

        // 商品ID
        if (userTask.getProductId() != null) {
            queryWrapper.eq(SUserTask::getProductId, userTask.getProductId());
        }

        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public int createUserTask(SUserTask userTask) {

        return this.baseMapper.insert(userTask);
    }

    @Override
    @Transactional
    public int updateUserTask(SUserTask userTask) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        // 任务ID
        if (userTask.getId() != null) {
            queryWrapper.eq(SUserTask::getId, userTask.getId());
        }

        // 用户ID
        if (userTask.getUserId() != null) {
            queryWrapper.eq(SUserTask::getUserId, userTask.getUserId());
        }

        return this.baseMapper.update(userTask, queryWrapper);
    }

    @Override
    public IPage<Map> findUserTaskList(SUserTask userTask, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_ASC, false);
            return this.baseMapper.findUserTaskDetail(page, userTask);
        } catch (Exception e) {
            log.error("查询我的任务异常", e);
            return null;
        }
    }

    @Override
    public List<Long> findUserIdsByParent(Long userId) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        queryWrapper.eq(SUserTask::getUserId, userId);

        List<SUserTask> userTaskList = this.baseMapper.selectList(queryWrapper);

        List<Long> userIds = new ArrayList();

        for (SUserTask userTask : userTaskList) {

            LambdaQueryWrapper<SUserTask> queryWrapper1 = new LambdaQueryWrapper<SUserTask>();
            queryWrapper.eq(SUserTask::getParentId, userTask.getId());

            List<SUserTask> userTaskList2 = this.baseMapper.selectList(queryWrapper);
            for (SUserTask userTask2 : userTaskList2) {
                userIds.add(userTask2.getUserId());
            }
        }

        // 去重
        Set set = new HashSet();
        set.addAll(userIds);
        userIds.clear();
        userIds.addAll(set);

        return userIds;
    }

}
