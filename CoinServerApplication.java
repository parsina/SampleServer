package com.coin.app;

import com.coin.app.service.BitcoinJService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com")
@EnableAutoConfiguration
public class CoinServerApplication
{
    @Autowired
    private BitcoinJService bitcoinJService;

    public static void main(String[] args)
    {
        //Prints banner to System.out
        SpringApplicationBuilder sa = new SpringApplicationBuilder().bannerMode(Banner.Mode.CONSOLE);
        sa.sources(CoinServerApplication.class);
        sa.run(args);
    }

    @Bean
    ApplicationRunner init()
    {
        bitcoinJService.initialize();
        return args ->
        {
        };
    }
}





//// To deploy to external server (Tomcat)
//// 1. Comment previous CoinServerApplication class and uncomment following one
//// 2. Uncomment <dependency> spring-boot-starter-tomcat in pom.xml
//// 3. In terminal type 'mvn clean package' to create war file in target
//// 4. Copy the war file to external tomcat

//@SpringBootApplication
//@ComponentScan("com")
//@EnableAutoConfiguration
//public class CoinServerApplication extends SpringBootServletInitializer
//{
//    @Autowired
//    private BitcoinJService bitcoinJService;
//
//    public static void main(String[] args)
//    {
//        SpringApplication.run(CoinServerApplication.class, args);
//    }
//
//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
//        return application.sources(CoinServerApplication.class);
//    }
//
//    @Bean
//    ApplicationRunner init()
//    {
//        bitcoinJService.initialize();
//        return args ->
//        {
//        };
//    }
//}
