package com.example.UserAuthService.security.models;

import com.example.UserAuthService.models.Role;
import org.springframework.security.core.GrantedAuthority;

public class CustomGrantedAuthority implements GrantedAuthority {

    private final String authority;



    public CustomGrantedAuthority(Role role) {
        this.authority = role.getValue() + "";
    }


    @Override
    public String getAuthority() {
        return authority;
    }
}
