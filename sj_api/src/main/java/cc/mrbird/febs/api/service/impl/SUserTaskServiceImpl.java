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

import java.util.*;

/**
 * @author MrBird
 */
@Service
public class  SUserTaskServiceImpl extends ServiceImpl<SUserTaskMapper, SUserTask> implements ISUserTaskService {

    @Override
    public List<SUserTask> findUserTaskList(SUserTask userTask) {

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

        // 支付状态
        if (userTask.getPayStatus() != null) {
            queryWrapper.eq(SUserTask::getPayStatus, userTask.getPayStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public SUserTask createUserTask(SUserTask userTask) {

        this.baseMapper.insert(userTask);

        return userTask;
    }

    @Override
    public SUserTask updateUserTask(SUserTask userTask) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        // 任务ID
        if (userTask.getId() != null) {
            queryWrapper.eq(SUserTask::getId, userTask.getId());
        }

        // 用户ID
        if (userTask.getUserId() != null) {
            queryWrapper.eq(SUserTask::getUserId, userTask.getUserId());
        }

        this.baseMapper.update(userTask, queryWrapper);
        return userTask;
    }

    @Override
    public IPage<Map> findTaskDetailByStatus( QueryRequest request, String userId, int status) {

        try {

            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "newestTime", FebsConstant.ORDER_DESC, false);
            IPage<Map> result = this.baseMapper.findTaskDetailByStatus(page, userId, status);

            return result;

        } catch (Exception e) {
            log.error("查询我的任务异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findUserTaskOutList(SUserTask userTask, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, false);
            return this.baseMapper.findUserTaskOutDetail(page, userTask);
        } catch (Exception e) {
            log.error("查询我的任务异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findUserTaskOfferList(SUserTask userTask, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, false);
            return this.baseMapper.findUserTaskOfferDetail(page, userTask);
        } catch (Exception e) {
            log.error("查询我的任务异常", e);
            return null;
        }
    }

    @Override
    public IPage<Map> findUserTaskFollowList( QueryRequest request,String userId) {

        try {

            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, null, null, false);
            IPage<Map> result = this.baseMapper.findUserTaskFollowDetail(page, userId);

            return result;
        } catch (Exception e) {
            log.error("查询我的关注任务列表异常", e);
            return null;
        }
    }

    @Override
    public Integer queryProductCount(String userId) {
        return this.baseMapper.queryProductCount(userId);
    }

    @Override
    public void updateTaskForUnLock() {
         this.baseMapper.updateTaskForUnLock();
    }

    @Override
    public void updateUserTaskLineFailBatch() {
         this.baseMapper.updateUserTaskLineFailBatch();
    }

    @Override
    public Integer updateTaskLineFailBatch() {
         return this.baseMapper.updateTaskLineFailBatch();
    }

    @Override
    public void updateUserTaskLineSuccessBatch(String taskId) {
        this.baseMapper.updateUserTaskLineSuccessBatch(taskId);
    }

    @Override
    public Integer updateTaskLineSuccessBatch(String taskLineId) {
        return this.baseMapper.updateTaskLineSuccessBatch(taskLineId);
    }

    @Override
    public Integer queryTotalCount(String userId) {
        return this.baseMapper.queryTotalCount(userId);
    }

}
