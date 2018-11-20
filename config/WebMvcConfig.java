package com.coin.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    @Value("${client.url}")
    private String appUrl;

    @Override
    public void addCorsMappings(CorsRegistry registry)
    {
        registry.addMapping("/**")
                .allowedOrigins(appUrl.split(","))
                .allowedMethods("HEAD", "OPTIONS", "GET", "POST")
                .allowCredentials(true)
                .maxAge(3600);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
        System.out.println( "<<<<< <<<< <<< << < CORS Origins > >> >>> >>>> >>>>> : " + appUrl);
    }


}
