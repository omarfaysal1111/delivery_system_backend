package com.delivery.auth.oauth2;

import java.util.Map;

public interface OAuth2UserInfo {
    String getId();
    String getName();
    String getEmail();
    Map<String, Object> getAttributes();
}
