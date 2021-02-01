package edu.dlut.demo.service.impl;


import edu.dlut.demo.dao.JourneyDao;
import edu.dlut.demo.model.Journey;
import edu.dlut.demo.model.JourneyVO;
import edu.dlut.demo.service.JourneyService;
import edu.dlut.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JourneyServiceImpl implements JourneyService {
    @Autowired
    JourneyDao journeyDao;
    @Autowired
    UserService userService;

    @Override
    public long insertJourney(Journey journey) {
        return journeyDao.insertJourney(journey);
    }

    @Override
    public boolean updateJourney(Journey journey) {
        return journeyDao.updateJourney(journey);
    }

    @Override
    public boolean deleteJourney(Journey journey) {
        return journeyDao.deleteJourney(journey);
    }

    @Override
    public List<Journey> queryJourneysByUserId(Long userId) {
        return journeyDao.queryJourneysByUserId(userId);
    }

    @Override
    public List<JourneyVO> queryJourneys(Journey journey) {
        List<Journey> journeys = journeyDao.queryJourneys(journey);
        List<JourneyVO> journeyVOs = new ArrayList<>(journeys.size());
        for (Journey j : journeys) {
            JourneyVO journeyVO = new JourneyVO();
            journeyVO.setJourney(j);
            journeyVO.setUserVO(userService.getUser(j.getUserId()));
            journeyVOs.add(journeyVO);
        }
        return journeyVOs;
    }
}
