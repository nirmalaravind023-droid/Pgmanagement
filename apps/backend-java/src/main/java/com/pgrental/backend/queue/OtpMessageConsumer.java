package com.pgrental.backend.queue;

import com.pgrental.backend.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class OtpMessageConsumer {

    @Value("${sms.provider:console}")
    private String smsProvider;

    @Value("${twilio.account.sid:}")
    private String twilioSid;

    @Value("${twilio.auth.token:}")
    private String twilioToken;

    @Value("${twilio.from.number:}")
    private String twilioFrom;

    @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
    public void consumeOtpMessage(Map<String, String> message) {
        String phone = message.get("phone");
        String otp = message.get("otp");

        if ("twilio".equalsIgnoreCase(smsProvider) && twilioSid != null && !twilioSid.isEmpty()) {
            sendViaTwilio(phone, otp);
        } else {
            log.info("[OTP] Phone: {} | OTP: {} (console mode — set SMS_PROVIDER=twilio to enable SMS)", phone, otp);
        }
    }

    private void sendViaTwilio(String phone, String otp) {
        // Wire Twilio SDK here when TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_FROM_NUMBER are set
        // com.twilio.Twilio.init(twilioSid, twilioToken);
        // com.twilio.rest.api.v2010.account.Message.creator(
        //     new com.twilio.type.PhoneNumber(phone),
        //     new com.twilio.type.PhoneNumber(twilioFrom),
        //     "Your PG Rental OTP is: " + otp
        // ).create();
        log.info("[OTP] Twilio stub — would send OTP {} to {}", otp, phone);
    }
}
