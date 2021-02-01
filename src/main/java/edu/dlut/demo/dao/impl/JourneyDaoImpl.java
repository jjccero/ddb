package edu.dlut.demo.dao.impl;

import com.alibaba.fastjson.JSON;
import edu.dlut.demo.dao.JourneyDao;
import edu.dlut.demo.model.Journey;
import edu.dlut.demo.util.ShardUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.ShardedJedis;

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

    private Journey getJourney(String journeyKey, ShardedJedis jedis) {
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
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            Long journeyId = jedis.incr("currentJourneyId");
            if (journeyId > 0L) {
                journey.setJourneyId(journeyId);
                String journeyKey = getJourneyKey(journeyId);
                String journeyIdString = journeyId.toString();
                jedis.set(journeyKey, JSON.toJSONString(journey));
                jedis.sadd(journeySetKey, journeyIdString);
                jedis.sadd(getUserJourneysKey(journey.getUserId()), journeyIdString);
                sadd(journey, journeyIdString, jedis);
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
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            Journey oldJourney = getJourney(journeyKey, jedis);
            if (oldJourney != null) {
                String journeyIdString = journeyId.toString();
                srem(oldJourney, journeyIdString, jedis);
                oldJourney.setFromCity(journey.getFromCity());
                oldJourney.setToCity(journey.getToCity());
                oldJourney.setDepartDate(journey.getDepartDate());
                oldJourney.setType(journey.getType());
                sadd(oldJourney, journeyIdString, jedis);
                jedis.set(journeyKey, JSON.toJSONString(oldJourney));
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
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            journey = getJourney(journeyKey, jedis);
            if (journey != null) {
                String journeyIdString = journeyId.toString();
                jedis.srem(journeySetKey, journeyIdString);
                jedis.srem(getUserJourneysKey(journey.getUserId()), journeyIdString);
                srem(journey, journeyIdString, jedis);
                jedis.del(journeyKey);
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
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
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
        try (ShardedJedis jedis = ShardUtil.getJedis()) {
            Set<String> journeyIdStrings = null;
            for (String key : keys) {
                Set<String> smembers = jedis.smembers(key);
                if (journeyIdStrings == null) {
                    journeyIdStrings = smembers;
                } else {
                    journeyIdStrings.retainAll(jedis.smembers(key));
                }
            }
            assert journeyIdStrings != null;
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

    private void srem(Journey journey, String journeyIdString, ShardedJedis jedis) {
        jedis.srem(getFromKey(journey.getFromCity()), journeyIdString);
        jedis.srem(getToKey(journey.getToCity()), journeyIdString);
        jedis.srem(getDateKey(journey.getDepartDate()), journeyIdString);
        jedis.srem(getTypeKey(journey.getType()), journeyIdString);
    }

    private void sadd(Journey journey, String journeyIdString, ShardedJedis jedis) {
        jedis.sadd(getFromKey(journey.getFromCity()), journeyIdString);
        jedis.sadd(getToKey(journey.getToCity()), journeyIdString);
        jedis.sadd(getDateKey(journey.getDepartDate()), journeyIdString);
        jedis.sadd(getTypeKey(journey.getType()), journeyIdString);
    }
}
