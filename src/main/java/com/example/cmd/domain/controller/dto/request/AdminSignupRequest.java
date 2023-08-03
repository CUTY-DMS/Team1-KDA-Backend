package com.example.cmd.domain.controller.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@NoArgsConstructor
public class AdminSignupRequest {

    @NotNull
    private String email;

    @NotNull
    private Long teachGrade;

    @NotNull
    private Long teachClass;

    @NotNull
    private String name;

    @Pattern(regexp = "^(?=.*[!@#$%^&*])(?=.{1,20}$).*",
            message = "비밀번호는 최대 20글자이고, 특수문자 1개 이상이 포함되어야 합니다.")
    private String password;

    @NotNull
    private Long birth;

    @NotNull
    private String code;
}
