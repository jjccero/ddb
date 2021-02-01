package edu.dlut.demo.common;


public class UserException extends Exception {
    public static UserException NOT_LOGIN = new UserException(10001, "未登录或登陆超时");
    public static UserException FAIL_LOGIN = new UserException(10002, "用户名或密码不存在");
    private Integer code;

    public UserException(Integer code,String message) {
        super(message);
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
