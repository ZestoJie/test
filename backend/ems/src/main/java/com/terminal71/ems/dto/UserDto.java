package com.terminal71.ems.dto;

public class UserDto {
    private Long id;
    private String name;
    private String role;
    private int stars;

    public UserDto() {}

    public UserDto(Long id, String name, String role, int stars) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.stars = stars;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }
}
