package cc.mrbird.febs.common.domain;

import java.util.HashMap;

/**
 * 特意约定下  code=0 处理正常的业务逻辑 code=-1 是说没登录 前端做登录处理 code=1 可以放任何信息 前端只要捕获code=1 就把message展示给用户
 * 所以 message里 特意加了个code=0
 * warn 里特意加了个code=1
 * 20200430-wmz
 * @author Administrator
 *
 */
public class FebsResponse extends HashMap<String, Object> {

    private static final long serialVersionUID = -8713837118340960775L;
    
    public FebsResponse message(String message) {
        this.put("message", message);
        this.put("code", 1);
        return this;
    }
    public FebsResponse warn(String message) {
        this.put("message", message);
        this.put("code", 1);
        return this;
    }

    public FebsResponse data(Object data) {
        this.put("data", data);
        return this;
    }

    @Override
    public FebsResponse put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
