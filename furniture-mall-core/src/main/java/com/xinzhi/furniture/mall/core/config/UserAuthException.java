package com.xinzhi.furniture.mall.core.config;

public class UserAuthException extends RuntimeException {

    public UserAuthException() {
        super("User not login");
    }

}
