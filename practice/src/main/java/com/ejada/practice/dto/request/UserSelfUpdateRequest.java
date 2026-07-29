package com.ejada.practice.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSelfUpdateRequest {

    @Email
    private String email;

    private String firstName;

    private String lastName;
}
