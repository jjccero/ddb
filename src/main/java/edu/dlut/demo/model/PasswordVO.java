package edu.dlut.demo.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordVO {
    private Long userId;
    private String oldPassword;
    private String password;
}
