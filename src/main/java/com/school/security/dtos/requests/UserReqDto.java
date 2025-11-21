package com.school.security.dtos.requests;

import com.school.security.enums.Gender;

public record UserReqDto(
        String firstname,
        String lastname,
        String email,
        String password,
        Gender gender) {}
