package com.example.UserAuthService.dtos;

import com.example.UserAuthService.models.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private String name;
    private String email;
//    private String password;

    public static UserDto from(User user) {

        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
