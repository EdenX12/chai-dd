/*
 * (C) 2015 EBANG 宜修网络科技有限公司All Rights Reserved.
 * 
 * 项目名 :
 *   一修哥微信公众平台
 *
 */
package cc.mrbird.febs.common.utils;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author wangmingzhen
 *
 */
public final class ResponseWriteUtil {

	/**
	 * 作用：后台向前台返回数据方法
	 * @param request
	 * @param response
	 * @param object 需要返回的前台页面的值
	 * @throws Exception
	 */
	public static void responseWriteClient(HttpServletRequest request,
		HttpServletResponse response,Object object) {

		try{
			response.setCharacterEncoding("utf-8");
			response.setHeader("pragma", "no-cache");
			response.setHeader("cache-control", "no-cache");

			PrintWriter out;

			out = response.getWriter();

			out.print(object);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
