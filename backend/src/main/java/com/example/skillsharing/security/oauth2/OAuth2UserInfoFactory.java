package com.example.skillsharing.security.oauth2;

import com.example.skillsharing.exception.BadRequestException;
import com.example.skillsharing.model.User.AuthProvider;

import java.util.Map;

public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new BadRequestException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
