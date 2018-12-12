package com.coin.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.User;
import com.coin.app.model.enums.AccountStatus;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.coin.app.repository.UserRepository;
import com.coin.app.security.JwtTokenProvider;
import com.coin.app.security.UserPrincipal;
import com.coin.app.service.mail.EmailService;
import com.coin.app.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BitcoinJService bitcoinJService;

    @Autowired
    private AccountService accountService;

    @Override
    public ResultData createUser(String username, String email, String password, String repeatedPassword)
    {
        ResultData result = new ResultData(false, "");

        boolean emailIsValid = Validator.isValidEmailAddress(email);
        boolean passworIsValid = Validator.isValidPassword(password);
        boolean passwordMatched = password.equals(repeatedPassword);

        if (!emailIsValid)
            result.setMessage("ایمیل صحیح نمی باشد.");
        else if (!passworIsValid)
            result.setMessage("پسوورد صحیح نمی باشد.");
        if (!passwordMatched)
            result.setMessage("پسوورد و تکرار آن یکسان نمی باشد.");
        if(username.length() < 4)
            result.setMessage("نام کاربری باید حداقل 4 کاراکتر باشد.");

        if (emailIsValid && passworIsValid && passwordMatched && username.length() > 3)
        {
            User user = userRepository.findByUsername(username);
            if (user != null)
            {
                result.setSuccess(false);
                result.setMessage("نام کاربری " + username + " قبلا ثبت گردیده. لطفا از یک نام کاربری دیگر استفاده نمایید.");
                return result;
            }
            else
            if(isReservedUsername(username))
            {
                result.setSuccess(false);
                result.setMessage("نام کاربری " + username + " رزرو شده است. لطفا از یک نام کاربری دیگر استفاده نمایید.");
                return result;
            }

            user = userRepository.findByEmail(email.toLowerCase());
            if (user != null && !user.getStatus().equals(UserStatus.INVITED))
            {
                result.setSuccess(false); // Should be removed
                result.setMessage("ایمیل  " + email + " فبلا در سیستم ثبت نام کرده است.");
                result.addProperty("userEmail", user.getEmail()); // Should be removed
                return result;
            } else if (user != null && user.getStatus().equals(UserStatus.INVITED))
            {
                user.setCreatedDate(new Date());
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setStatus(UserStatus.INACTIVE);
                user.setConfirmationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                result.setSuccess(true);
                result.setMessage("");
                result.addProperty("userEmail", user.getEmail());
            } else
            {
                user = new User();
                user.setCreatedDate(new Date());
                user.setParentId(-1L);
                user.setEmail(email.toLowerCase());
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setStatus(UserStatus.INACTIVE);
                user.setRole(UserRole.ROLE_USER);
                user.setConfirmationToken(UUID.randomUUID().toString());
                userRepository.save(user);
                result.setSuccess(true);
                result.setMessage("");
                result.addProperty("userEmail", user.getEmail());
            }
        }
        return result;
    }

    public boolean isReservedUsername(String username)
    {
        List<String> reservedKeys = Arrays.asList("about", "abuse", "access", "account", "accounts", "address", "manager", "admin", "admindashboard", "administration", "administrator", "administrators", "admins", "adsense", "adult", "advertising", "adwords", "affiliate",
                "affiliates", "ajax", "analytics", "android", "anon", "anonymous", "api1", "api2", "api3", "apps", "archive", "assets", "assets1", "assets2", "assets3", "assets4", "assets5", "atom", "auth", "authentication", "avatar", "backup", "banner", "banners", "beta",
                "billing", "billings", "blog", "blogs", "board", "bots", "business", "cache", "cadastro", "calendar", "campaign", "careers", "chat", "client", "cliente", "clients", "cname", "code", "comercial", "community", "compare", "compras", "config", "connect", "contact",
                "contest", "copyright", "cpanel", "create", "css1", "css2", "css3", "dashboard", "data", "delete", "demo", "design", "designer", "devel", "developer", "developers", "development", "directory", "docs", "domain", "donate", "download", "downloads", "ecommerce",
                "edit", "editor", "email", "e-mail", "example", "favorite", "feed", "feedback", "feeds", "file", "files", "flog", "follow", "forum", "forums", "free", "gadget", "gadgets", "games", "gettingstarted", "graph", "graphs", "group", "groups", "guest", "help", "home",
                "homepage", "host", "hosting", "hostmaster", "hostname", "html", "http", "httpd", "https", "image", "images", "imap", "img1", "img2", "img3", "inbox", "index", "indice", "info", "information", "intranet", "invite", "invoice", "invoices", "ipad", "iphone", "jabber",
                "jars", "java", "javascript", "jobs", "knowledgebase", "launchpad", "legal", "list", "lists", "login", "logout", "logs", "mail", "mail1", "mail2", "mail3", "mail4", "mail5", "mailer", "mailing", "main", "manage", "manager", "marketing", "master", "media", "message",
                "messages", "messenger", "microblog", "microblogs", "mine", "mobile", "movie", "movies", "music", "musicas", "mysql", "name", "named", "network", "networks", "news", "newsite", "newsletter", "nick", "nickname", "notes", "noticias", "official", "online", "operator",
                "order", "orders", "page", "pager", "pages", "panel", "partner", "partnerpage", "partners", "password", "payment", "payments", "perl", "photo", "photoalbum", "photos", "pics", "picture", "pictures", "plugin", "plugins", "policy", "pop3", "popular", "portal", "post",
                "postfix", "postmaster", "posts", "press", "privacy", "private", "profile", "project", "projects", "promo", "public", "python", "random", "redirect", "register", "registration", "resolver", "root", "ruby", "sale", "sales", "sample", "samples", "sandbox", "script",
                "scripts", "search", "secure", "security", "send", "server", "servers", "service", "setting", "settings", "setup", "shop", "signin", "signup", "site", "sitemap", "sitenews", "sites", "smtp", "soporte", "sorry", "staff", "stage", "staging", "start", "stat", "static",
                "statistics", "stats", "status", "store", "stores", "subdomain", "subscribe", "suporte", "support", "survey", "surveys", "system", "tablet", "tablets", "talk", "task", "tasks", "teams", "tech", "telnet", "theme","themes", "todo", "tools", "trac", "translate", "update",
                "upload", "uploads", "usage", "user", "username", "usernames", "users", "usuario", "validation", "validations", "vendas", "video", "videos", "visitor", "webdisk", "webmail", "webmaster", "website", "websites", "whois", "wiki", "workshop", "www", "www1", "www2", "www3",
                "www4", "www5", "www6", "www7", "bigbet", "bigbit", "bitbet", "bigbitbet", "wwws", "wwww", "yourdomain", "yourname", "yoursite", "yourusername", "git", "svn", "ftp", "ssl", "qa", "ideas", "cdn", "cdn1", "cdn2", "cdn3", "cdn4", "cdn5", "cdn6", "cdn7", "cdn8", "cdn9", "mx", "css", "my", "updates", "pay",
                "rss", "mygroup", "mygroups", "upgrade", "myteam", "myteams", "log-in", "log-out", "refer", "referral", "product", "products", "cms", "faq", "faqs", "firewall", "gallery", "member", "members", "report", "reports", "course", "courses", "training", "marketplace", "scorm",
                "myprofile", "people", "ssh", "browse", "uptime", "content", "go1", "archives", "test", "test1", "test2", "test3", "test4", "test5", "trial", "tour", "pricing", "packages", "releasenotes", "changelog", "event", "events", "stories", "integrations",
                "platform", "styleguide", "kb", "care", "communities");
        return reservedKeys.contains(username.trim().toLowerCase());
    }

    public ResultData confirmRegistration(String token)
    {
        ResultData result = new ResultData(false, "");

        User user = userRepository.findByConfirmationToken(token);
        if (user != null)
        {
            if (user.getStatus().equals(UserStatus.ACTIVE))
            {
                result.setMessage("این حساب کاربری قبلا فعال شده است.");
                return result;
            }
            while (user.getAccount() == null)
                user.setAccount(accountService.createAccount(user));
            user.setStatus(UserStatus.ACTIVE);
            user.getAccount().setStatus(AccountStatus.ACTIVE);
            userRepository.save(user);
            result.setSuccess(true);
            result.setMessage("");
            result.addProperty("id", user.getId());
            result.addProperty("username", user.getUsername());
            result.addProperty("email", user.getEmail());
        }
        return result;
    }

    @Override
    public ResultData login(String username, String password)
    {
        ResultData result = new ResultData(false, "");
        User user = userRepository.findByEmail(username);
        if (user == null)
            user = userRepository.findByEmail(username);
        if (user == null)
        {
            result.setMessage("ایمیل صحیح نمی باشد.");
            result.addProperty("reason","EMAIL_NOT_FOUND");
            return result;
        } else if (user.getStatus().equals(UserStatus.INACTIVE) || user.getStatus().equals(UserStatus.INVITED))
        {
            result.setMessage("حساب کاربری غیر فعال است.");
            result.addProperty("reason","INVITED_USER");
            return result;
        } else if (user.getStatus().equals(UserStatus.DELETED))
        {
            result.setMessage("کاربر حذف شده است.");
            result.addProperty("reason","DELETED_USER");
            return result;
        } else if (!passwordEncoder.matches(password, user.getPassword()))
        {
            result.setMessage("کلمه عبور صحیح نمی باشد.");
            result.addProperty("reason","PASSWORD_INCORRECT");
            return result;
        } else if (user.getStatus().equals(UserStatus.ACTIVE))
        {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.generateToken(authentication);
//            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));

            result.setSuccess(true);
            result.setMessage("User loged in !");
            result.addProperty("id", user.getId());
            result.addProperty("username", user.getUsername());
            result.addProperty("email", user.getEmail());
            result.addProperty("role", user.getRole());
            result.addProperty("info", user.getUserInfo());
            result.addProperty("token", jwt);
        }
        return result;
    }

    public User findByConfirmationToken(String confirmationToken)
    {
        return userRepository.findByConfirmationToken(confirmationToken);
    }

    @Override
    public boolean isAuthenticated(Long userId)
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userId == null ? auth.isAuthenticated() : auth.isAuthenticated() && ((UserPrincipal) auth.getPrincipal()).getId().equals(userId);
    }

    @Override
    public User getCurrentUser()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.isAuthenticated() ? userRepository.findById(((UserPrincipal) auth.getPrincipal()).getId()).get() : null;
    }

    @Override
    public ResultData sendInvitations(List<String> allEmails)
    {
        List<String> emails = new ArrayList<>();
        for (String email: allEmails)
            if(!emails.contains(email.trim().toLowerCase()))
                emails.add(email.trim().toLowerCase());


        List<String> validEmails = new ArrayList<>();
        List<String> existEmails = new ArrayList<>();
        List<String> wrongEmails = new ArrayList<>();

        for (String email : emails)
        {
            if (Validator.isValidEmailAddress(email))
            {
                if (userRepository.findByEmail(email) == null)
                {
                    User user = new User();
                    user.setCreatedDate(new Date());
                    user.setParentId(getCurrentUser().getId());
                    user.setEmail(email.toLowerCase());
                    user.setUsername("InvitedUser-" + email);
                    user.setPassword(passwordEncoder.encode("123123"));
                    user.setStatus(UserStatus.INVITED);
                    user.setRole(UserRole.ROLE_USER);
                    user.setConfirmationToken(UUID.randomUUID().toString());
                    userRepository.save(user);
                    emailService.sendInvitationEmail(email);
                    validEmails.add(email);
                } else
                    existEmails.add(email);
            } else
                wrongEmails.add(email);
        }

        ResultData resultData = new ResultData(true, "");
        resultData.addProperty("validEmails", validEmails);
        resultData.addProperty("existEmails", existEmails);
        resultData.addProperty("wrongEmails", wrongEmails);

        return resultData;
    }

    @Override
    public User findByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByUserName(String username)
    {
        return userRepository.findByUsername(username);
    }

    @Override
    public User findById(Long id)
    {
        return userRepository.findById(id).get();
    }

    @Override
    public User saveUser(User user)
    {
        return userRepository.save(user);
    }

    @Override
    public boolean checkLoggedInUser(String email, String password)
    {
        return passwordEncoder.matches(password, userRepository.findByEmail(email).getPassword());
    }

    @Override
    public ResultData changeUserPassword(String password)
    {
        if (!Validator.isValidPassword(password))
            return new ResultData(false, "putNewPassErr");
        User user = getCurrentUser();
        if(passwordEncoder.matches(password, user.getPassword()))
            return new ResultData(false, "equalNewOld");

        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return new ResultData(true, "");
    }

    @Override
    public ResultData forgotPassword(String email)
    {
        User user = userRepository.findByEmail(email);
        if(user == null)
            return new ResultData(false, "ایمیل وارد شده صحیح نمی باشد و یا در سیستم ثبت نشده است. لطفا ایمیل وارد شده را مجددا بررسی نمایید.");
        else
        {
            String[] passwordArray = UUID.randomUUID().toString().split("-");
            String password = "";
            for(int i = 0; i < passwordArray.length; i++)
                password = password.concat(i%2 == 0 ? passwordArray[i].toUpperCase() : passwordArray[i]);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            emailService.sendNewPassword(user.getEmail(), password);
            return new ResultData(true, "");
        }
    }
}
