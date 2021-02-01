package edu.dlut.demo.dao.impl;

import com.alibaba.fastjson.JSON;
import edu.dlut.demo.dao.JourneyDao;
import edu.dlut.demo.model.Journey;
import edu.dlut.demo.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
public class JourneyDaoImpl implements JourneyDao {
    private static String journeySetKey = "journeys";

    private String getJourneyKey(Long journeyId) {
        return journeyId != null ? "journey:" + journeyId : null;
    }

    private String getFromKey(String fromCity) {
        return fromCity != null ? "fromCity:" + fromCity : null;
    }

    private String getToKey(String toCity) {
        return toCity != null ? "toCity:" + toCity : null;
    }

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private String getDateKey(Timestamp departDate) {
        return departDate != null ? "departDate:" + simpleDateFormat.format(departDate) : null;
    }

    private String getTypeKey(Short type) {
        return type != null ? "type:" + type : null;
    }

    private String getUserJourneysKey(Long userId) {
        return userId != null ? "userJourneys:" + userId : null;
    }

    private Journey getJourney(String journeyKey, Jedis jedis) {
        if (journeyKey != null) {
            String journeyString = jedis.get(journeyKey);
            if (journeyString != null) {
                return JSON.parseObject(journeyString, Journey.class);
            }
        }
        return null;
    }

    @Override
    public long insertJourney(Journey journey) {
        try (Jedis jedis = RedisUtil.getJedis()) {
            Long journeyId = jedis.incr("currentJourneyId");
            if (journeyId > 0L) {
                journey.setJourneyId(journeyId);
                String journeyKey = getJourneyKey(journeyId);
                String journeyIdString = journeyId.toString();
                Transaction transaction = jedis.multi();
                transaction.set(journeyKey, JSON.toJSONString(journey));
                transaction.sadd(journeySetKey, journeyIdString);
                transaction.sadd(getUserJourneysKey(journey.getUserId()), journeyIdString);
                sadd(journey, journeyIdString, transaction);
                transaction.exec();
                return journeyId;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return 0L;
    }

    @Override
    public boolean updateJourney(Journey journey) {
        boolean res = false;
        Long journeyId = journey.getJourneyId();
        String journeyKey = getJourneyKey(journeyId);
        try (Jedis jedis = RedisUtil.getJedis()) {
            Journey oldJourney = getJourney(journeyKey, jedis);
            if (oldJourney != null) {
                String journeyIdString = journeyId.toString();
                Transaction transaction = jedis.multi();
                srem(oldJourney, journeyIdString, transaction);
                oldJourney.setFromCity(journey.getFromCity());
                oldJourney.setToCity(journey.getToCity());
                oldJourney.setDepartDate(journey.getDepartDate());
                oldJourney.setType(journey.getType());
                sadd(oldJourney, journeyIdString, transaction);
                transaction.set(journeyKey, JSON.toJSONString(oldJourney));
                transaction.exec();
                res = true;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    @Override
    public boolean deleteJourney(Journey journey) {
        boolean res = false;
        Long journeyId = journey.getJourneyId();
        String journeyKey = getJourneyKey(journeyId);
        try (Jedis jedis = RedisUtil.getJedis()) {
            journey = getJourney(journeyKey, jedis);
            if (journey != null) {
                String journeyIdString = journeyId.toString();
                Transaction transaction = jedis.multi();
                transaction.srem(journeySetKey, journeyIdString);
                transaction.srem(getUserJourneysKey(journey.getUserId()), journeyIdString);
                srem(journey, journeyIdString, transaction);
                transaction.del(journeyKey, JSON.toJSONString(journey));
                transaction.exec();
                res = true;
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return res;
    }

    @Override
    public List<Journey> queryJourneysByUserId(Long userId) {
        List<Journey> journeys = null;
        String userJourneysKey = getUserJourneysKey(userId);
        try (Jedis jedis = RedisUtil.getJedis()) {
            Set<String> journeyIdStrings = jedis.smembers(userJourneysKey);
            journeys = new ArrayList<>(journeyIdStrings.size());
            for (String journeyIdString : journeyIdStrings) {
                Long journeyId = Long.parseLong(journeyIdString);
                String journeyKey = getJourneyKey(journeyId);
                Journey journey = getJourney(journeyKey, jedis);
                if (journey != null)
                    journeys.add(journey);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return journeys;
    }

    @Override
    public List<Journey> queryJourneys(Journey journey) {
        List<Journey> journeys = null;
        List<String> keys = new ArrayList<>();
        String fromCity = journey.getFromCity();
        String toCity = journey.getToCity();
        Timestamp departDate = journey.getDepartDate();
        Short type = journey.getType();
        if (fromCity != null) {
            fromCity = fromCity.trim();
            if (!fromCity.equals("")) keys.add(getFromKey(fromCity));
        }
        if (toCity != null) {
            toCity = toCity.trim();
            if (!toCity.equals("")) keys.add(getToKey(toCity));
        }
        if (type != null) keys.add(getTypeKey(type));
        if (departDate != null) keys.add(getDateKey(departDate));
        if (keys.isEmpty()) keys.add(journeySetKey);
        try (Jedis jedis = RedisUtil.getJedis()) {
            Set<String> journeyIdStrings = jedis.sinter(keys.toArray(new String[0]));
            journeys = new ArrayList<>(journeyIdStrings.size());
            for (String journeyIdString : journeyIdStrings) {
                Long journeyId = Long.parseLong(journeyIdString);
                String journeyKey = getJourneyKey(journeyId);
                journey = getJourney(journeyKey, jedis);
                if (journey != null)
                    journeys.add(journey);
            }
        } catch (Exception e) {
            log.error(e.toString());
        }
        return journeys;
    }

    private void srem(Journey journey, String journeyIdString, Transaction transaction) {
        transaction.srem(getFromKey(journey.getFromCity()), journeyIdString);
        transaction.srem(getToKey(journey.getToCity()), journeyIdString);
        transaction.srem(getDateKey(journey.getDepartDate()), journeyIdString);
        transaction.srem(getTypeKey(journey.getType()), journeyIdString);
    }

    private void sadd(Journey journey, String journeyIdString, Transaction transaction) {
        transaction.sadd(getFromKey(journey.getFromCity()), journeyIdString);
        transaction.sadd(getToKey(journey.getToCity()), journeyIdString);
        transaction.sadd(getDateKey(journey.getDepartDate()), journeyIdString);
        transaction.sadd(getTypeKey(journey.getType()), journeyIdString);
    }
}
