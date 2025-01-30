package com.pdf.ist2.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

}

    // @Bean
    // public ApplicationRunner testEmailSender(JavaMailSender mailSender) {
    //     return args -> {
    //         try {
    //             SimpleMailMessage message = new SimpleMailMessage();
    //             message.setTo("jvmamedebr@gmail.com");
    //             message.setSubject("Teste de Envio de E-mail");
    //             message.setText("Este é um teste de envio de e-mail.");
    //             mailSender.send(message);
    //             logger.info("E-mail de teste enviado com sucesso.");
    //         } catch (Exception e) {
    //             logger.error("Falha no envio do e-mail de teste", e);
    //         }
    //     };
    // }
