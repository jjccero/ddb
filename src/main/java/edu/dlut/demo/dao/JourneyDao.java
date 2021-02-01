package edu.dlut.demo.dao;

import edu.dlut.demo.model.Journey;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface JourneyDao {
    long insertJourney(Journey journey);

    boolean updateJourney(Journey journey);

    boolean deleteJourney(Journey journey);

    List<Journey> queryJourneysByUserId(Long userId);

    List<Journey> queryJourneys(Journey journey);
}
