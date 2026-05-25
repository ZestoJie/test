package com.terminal71.ems.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    private String id;
    private String email;
    private String name;

    // getters and setters
}
