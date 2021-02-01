package edu.dlut.demo.common;


import edu.dlut.demo.service.UserService;
import edu.dlut.demo.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class UserIntercepter implements HandlerInterceptor {
    @Autowired
    UserContext userContext;
    @Autowired
    UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        log.info(IpUtil.getIpAddress(request) + "|" + request.getRequestURI());
        String sessionId = request.getHeader("sessionId");
        if (sessionId != null)
            userContext.setUserId(userService.getUserId(sessionId));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        userContext.remove();
    }
}
