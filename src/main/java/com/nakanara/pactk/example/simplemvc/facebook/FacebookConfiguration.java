package com.nakanara.pactk.example.simplemvc.facebook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.web.SignInAdapter;

@Configuration @EnableSocial
//@EnableConfigurationProperties(FacebookProperties.class)
public class FacebookConfiguration extends SocialConfigurerAdapter{

    @Autowired
    private EnhancedFacebookProperties properties;


    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return super.getUsersConnectionRepository(connectionFactoryLocator);
    }
}