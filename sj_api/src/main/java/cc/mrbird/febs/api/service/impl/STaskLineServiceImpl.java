package cc.mrbird.febs.api.service.impl;

import cc.mrbird.febs.api.entity.STaskLine;
import cc.mrbird.febs.api.entity.SUserBrowser;
import cc.mrbird.febs.api.entity.SUserShare;
import cc.mrbird.febs.api.entity.SUserTaskLine;
import cc.mrbird.febs.api.mapper.STaskLineMapper;
import cc.mrbird.febs.api.service.ISTaskLineService;
import cc.mrbird.febs.api.service.ISUserBrowserService;
import cc.mrbird.febs.api.service.ISUserShareService;
import cc.mrbird.febs.api.service.ISUserTaskLineService;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author MrBird
 */
@Service
public class STaskLineServiceImpl extends ServiceImpl<STaskLineMapper, STaskLine> implements ISTaskLineService {
	@Autowired
	private ISUserTaskLineService sUserTaskLineService;
	@Autowired
	private ISUserShareService sUserShareService;
	@Autowired
	private ISUserBrowserService sUserBrowserService;
    
	@Override
    public Integer queryTaskLineCount(String productId,String userId) {
        return this.baseMapper.queryTaskLineCount(productId, userId);
    }

    @Override
    public Integer queryMinLineOrder(String productId) {
        return this.baseMapper.queryMinLineOrder(productId);
    }

    @Override
    public String queryIdByLineOrder(String productId, Integer lineOrder) {
        return this.baseMapper.queryIdByLineOrder(productId, lineOrder);
    }

    @Override
    public List<STaskLine> findTaskLineList(STaskLine taskLine) {

        LambdaQueryWrapper<STaskLine> queryWrapper = new LambdaQueryWrapper();

        if (taskLine.getId() != null) {
            queryWrapper.eq(STaskLine::getId, taskLine.getId());
        }

        if (taskLine.getProductId() != null) {
            queryWrapper.eq(STaskLine::getProductId, taskLine.getProductId());
        }

        if (taskLine.getOrderProductId() != null) {
            queryWrapper.eq(STaskLine::getOrderProductId, taskLine.getOrderProductId());
        }

        if (taskLine.getLineStatus() != null) {
            queryWrapper.eq(STaskLine::getLineStatus, taskLine.getLineStatus());
        }

        if (taskLine.getSettleStatus() != null) {
            queryWrapper.eq(STaskLine::getSettleStatus, taskLine.getSettleStatus());
        }

        return this.baseMapper.selectList(queryWrapper);
    }

    @Override
    public STaskLine findTaskLineForSettle(String productId, String userId) {
    	String taskLineId=null;
    	QueryWrapper<SUserTaskLine> queryWrapper1=new QueryWrapper<SUserTaskLine>();
    	queryWrapper1.eq("product_id", productId);
    	queryWrapper1.eq("user_id", userId);
    	queryWrapper1.eq("status", 0);
    	
    	queryWrapper1.eq("pay_status", 1);
		//先判断我有没有购买这个产品的任务线
    	List<SUserTaskLine> st=this.sUserTaskLineService.list(queryWrapper1);
    	if(st!=null&&st.size()>0) {
    		taskLineId=st.get(0).getTaskLineId();
    	}else {
    		QueryWrapper<SUserBrowser> queryWrapper2=new QueryWrapper<SUserBrowser>();
			//如果没查到 再查上级有没有任务
    		//1 先查询这个用户 这个产品有没有上级分享
    		queryWrapper2.eq("user_id", userId);
    		queryWrapper2.eq("product_id", productId);
    		queryWrapper2.orderByDesc("create_time");
    		List<SUserBrowser> sslist=sUserBrowserService.list(queryWrapper2);
    	
    		
    		if(sslist!=null&&sslist.size()>0&&sslist.get(0).getShareId()!=null) {
    		
    			//根据parentId 查询上级分享人
    			SUserShare sss=sUserShareService.getById(sslist.get(0).getShareId());
    			queryWrapper1=new QueryWrapper<SUserTaskLine>();
    			queryWrapper1.eq("product_id", productId);
    	    	queryWrapper1.eq("status", 0);
    	    	queryWrapper1.eq("pay_status", 1);
    			queryWrapper1.eq("user_id", sss.getUserId());
    			List<SUserTaskLine> st2=this.sUserTaskLineService.list(queryWrapper1);
    			if(st2!=null&&st2.size()>0) {
    	    		taskLineId=st2.get(0).getTaskLineId();
    	    	}
    		}
    	
    	}
    	if(taskLineId!=null) {
    		STaskLine tl=this.baseMapper.selectById(taskLineId);
    		return tl;
    	}

        LambdaQueryWrapper<STaskLine> queryWrapper = new LambdaQueryWrapper();

        // 商品ID
        queryWrapper.eq(STaskLine::getProductId, productId);

        // 结算状态  0：未完成
        queryWrapper.eq(STaskLine::getSettleStatus, 0);
        queryWrapper.orderByAsc(STaskLine::getLineOrder);

        List<STaskLine> list = this.baseMapper.selectList(queryWrapper);

        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }
    }

    @Override
    public void updateUserTaskLineForSettle(List<String> list) {
        this.baseMapper.updateUserTaskLineForSettle(list);
    }

}
