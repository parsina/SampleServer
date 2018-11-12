package com.coin.app;

import com.coin.app.service.BackgroundJobsService;
import com.coin.app.service.BitcoinJService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com")
@EnableAutoConfiguration
public class CoinServerApplication
{
    @Autowired
    private BitcoinJService bitcoinJService;

    @Autowired
    private BackgroundJobsService backgroundJobsService;

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
        backgroundJobsService.startJobs();
        return args ->
        {
        };
    }
}
