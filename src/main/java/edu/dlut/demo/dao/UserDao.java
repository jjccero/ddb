package edu.dlut.demo.dao;


import edu.dlut.demo.model.PasswordVO;
import edu.dlut.demo.model.User;
import edu.dlut.demo.model.UserVO;


public interface UserDao {
    boolean insertUser(User user);

    boolean updateUser(User user);

    UserVO selectUserVOByUserId(Long userId);

    User selectUserByUsername(String username);

    String selectPasswordByUserId(Long userId);

    boolean updatePassword(PasswordVO passwordVO);
}
