package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.SUserTask;
import cc.mrbird.febs.api.mapper.SUserTaskMapper;
import cc.mrbird.febs.api.service.ISProductService;
import cc.mrbird.febs.api.service.ISUserTaskService;
import cc.mrbird.febs.common.domain.FebsConstant;
import cc.mrbird.febs.common.domain.QueryRequest;
import cc.mrbird.febs.common.utils.SortUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author MrBird
 */
@Service
public class SUserTaskServiceImpl extends ServiceImpl<SUserTaskMapper, SUserTask> implements ISUserTaskService {

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
    public Integer findProductCount(String userId,String productId) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        // 用户ID
        queryWrapper.eq(SUserTask::getUserId, userId);

        // 商品ID
        queryWrapper.eq(SUserTask::getProductId, productId);

        // 支付状态
        queryWrapper.eq(SUserTask::getPayStatus, 1);

        // 状态  0 已接任务  1 转让中 3 任务完结  进行中+结算中
       /* List status = new ArrayList();
        status.add(0);
        status.add(1);
        status.add(3);
*/
        List<SUserTask> userTaskList = this.baseMapper.selectList(queryWrapper);

        List<String> productIdList = new ArrayList();
        for (SUserTask userTask1 : userTaskList) {
            productIdList.add(userTask1.getProductId());
        }

        // 去掉重复商品ID
        Set set = new HashSet();
        set.addAll(productIdList);
        productIdList.clear();
        productIdList.addAll(set);

        return productIdList.size();
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
    public IPage<Map> findUserTaskEndList(SUserTask userTask, QueryRequest request) {
        try {
            Page<Map> page = new Page<>();
            SortUtil.handlePageSort(request, page, "createTime", FebsConstant.ORDER_DESC, false);
            return this.baseMapper.findUserTaskEndDetail(page, userTask);
        } catch (Exception e) {
            log.error("查询我的任务异常", e);
            return null;
        }
    }

    @Override
    public List<String> findUserIdsByParent(String userId) {

        LambdaQueryWrapper<SUserTask> queryWrapper = new LambdaQueryWrapper<SUserTask>();

        queryWrapper.eq(SUserTask::getUserId, userId);

        List<SUserTask> userTaskList = this.baseMapper.selectList(queryWrapper);

        List<String> userIds = new ArrayList();

        for (SUserTask userTask : userTaskList) {

            LambdaQueryWrapper<SUserTask> queryWrapper1 = new LambdaQueryWrapper<SUserTask>();


            List<SUserTask> userTaskList2 = this.baseMapper.selectList(queryWrapper1);
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

    @Override
    public Integer queryProductCount(String userId) {
        return this.baseMapper.queryProductCount(userId);
    }

}
