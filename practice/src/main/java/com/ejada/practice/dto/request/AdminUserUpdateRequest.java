package com.ejada.practice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class AdminUserUpdateRequest {

    @Email
    private String email;

    private String firstName;

    private String lastName;

    private Set<String> roles;
}
