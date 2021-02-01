package edu.dlut.demo.controller;


import edu.dlut.demo.common.UserContext;
import edu.dlut.demo.common.UserException;
import edu.dlut.demo.model.Journey;
import edu.dlut.demo.model.JourneyVO;
import edu.dlut.demo.service.JourneyService;
import edu.dlut.demo.util.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class JourneyController {
    @Autowired
    JourneyService journeyService;
    @Autowired
    UserContext userContext;

    @PostMapping("/insertJourney")
    public long insertJourney(@RequestBody Journey journey) throws UserException {
        journey.setUserId(userContext.getUserId(true));
        journey.setGmtCreate(TimeUtil.getGmtCreate());
        return journeyService.insertJourney(journey);
    }

    @PostMapping("/updateJourney")
    public boolean updateJourney(@RequestBody Journey journey) throws UserException {
        journey.setUserId(userContext.getUserId(true));
        return journeyService.updateJourney(journey);
    }

    @PostMapping("/deleteJourney")
    public boolean deleteJourney(@RequestBody Journey journey) throws UserException {
        journey.setUserId(userContext.getUserId(true));
        return journeyService.deleteJourney(journey);
    }

    @GetMapping("/queryJourneysByUserId")
    public List<Journey> queryJourneysByUserId() throws UserException {
        return journeyService.queryJourneysByUserId(userContext.getUserId(true));
    }

    @PostMapping("/queryJourneys")
    public List<JourneyVO> queryJourneys(@RequestBody Journey journey) {
        return journeyService.queryJourneys(journey);
    }
}
