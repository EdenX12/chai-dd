/*
 * (C) 2015 EBANG 宜修网络科技有限公司All Rights Reserved.
 * 
 * 项目名 :
 *   一修哥微信公众平台
 *
 */
package cc.mrbird.febs.common.utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author wangmingzhen
 *
 */
public final class XmlUtil {

	@SuppressWarnings("rawtypes")
	public static String maptoXml(Map map) {
		Document document = DocumentHelper.createDocument();
		Element nodeElement = document.addElement("xml");
		for (Object obj : map.keySet()) {
			Element keyElement = nodeElement.addElement(String.valueOf(obj));
			//keyElement.addAttribute("label", String.valueOf(obj));
			keyElement.setText(String.valueOf(map.get(obj)));
		}
		return doc2String(document);
	}

	public static String doc2String(Document document) {

		String s = "";
		try {
			// 使用输出流来进行转化 
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// 使用UTF-8编码 
			OutputFormat format = new OutputFormat("   ", true, "UTF-8");
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(document);
			s = out.toString("UTF-8");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return s;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map xmltoMap(String xml) {

		try {

			Map map = new HashMap();
			Document document = DocumentHelper.parseText(xml);
			Element nodeElement = document.getRootElement();
			List node = nodeElement.elements();

			for (Iterator it = node.iterator(); it.hasNext();) {
				Element elm = (Element) it.next();
				// System.out.println(elm.getName()+"++++++++"+elm.getText());
				map.put(elm.getName(), elm.getText());
				elm = null;
			}

			node = null;
			nodeElement = null;
			document = null;

			return map;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	//JSON转map
	public static Map<String,Object> jsontoMap(String jsonStr){
		 Map<String, Object> map = new HashMap<String, Object>();  
	        //最外层解析  
	        JSONObject json = JSONObject.parseObject(jsonStr);
	        for(Object k : json.keySet()){  
	            Object v = json.get(k);   
	            //如果内层还是数组的话，继续解析  
	            if(v instanceof JSONArray){  
	                List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
	                Iterator<Object> it = ((JSONArray)v).listIterator();  
	                while(it.hasNext()){  
	                    JSONObject json2 =JSONObject.parseObject(it.next().toString());  
	                    list.add(XmlUtil.jsontoMap(json2.toString()));  
	                }  
	                map.put(k.toString(), list);  
	            } else {  
	                map.put(k.toString(), v);  
	            }  
	        }  
	        return map;  
	}

}
