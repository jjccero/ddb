package edu.dlut.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
public class Journey {
    private Long journeyId;
    private Long userId;
    private Timestamp departDate;
    private Long gmtCreate;
    private String fromCity;
    private String toCity;
    private String note;
    private Short type;
}
