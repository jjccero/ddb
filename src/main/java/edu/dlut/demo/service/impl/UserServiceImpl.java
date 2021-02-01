package edu.dlut.demo.service.impl;

import edu.dlut.demo.common.UserException;
import edu.dlut.demo.dao.UserDao;
import edu.dlut.demo.model.LoginVO;
import edu.dlut.demo.model.PasswordVO;
import edu.dlut.demo.model.User;
import edu.dlut.demo.model.UserVO;
import edu.dlut.demo.service.UserService;
import edu.dlut.demo.util.RedisUtil;
import edu.dlut.demo.util.ShardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedis;

import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserDao userDao;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User login(LoginVO loginVO) throws UserException {
        if (loginVO != null) {
            User user = userDao.selectUserByUsername(loginVO.getUsername());
            if (user != null && passwordEncoder.matches(loginVO.getPassword(), user.getPassword())) {
                user.setPassword(null);
                String sessionId = UUID.randomUUID().toString();
                user.setSessionId(sessionId);
                try (ShardedJedis jedis = ShardUtil.getJedis()) {
                    jedis.set(sessionId, user.getUserId().toString(), RedisUtil.EX_DAY);
                    return user;
                } catch (Exception e) {
                    log.error(e.toString());
                }
            }
        }
        throw UserException.FAIL_LOGIN;
    }

    @Override
    public boolean logout(String sessionId) {
        boolean res = false;
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            res = jedis.del(sessionId) == 1L;
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    @Override
    public boolean signup(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.insertUser(user);
    }

    @Override
    public boolean updateUser(User user) {
        return userDao.updateUser(user);
    }

    @Override
    public UserVO getUser(Long userId) {
        return userDao.selectUserVOByUserId(userId);
    }

    @Override
    public Long getUserId(String sessionId) {
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String userIdString = jedis.get(sessionId);
            if (userIdString != null)
                return Long.parseLong(userIdString);
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public boolean updatePassword(PasswordVO passwordVO) {
        boolean res = false;
        String oldPassword = userDao.selectPasswordByUserId(passwordVO.getUserId());
        if (passwordEncoder.matches(passwordVO.getOldPassword(), oldPassword)) {
            passwordVO.setPassword(passwordEncoder.encode(passwordVO.getPassword()));
            res = userDao.updatePassword(passwordVO);
        }
        return res;
    }
}
