package cc.mrbird.febs.common.utils;

import java.net.URLEncoder;

public final class ContentUtil {
	public static String getString(String source) throws Exception{
		if(source==null){
			return "";
		}
		int len = source.length();
        boolean isEmoji = false;
        for (int i = 0; i < len; i++) {
            char hs = source.charAt(i);
            if (0xd800 <= hs && hs <= 0xdbff) {
                if (source.length() > 1) {
                    char ls = source.charAt(i + 1);
                    int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
                    if (0x1d000 <= uc && uc <= 0x1f77f) {
                        return URLEncoder.encode(source, "utf-8");
                    }
                }
            } else {
                // non surrogate
                if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
                	 return URLEncoder.encode(source, "utf-8");
                } else if (0x2B05 <= hs && hs <= 0x2b07) {
                	 return URLEncoder.encode(source, "utf-8");
                } else if (0x2934 <= hs && hs <= 0x2935) {
                	 return URLEncoder.encode(source, "utf-8");
                } else if (0x3297 <= hs && hs <= 0x3299) {
                	 return URLEncoder.encode(source, "utf-8");
                } else if (hs == 0xa9 || hs == 0xae || hs == 0x303d
                        || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
                        || hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
                	 return URLEncoder.encode(source, "utf-8");
                }
                if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
                    char ls = source.charAt(i + 1);
                    if (ls == 0x20e3) {
                    	 return URLEncoder.encode(source, "utf-8");
                    }
                }
            }
	}
        return source;
	}
}
