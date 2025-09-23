package com.yizhaoqi.pairesume.common.constant;

public class RedisKeys {

    /**
     * 邮箱注册验证码 Key
     * email:register:a@b.com -> code
     */
    public static final String EMAIL_REGISTER_CODE_KEY = "email:register:code:";

    /**
     * 邮箱注册频率限制 Key
     * email:register:rate_limit:a@b.com -> timestamp
     */
    public static final String EMAIL_REGISTER_RATE_LIMIT_KEY = "email:register:rate_limit:";

    /**
     * 邮箱重置密码验证码 Key
     * email:reset:a@b.com -> code
     */
    public static final String EMAIL_RESET_PASSWORD_CODE_KEY = "email:reset_password:code:";

    /**
     * 邮箱重置密码频率限制 Key
     * email:reset_password:rate_limit:a@b.com -> timestamp
     */
    public static final String EMAIL_RESET_PASSWORD_RATE_LIMIT_KEY = "email:reset_password:rate_limit:";
    /**
     * Access Token 黑名单 Key
     * blacklist:access_token:xxxxxxxx -> ""
     */
    public static final String BLACKLIST_ACCESS_TOKEN_KEY = "blacklist:access_token:";

    /**
     * Refresh Token 存储 Key
     * refresh_token:xxxxxxxx -> user_info
     */
    public static final String REFRESH_TOKEN_KEY = "refresh_token:";

}
