package com.whq.mvc.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Date;

@Data
public class User {
    private Integer id;

    private String username;

    private String password;

    private String gender;

    private Date birthday;

    private String description;
}