package com.coin.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com")
public class CoinServerApplication
{
//    @Autowired
//    BitcoinJService bitcoinJService;
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    AccountService accountService;

//    @GetMapping("/user")
//    @ResponseBody
//    public Principal user(Principal user)
//    {
//        return user;
//    }

//    @GetMapping("/resource")
//    @ResponseBody
//    public Map<String, Object> home()
//    {
//        Map<String, Object> model = new HashMap<String, Object>();
//        model.put("id", UUID.randomUUID().toString());
//        model.put("content", "Hello World");
//        return model;
//    }

//    @GetMapping(value = "/{path:[^\\.]*}")
//    public String redirect()
//    {
//        return "forward:/";
//    }


    public static void main(String[] args)
    {
        SpringApplication.run(CoinServerApplication.class, args);
    }

//    @Configuration
//    @Order(SecurityProperties.DEFAULT_FILTER_ORDER)
//    protected static class SecurityConfiguration extends WebSecurityConfigurerAdapter
//    {
//        @Override
//        protected void configure(HttpSecurity http) throws Exception
//        {
//            // @formatter:off
//            http
//                    .httpBasic().and()
//                    .authorizeRequests()
//                    .antMatchers("/index.html", "/", "/home", "/login").permitAll()
//                    .anyRequest().authenticated()
//                    .and()
//                    .csrf()
//                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
//            // @formatter:on
//        }
//    }

//    @Bean
//    ApplicationRunner init()
//    {
//        bitcoinJService.initialize();
//
//        User user = userService.createUser("User", "01", "user01@coinnet.net", "123123");
//        if(user.getStatus().equals(UserStatus.INACTIVE))
//        {
//            user.setAccount(accountService.createAccount(user));
//            userService.activateUser(user);
//        }
//        else
//            bitcoinJService.initializeWallet(user);
//
//        bitcoinJService.startCoinReceiveListener(user.getAccount());

//        binanceAPI();


//        return args ->
//        {
//            Stream.of("Ferrari", "Jaguar", "Porsche", "Lamborghini", "Bugatti",
//                    "AMC Gremlin", "Triumph Stag", "Ford Pinto", "Yugo GV").forEach(name ->
//            {
//                Car car = new Car();
//                car.setName(name);
////                repository.save(car);
//            });
//            repository.findAll().forEach(System.out::println);
//        };
//    }

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
