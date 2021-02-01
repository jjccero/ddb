package edu.dlut.demo.service;


import edu.dlut.demo.common.UserException;
import edu.dlut.demo.model.LoginVO;
import edu.dlut.demo.model.PasswordVO;
import edu.dlut.demo.model.User;
import edu.dlut.demo.model.UserVO;

public interface UserService {
    User login(LoginVO loginVO) throws UserException;

    boolean logout(String sessionId);

    boolean signup(User user);

    boolean updateUser(User user);

    UserVO getUser(Long userId);

    Long getUserId(String sessionId);

    boolean updatePassword(PasswordVO passwordVO);

}
