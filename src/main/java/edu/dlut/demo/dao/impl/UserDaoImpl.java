package edu.dlut.demo.dao.impl;

import com.alibaba.fastjson.JSON;
import edu.dlut.demo.dao.UserDao;
import edu.dlut.demo.model.PasswordVO;
import edu.dlut.demo.model.User;
import edu.dlut.demo.model.UserVO;
import edu.dlut.demo.util.ShardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.ShardedJedis;

@Slf4j
@Repository
public class UserDaoImpl implements UserDao {
    private String getUserEey(Long userId) {
        return userId != null ? "user:" + userId : null;
    }

    private String getUsernameKey(String username) {
        return "username:" + username;
    }

    private User getUser(String userKey, ShardedJedis jedis) {
        if (userKey != null) {
            String userString = jedis.get(userKey);
            if (userString != null) {
                return JSON.parseObject(userString, User.class);
            }
        }
        return null;
    }

    @Override
    public boolean insertUser(User user) {
        boolean res = false;
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String usernameKey = getUsernameKey(user.getUsername());
            if (jedis.setnx(usernameKey, "placeholder") == 1L) {
                Long userId = jedis.incr("currentUserId");
                jedis.set(usernameKey, userId.toString());
                String userKey = getUserEey(userId);
                user.setUserId(userId);
                jedis.set(userKey, JSON.toJSONString(user));
                res = true;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    @Override
    public boolean updateUser(User user) {
        boolean res = false;
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String userKey = "user:" + user.getUserId();
            User oldUser = getUser(userKey, jedis);
            if (oldUser != null) {
                oldUser.setSex(user.getSex());
                oldUser.setContact(user.getContact());
                jedis.set(userKey, JSON.toJSONString(oldUser));
                res = true;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    @Override
    public UserVO selectUserVOByUserId(Long userId) {
        UserVO userVO = new UserVO();
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String userKey = getUserEey(userId);
            User user = getUser(userKey, jedis);
            if (user != null) {
                userVO.setContact(user.getContact());
                userVO.setSex(user.getSex());
                userVO.setUserId(userId);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return userVO;
    }

    @Override
    public User selectUserByUsername(String username) {
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String usernameKey = getUsernameKey(username);
            String userIdString = jedis.get(usernameKey);
            if (userIdString != null) {
                Long userId = Long.parseLong(userIdString);
                String userKey = getUserEey(userId);
                return getUser(userKey, jedis);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return null;
    }

    @Override
    public String selectPasswordByUserId(Long userId) {
        String password = null;
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String userKey = getUserEey(userId);
            User user = getUser(userKey, jedis);
            if (user != null) {
                password = user.getPassword();
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return password;
    }

    @Override
    public boolean updatePassword(PasswordVO passwordVO) {
        boolean res = false;
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            String userKey = getUserEey(passwordVO.getUserId());
            User oldUser = JSON.parseObject(jedis.get(userKey), User.class);
            if (oldUser != null) {
                oldUser.setPassword(passwordVO.getPassword());
                jedis.set(userKey, JSON.toJSONString(oldUser));
                res = true;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

}
