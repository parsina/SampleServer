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
//        SpringApplication.run(CoinServerApplication.class, args);

        //SpringApplicationBuilder() returns an ApplicationContext, but creating the variable is optional

        SpringApplicationBuilder sa = new SpringApplicationBuilder().bannerMode(Banner.Mode.CONSOLE); //Prints banner to System.out

//        new SpringApplicationBuilder().bannerMode(Banner.Mode.LOG); //Prints banner to the log file
//
//        new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF); //Disables the banner



        //Now, if you need to do something else with the ApplicationContext, you can (such as print all the beans, etc.)

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

//    private void binanceAPI()
//    {
//        String apiKey = "zg9qGIEnCYQ2MYZ1a8J2y1feUK6vJNyCuP2iee60jSK6TMgSFC3HCKLGlLsGf0F0";
//        String secretKey = "yDQjg1vhfC27svv9LjU7FRugeA8j8GVdy0EYaN5qjH867HdDJjnEcfnI9emC8dl6";
//
//        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secretKey);
//        BinanceApiRestClient client = factory.newRestClient();
//        client.ping(); // Test connectivity
//
////        testAPI(client);
//
//        //Get account balances
//        com.binance.api.client.domain.account.Account account = client.getAccount();
//        System.out.println(account.getAssetBalance("DENT").getFree());
//        System.out.println(account.getAssetBalance("BTC").getFree());
//
//        // Latest price of a symbol
//        TickerStatistics tickerStatistics = client.get24HrPriceStatistics("DENTBTC");
//        System.out.println("Ask : " + tickerStatistics.getAskPrice() + " ---> " + String.format("%.10f", Double.valueOf(tickerStatistics.getAskPrice()) * 1.01));
//        System.out.println("Bid : " + tickerStatistics.getBidPrice() + " ---> " + String.format("%.10f", Double.valueOf(tickerStatistics.getBidPrice()) * 0.99));
//        System.out.println("Lates : " + tickerStatistics.getAskPrice());
//    }
}
