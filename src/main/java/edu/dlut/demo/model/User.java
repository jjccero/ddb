package edu.dlut.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class User {
    private Long userId;
    private String username;
    private String password;
    private String contact;
    private Long gmtCreate;
    private Short sex;
    private String sessionId;
}
