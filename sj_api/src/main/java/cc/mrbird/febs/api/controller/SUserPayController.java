package cc.mrbird.febs.api.controller;

import cc.mrbird.febs.api.entity.*;
import cc.mrbird.febs.api.service.ISOfferPriceService;
import cc.mrbird.febs.api.service.ISOrderService;
import cc.mrbird.febs.api.service.ISUserPayService;
import cc.mrbird.febs.api.service.ISUserTaskService;
import cc.mrbird.febs.common.annotation.Log;
import cc.mrbird.febs.common.controller.BaseController;
import cc.mrbird.febs.common.exception.FebsException;
import cc.mrbird.febs.common.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * @author MrBird
 */
@Slf4j
@RestController
@RequestMapping("/api/s-user-pay")
public class SUserPayController extends BaseController {

    private String message;

    @Autowired
    private ISUserPayService userPayService;

    @Autowired
    private ISUserTaskService userTaskService;

    @Autowired
    private ISOrderService orderService;

    @Autowired
    private ISOfferPriceService offerPriceService;

    /**
     * 新增用户支付
     */
    @Log("新增用户支付")
    @PostMapping("/addUserPay")
    public void addUserPay(@Valid SUserPay userPay) throws FebsException {

        try {

            userPay.setCreateTime(new Date());

            SUser user = FebsUtil.getCurrentUser();
            userPay.setUserId(user.getId());

            this.userPayService.save(userPay);

        } catch (Exception e) {
            message = "新增用户支付失败";
            log.error(message, e);
            throw new FebsException(message);
        }
    }

    /**
     * 微信支付成功回调
     */
    @Log("微信支付成功回调")
    @PostMapping("/paySuccess")
    public void paySuccess(HttpServletRequest request, HttpServletResponse response) throws FebsException {

        // 获取返回数据
        StringBuffer sb   =new StringBuffer();
        InputStream is = null;
        try {
            is = request.getInputStream();
            byte[] b =new byte[2048];
            for (int n;(n=is.read(b))!=-1;){
                sb.append(new String(b,0,n));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 返回值转xml
        Map<String,Object> map = XmlUtil.xmltoMap(sb.toString());

        // 订单号
        String paySn = map.get("out_trade_no").toString();

        // 支付流水号
        String transSn = map.get("transaction_id").toString();

        // 关联ID
        String relationId = map.get("attach").toString();

        // 支付金额
        String total_fee = map.get("total_fee").toString();
        BigDecimal total = new BigDecimal(total_fee).divide(new BigDecimal("100"));

        // openId
        String openid = map.get("openid").toString();

        // 支付成功时更新订单状态
        if (map.get("return_code").equals("SUCCESS")) {

            String strPayType = paySn.substring(0, 1);

            // 用户支付表插入
            SUserPay userPay = new SUserPay();
            userPay.setCreateTime(new Date());
            userPay.setPayAmount(total);
            userPay.setTotalAmount(total);
            userPay.setPaySn(paySn);
            userPay.setPayStatus(1);
            userPay.setPayTime(new Date());
            userPay.setRelationId(Long.parseLong(relationId));
            userPay.setTransSn(transSn);
            userPay.setPayType(1);

            if ("T".equals(strPayType)) {

                // 领取任务支付成功
                SUserTask userTask = userTaskService.getById(relationId);

                userPay.setUserId(userTask.getUserId());

                userPayService.save(userPay);

                // 变更用户任务的支付状态
                userTask.setPayStatus(1);
                userTask.setUpdateTime(new Date());
                userTaskService.updateUserTask(userTask);

            } else if ("O".equals(strPayType)) {

                // 支付购买订单成功
                SOrder order = orderService.getById(relationId);

                userPay.setUserId(order.getUserId());

                userPayService.save(userPay);

                // 变更订单状态 已付款
                order.setPaymentState(1);
                order.setOrderStatus(1);
                order.setPaymentTime(new Date());
                orderService.updateOrder(order);

            } else if ("P".equals(strPayType)) {

                // 转让任务报价成功
                SOfferPrice offerPrice = offerPriceService.getById(relationId);

                userPay.setUserId(offerPrice.getUserId());

                userPayService.save(userPay);

                // 变更订单状态 已付款
                offerPrice.setPayStatus(1);
                offerPrice.setUpdateTime(new Date());
                offerPriceService.updateById(offerPrice);
            }

        }

        ResponseWriteUtil.responseWriteClient(request, response, "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>");
    }
}
