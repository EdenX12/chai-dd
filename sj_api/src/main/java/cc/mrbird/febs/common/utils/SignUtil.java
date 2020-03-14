/*
 * (C) 2015 EBANG 宜修网络科技有限公司All Rights Reserved.
 * 
 * 项目名 :
 *   一修哥微信公众平台
 *
 */
package cc.mrbird.febs.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class SignUtil {

	// 与接口配置信息中的Token要一致  
	private static String token = "rongque";  

	/** 
	 * 验证签名 
	 *  
	 * @param signature 
	 * @param timestamp 
	 * @param nonce 
	 * @return 
	 */  
	public static boolean checkSignature(String signature, String timestamp, String nonce) {

		String[] arr = new String[] { token, timestamp, nonce };
		// 将token、timestamp、nonce三个参数进行字典序排序 
		Arrays.sort(arr);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < arr.length; i++) {
			content.append(arr[i]);
		}
		MessageDigest md = null;
		String tmpStr = null;

		try {
			md = MessageDigest.getInstance("SHA-1");
			// 将三个参数字符串拼接成一个字符串进行sha1加密 
			byte[] digest = md.digest(content.toString().getBytes());
			tmpStr = byteToStr(digest);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		content = null;

		// 将sha1加密后的字符串可与signature对比，标识该请求来源于微信  
		return tmpStr != null ? tmpStr.equals(signature.toUpperCase()) : false;  
	}

	// 获取加密字符串
	public static String findSignature(Map<String,Object> map){

		Set<String> st = map.keySet();
		String[] aa = new String[st.size()];
		Iterator<String> it = st.iterator();
		for(int i = 0; i < st.size(); i++) {
			aa[i] = it.next();
		}
		Arrays.sort(aa);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < aa.length; i++) {
			content.append("&"+aa[i]+"="+map.get(aa[i]));
		}

		String tmpStr = null;
		String ss=content.toString().substring(1);

		// 将三个参数字符串拼接成一个字符串进行sha1加密  
		tmpStr = SHA1.encode(ss);

		return tmpStr;
	}

	// 获取加密字符串
	public static String findSignForPay(Map<String,Object> map,String appKey){

		Set<String> st=map.keySet();
		String[] aa=new String[st.size()];
		Iterator<String> it=st.iterator();
		for(int i=0;i<st.size();i++){
			aa[i]=it.next();
		}
		Arrays.sort(aa);  
		StringBuilder content = new StringBuilder();  
		for (int i = 0; i < aa.length; i++) {  
			content.append("&"+aa[i]+"="+map.get(aa[i]));  
		}
		content.append("&key=");
		content.append(appKey);

		String tmpStr = null;  
		String ss=content.toString().substring(1);

		// 将三个参数字符串拼接成一个字符串进行sha1加密  
		//MD5 md2=new MD5();
		System.out.println(ss);
		tmpStr=MD5.encode(ss);
		System.out.println(tmpStr);
		return tmpStr;
	}
	
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}

	// MD5加密
	public static String findMd5(Map<String,Object> map){

		Set<String> st=map.keySet();
		String[] aa=new String[st.size()];
		Iterator<String> it=st.iterator();
		for(int i=0;i<st.size();i++){
			aa[i]=it.next();
		}
		Arrays.sort(aa);
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < aa.length; i++) {
			content.append("&"+aa[i]+"="+map.get(aa[i]));
		}

		String tmpStr = null;
		String ss=content.toString().substring(1);

		// 将三个参数字符串拼接成一个字符串进行sha1加密  
		MD5 md2=new MD5();
		tmpStr=MD5.encode(ss).toUpperCase();

		return tmpStr;
	}

	/** 
	 * 将字节数组转换为十六进制字符串 
	 *  
	 * @param byteArray 
	 * @return 
	 */  
	private static String byteToStr(byte[] byteArray) {
		String strDigest = "";
		for (int i = 0; i < byteArray.length; i++) {
			strDigest += byteToHexStr(byteArray[i]);
		}
		return strDigest;
	}

	/** 
	 * 将字节转换为十六进制字符串 
	 *  
	 * @param mByte 
	 * @return 
	 */  
	private static String byteToHexStr(byte mByte) {  
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };  
		char[] tempArr = new char[2];
		tempArr[0] = Digit[(mByte >>> 4) & 0X0F];
		tempArr[1] = Digit[mByte & 0X0F];

		String s = new String(tempArr);
		return s;
	}

	public static String sha1(String data) throws NoSuchAlgorithmException {

		MessageDigest md = MessageDigest.getInstance("SHA1");
		md.update(data.getBytes());
		StringBuffer buf = new StringBuffer();

		byte[] bits = md.digest();

		for(int i=0;i<bits.length;i++){
		
			int a = bits[i];
			if(a<0) a+=256;
			if(a<16) buf.append("0");
			buf.append(Integer.toHexString(a));
		}

		return buf.toString();
	}

}
