package com.terminal71.ems.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    private String id;
    private String email;
    private String name;

    // getters and setters
}
