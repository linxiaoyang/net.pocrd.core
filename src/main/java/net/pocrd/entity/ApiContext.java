package net.pocrd.entity;

import net.pocrd.core.LocalException;
import net.pocrd.define.SerializeType;
import net.pocrd.responseEntity.KeyValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Api请求上下文信息
 */
public class ApiContext {
    private static final Logger                  logger      = LoggerFactory.getLogger(ApiContext.class);
    /**
     * 当前线程的ApiContext对象
     */
    private static       ThreadLocal<ApiContext> threadLocal = new ThreadLocal<ApiContext>();

    /**
     * 获取当前Api上下文
     */
    public static ApiContext getCurrent() {
        ApiContext current = threadLocal.get();
        if (current == null) {
            current = new ApiContext();
            threadLocal.set(current);
        }
        return current;
    }

    private ApiContext() {
    }

    public final Pattern callbackRegex = Pattern.compile("^[A-Za-z]\\w{5,64}$");

    /**
     * 调用资源描述
     */
    public ArrayList<ApiMethodCall> apiCallInfos = null;

    /**
     * 是否为ssl链接
     */
    public boolean isSSL = false;

    /**
     * 当前调用资源描述
     */
    public ApiMethodCall currentCall = null;

    /**
     * 访问信息
     */
    public Map<String, String> requestInfo;

    public final void ignoreParameterForSecurity(String key) {
        if (requestInfo != null) {
            requestInfo.remove(key);
        }
    }

    public final String getRequestString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(isSSL ? "https://" : "http://");
        sb.append(host);
        sb.append("/m.api?");
        if (requestInfo != null) {
            if (CompileConfig.isDebug) {  // 开发环境下用于将打印到日志的url还原成能够直接放到浏览器请求的编码格式。
                try {
                    for (String key : requestInfo.keySet()) {
                        if (key != null) {
                            sb.append(key);
                            sb.append("=");
                            sb.append(URLEncoder.encode(requestInfo.get(key), "UTF-8"));
                            sb.append("&");
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    logger.error("URLEncoder encode the post data failad", e);
                }
            } else {
                for (String key : requestInfo.keySet()) {
                    if (key != null) {
                        sb.append(key);
                        sb.append("=");
                        sb.append(requestInfo.get(key));
                        sb.append("&");
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * 用户账号,日志用
     */
    public String uid;

    /**
     * http请求的标识符
     */
    public String cid;

    /**
     * 设备序列号,业务用
     */
    public long deviceId;

    /**
     * 设备序列号,日志用
     */
    public String deviceIdStr;

    /**
     * 客户端应用版本号
     */
    public String versionCode;

    /**
     * 客户端应用版本名 例:1.6.0
     */
    public String versionName;

    /**
     * 应用编号,显示传参的_aid
     */
    public int appid;

    /**
     * 第三方合作者编号
     */
    public int thirdPartyId;

    /**
     * 返回值序列化方式
     */
    public SerializeType format = SerializeType.JSON;

    /**
     * 返回消息的语言
     */
    public String location;

    /**
     * 访问时间
     */
    public long startTime = 0;

    /**
     * 时间开销
     */
    public int costTime;

    /**
     * 客户端信息
     */
    public String agent;

    /**
     * http referer
     */
    public String referer;

    /**
     * 访问站点
     */
    public String host;

    /**
     * 是否清除用戶 token
     */
    public boolean clearUserToken = false;

    /**
     * 是否清除用戶 token 标志位
     */
    public boolean clearUserTokenFlag = false;

    /**
     * 客户端IP
     */
    public String clientIP;

    /**
     * Device Token
     */
    public String deviceToken;

    /**
     * Token
     */
    public String token;

    /**
     * secret token 用于在不同domian间传递csrftoken, 只能在https协议下传入
     */
    public String stoken;

    /**
     * Security Level 本次调用所需的综合安全级别
     */
    public int requiredSecurity;

    /**
     * 调用者信息
     */
    public CallerInfo caller;

    /**
     * 已进行序列化的method call计数, 用于接口合并调用时分段下发返回值
     */
    public int serializeCount;

    /**
     * 错误数据
     */
    public LocalException localException;

    /**
     * 线程相关的序列化数据缓冲池，用于暂存序列化数据
     */
    public ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);

    /**
     * jsonp回调信息
     */
    public byte[] jsonpCallback = null;

    /**
     * 返回给客户端的额外消息
     */
    private Map<String, KeyValuePair> notifications = new HashMap<String, KeyValuePair>();

    /**
     * 客户端传上来的 cookie
     */
    private Map<String, String> cookies = new HashMap<String, String>();

    /**
     * 添加 cookie
     *
     * @param key
     * @param value
     */
    public final void addCookie(String key, String value) {
        cookies.put(key, value);
    }

    /**
     * 获取 cookie 值
     *
     * @param key
     * @return
     */
    public final String getCookie(String key) {
        return cookies.get(key);
    }

    /**
     * 不存储重复的key
     *
     * @param n notification
     */
    public final void addNotification(KeyValuePair n) {
        if (n == null)
            return;
        if (!notifications.containsKey(n.key)) {
            notifications.put(n.key, n);
        }
    }

    public final void clearNotification() {
        notifications.clear();
    }

    public final List<KeyValuePair> getNotifications() {
        return new ArrayList<KeyValuePair>(notifications.values());
    }

    /**
     * 清除变量信息
     */
    public final void clear() {
        this.agent = null;
        this.apiCallInfos = null;
        this.appid = 0;
        this.caller = null;
        this.cid = null;
        this.clearUserToken = false;
        this.clearUserTokenFlag = false;
        this.clientIP = null;
        this.cookies.clear();
        this.costTime=0;
        this.currentCall = null;
        this.deviceId = 0;
        this.deviceIdStr = null;
        this.deviceToken = null;
        this.format = SerializeType.JSON;
        this.host = null;
        this.isSSL = false;
        this.jsonpCallback = null;
        this.localException = null;
        this.location = null;
        this.notifications.clear();
        this.outputStream.reset();
        this.referer = null;
        this.requiredSecurity = 0;
        this.requestInfo = null;
        this.serializeCount = 0;
        this.startTime = 0;
        this.stoken = null;
        this.thirdPartyId = 0;
        this.token = null;
        this.uid = null;
        this.versionCode = null;
        this.versionName = null;
        MDC.clear();
    }
}