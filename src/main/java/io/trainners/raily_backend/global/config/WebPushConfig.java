package io.trainners.raily_backend.global.config;

import nl.martijndwars.webpush.PushService;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.GeneralSecurityException;
import java.security.Security;

@Configuration
@EnableConfigurationProperties(WebPushProperties.class)
public class WebPushConfig {
    @Bean
    public PushService pushService(WebPushProperties props) throws GeneralSecurityException {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        return new PushService(props.publicKey(), props.privateKey(), props.subject());
    }
}
