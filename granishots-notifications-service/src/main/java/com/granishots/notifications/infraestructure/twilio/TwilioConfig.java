package com.granishots.notifications.infraestructure.twilio;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class TwilioConfig {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void initTwilio() {
        try {
            Twilio.init(accountSid, authToken);
            log.info("✅ Twilio SDK inicializado correctamente. Account SID: {}...", accountSid.substring(0, 8));
        } catch (Exception e) {
            log.error("❌ Error al inicializar Twilio SDK: {}", e.getMessage());
        }
    }
}
