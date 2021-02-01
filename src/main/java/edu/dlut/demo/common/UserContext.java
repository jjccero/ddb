package edu.dlut.demo.common;

import org.springframework.stereotype.Component;


@Component
public class UserContext {
    private static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();


    public void setUserId(Long userId) {
        userThreadLocal.set(userId);
    }

    void remove() {
        userThreadLocal.remove();
    }

    public Long getUserId(boolean throwException) throws UserException {
        Long userId = userThreadLocal.get();
        if (userId == null && throwException)
            throw UserException.NOT_LOGIN;
        return userId;
    }
}
