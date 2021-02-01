package edu.dlut.demo.controller;


import edu.dlut.demo.common.UserContext;
import edu.dlut.demo.common.UserException;
import edu.dlut.demo.model.LoginVO;
import edu.dlut.demo.model.PasswordVO;
import edu.dlut.demo.model.User;
import edu.dlut.demo.service.UserService;
import edu.dlut.demo.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    @Autowired
    UserService userService;
    @Autowired
    UserContext userContext;

    @PostMapping("/signup")
    public boolean signup(@RequestBody User user) {
        user.setGmtCreate(TimeUtil.getGmtCreate());
        return userService.signup(user);
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginVO loginVO) throws UserException {
        return userService.login(loginVO);
    }

    @GetMapping("/logout")
    public boolean logout(String sessionId) {
        return userService.logout(sessionId);
    }

    @PostMapping("/updateUser")
    public boolean updateUser(@RequestBody User user) throws UserException {
        user.setUserId(userContext.getUserId(true));
        return userService.updateUser(user);
    }

    @PostMapping("/updatePassword")
    public boolean updatePassword(@RequestBody PasswordVO passwordVO) throws UserException {
        passwordVO.setUserId(userContext.getUserId(true));
        return userService.updatePassword(passwordVO);
    }
}
