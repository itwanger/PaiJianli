package com.yizhaoqi.pairesume.common.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 通用返回对象
 *
 * @param <T>
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功
     */
    public static final int SUCCESS = 200;

    /**
     * 失败
     */
    public static final int FAIL = 500;

    private Integer code;

    private String msg;

    private T data;

    public R() {}

    public R(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS, "操作成功", data);
    }

    public static <T> R<T> ok(String msg) {
        return new R<>(SUCCESS, msg, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return new R<>(SUCCESS, msg, data);
    }

    public static <T> R<T> fail(String msg) {
        return new R<>(500, msg, null);
    }

    public static <T> R<T> fail(Integer code, String msg) {
        return new R<>(code, msg);
    }
}
