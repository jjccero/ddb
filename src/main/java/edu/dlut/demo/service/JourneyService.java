package edu.dlut.demo.service;


import edu.dlut.demo.model.Journey;
import edu.dlut.demo.model.JourneyVO;

import java.util.List;

public interface JourneyService {
    long insertJourney(Journey journey);

    boolean updateJourney(Journey journey);

    boolean deleteJourney(Journey journey);

    List<Journey> queryJourneysByUserId(Long userId);

    List<JourneyVO> queryJourneys(Journey journey);
}
