package com.delivery.auth.oauth2;

import com.delivery.common.exception.AuthException;
import com.delivery.common.security.UserPrincipal;
import com.delivery.user.domain.AuthProvider;
import com.delivery.user.domain.Role;
import com.delivery.user.domain.User;
import com.delivery.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!"google".equalsIgnoreCase(registrationId)) {
            throw new AuthException("OAuth2 provider not supported: " + registrationId);
        }

        OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());
        User user = userRepository.findByEmail(userInfo.getEmail())
                .map(existing -> updateExisting(existing, userInfo))
                .orElseGet(() -> registerNew(userInfo));

        return UserPrincipal.fromOAuth2(user, oAuth2User.getAttributes());
    }

    private User registerNew(OAuth2UserInfo info) {
        User user = User.builder()
                .email(info.getEmail())
                .name(info.getName())
                .role(Role.ROLE_CUSTOMER)
                .provider(AuthProvider.GOOGLE)
                .providerId(info.getId())
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private User updateExisting(User existing, OAuth2UserInfo info) {
        existing.setName(info.getName());
        existing.setProviderId(info.getId());
        return userRepository.save(existing);
    }
}
