package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.Bitrix;
import com.coin.app.model.BitrixTransaction;
import com.coin.app.model.enums.BitrixType;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.coin.app.repository.BitrixRepository;
import com.coin.app.repository.BitrixTransactionRepository;
import com.coin.app.security.JwtTokenProvider;
import com.coin.app.security.UserPrincipal;
import com.coin.app.service.mail.EmailService;
import com.coin.app.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class BitrixServiceImpl implements BitrixService
{
    @Value("${client.url}")
    private String appUrl;

    @Autowired
    private BitrixRepository bitrixRepository;

    @Autowired
    private BitrixTransactionRepository bitrixTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BitcoinJService bitcoinJService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Job testJob;

    @Override
    public void initializeDataBase()
    {
        if (getAdmin() == null)
        {
            Bitrix wallet = new Bitrix();
            wallet.setAddress(bitcoinJService.getNewWalletAddress());
            wallet.setBalance(bitcoinJService.getWalletBalance());
            wallet.setCreatedDate(new Date());
            wallet.setEmail("baron.alex1980+wallet@gmail.com");
            wallet.setNodes(0);
            wallet.setParentId(-3L);
            wallet.setPassword(passwordEncoder.encode("Qwerty123"));
            wallet.setPlan(BitrixType.STARTER);
            wallet.setReferee(UUID.randomUUID().toString().replace("-", ""));
            wallet.setRole(UserRole.ROLE_USER);
            wallet.setStatus(UserStatus.ACTIVE);
            wallet.setStep(0);
            wallet.setUsername("Wallet");
            wallet.setToken(UUID.randomUUID().toString().replace("-", ""));
            wallet.setReference("BitRix");
            bitrixRepository.save(wallet);


            Bitrix fund = new Bitrix();
            fund.setAddress(bitcoinJService.getNewWalletAddress());
            fund.setBalance(0L);
            fund.setCreatedDate(new Date());
            fund.setEmail("baron.alex1980+fund@gmail.com");
            fund.setNodes(0);
            fund.setParentId(-2L);
            fund.setPassword(passwordEncoder.encode("Qwerty123"));
            fund.setPlan(BitrixType.STARTER);
            fund.setReferee(UUID.randomUUID().toString().replace("-", ""));
            fund.setRole(UserRole.ROLE_USER);
            fund.setStatus(UserStatus.ACTIVE);
            fund.setStep(0);
            fund.setUsername("Fund");
            fund.setToken(UUID.randomUUID().toString().replace("-", ""));
            fund.setReference("BitRix");
            bitrixRepository.save(fund);

            Bitrix admin = new Bitrix();
            admin.setAddress(bitcoinJService.getNewWalletAddress());
            admin.setBalance(0L);
            admin.setCreatedDate(new Date());
            admin.setEmail("baron.alex1980+admin@gmail.com");
            admin.setNodes(6);
            admin.setParentId(-1L);
            admin.setPassword(passwordEncoder.encode("Qwerty123"));
            admin.setPlan(BitrixType.STARTER);
            admin.setReferee(UUID.randomUUID().toString().replace("-", ""));
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setStep(0);
            admin.setUsername("Admin");
            admin.setToken(UUID.randomUUID().toString().replace("-", ""));
            admin.setReference("BitRix");
            admin = bitrixRepository.save(admin);

            Bitrix firstMaster = new Bitrix();
            firstMaster.setAddress(bitcoinJService.getNewWalletAddress());
            firstMaster.setBalance(0L);
            firstMaster.setCreatedDate(new Date());
            firstMaster.setEmail("baron.alex1980+firstMaster@gmail.com");
            firstMaster.setNodes(2);
            firstMaster.setParentId(admin.getId());
            firstMaster.setPassword(passwordEncoder.encode("Qwerty123"));
            firstMaster.setPlan(BitrixType.STARTER);
            firstMaster.setReferee(UUID.randomUUID().toString().replace("-", ""));
            firstMaster.setRole(UserRole.ROLE_USER);
            firstMaster.setStatus(UserStatus.ACTIVE);
            firstMaster.setStep(0);
            firstMaster.setUsername("First Master");
            firstMaster.setToken(UUID.randomUUID().toString().replace("-", ""));
            firstMaster.setReference("Admin");
            firstMaster = bitrixRepository.save(firstMaster);
            admin.setFirstChildId(firstMaster.getId());
            bitrixRepository.save(admin);

            Bitrix secondMaster = new Bitrix();
            secondMaster.setAddress(bitcoinJService.getNewWalletAddress());
            secondMaster.setBalance(0L);
            secondMaster.setCreatedDate(new Date());
            secondMaster.setEmail("baron.alex1980+secondMaster@gmail.com");
            secondMaster.setNodes(2);
            secondMaster.setParentId(admin.getId());
            secondMaster.setPassword(passwordEncoder.encode("Qwerty123"));
            secondMaster.setPlan(BitrixType.STARTER);
            secondMaster.setReferee(UUID.randomUUID().toString().replace("-", ""));
            secondMaster.setRole(UserRole.ROLE_USER);
            secondMaster.setStatus(UserStatus.ACTIVE);
            secondMaster.setStep(0);
            secondMaster.setUsername("Second Master");
            secondMaster.setToken(UUID.randomUUID().toString().replace("-", ""));
            secondMaster.setReference("Admin");
            secondMaster = bitrixRepository.save(secondMaster);
            admin.setSecondChildId(secondMaster.getId());
            bitrixRepository.save(admin);

            Bitrix starter = new Bitrix();
            starter.setAddress(bitcoinJService.getNewWalletAddress());
            starter.setBalance(0L);
            starter.setCreatedDate(new Date());
            starter.setEmail("baron.alex1980+starter@gmail.com");
            starter.setNodes(0);
            starter.setParentId(firstMaster.getId());
            starter.setPassword(passwordEncoder.encode("Qwerty123"));
            starter.setPlan(BitrixType.STARTER);
            starter.setReferee(UUID.randomUUID().toString().replace("-", ""));
            starter.setRole(UserRole.ROLE_USER);
            starter.setStatus(UserStatus.ACTIVE);
            starter.setStep(0);
            starter.setUsername("Baron");
            starter.setToken(UUID.randomUUID().toString().replace("-", ""));
            starter.setReference("First Master");
            starter = bitrixRepository.save(starter);
            firstMaster.setFirstChildId(starter.getId());
            bitrixRepository.save(firstMaster);

            Bitrix bronze = new Bitrix();
            bronze.setAddress(bitcoinJService.getNewWalletAddress());
            bronze.setBalance(0L);
            bronze.setCreatedDate(new Date());
            bronze.setEmail("baron.alex1980+bronze@gmail.com");
            bronze.setNodes(0);
            bronze.setParentId(firstMaster.getId());
            bronze.setPassword(passwordEncoder.encode("Qwerty123"));
            bronze.setPlan(BitrixType.BRONZE);
            bronze.setReferee(UUID.randomUUID().toString().replace("-", ""));
            bronze.setRole(UserRole.ROLE_USER);
            bronze.setStatus(UserStatus.ACTIVE);
            bronze.setStep(0);
            bronze.setUsername("Boston");
            bronze.setToken(UUID.randomUUID().toString().replace("-", ""));
            bronze.setReference("First Master");
            bronze = bitrixRepository.save(bronze);
            firstMaster.setSecondChildId(bronze.getId());
            bitrixRepository.save(firstMaster);

            Bitrix silver = new Bitrix();
            silver.setAddress(bitcoinJService.getNewWalletAddress());
            silver.setBalance(0L);
            silver.setCreatedDate(new Date());
            silver.setEmail("baron.alex1980+silver@gmail.com");
            silver.setNodes(0);
            silver.setParentId(secondMaster.getId());
            silver.setPassword(passwordEncoder.encode("Qwerty123"));
            silver.setPlan(BitrixType.SILVER);
            silver.setReferee(UUID.randomUUID().toString().replace("-", ""));
            silver.setRole(UserRole.ROLE_USER);
            silver.setStatus(UserStatus.ACTIVE);
            silver.setStep(0);
            silver.setUsername("Bigmag");
            silver.setToken(UUID.randomUUID().toString().replace("-", ""));
            silver.setReference("Second Master");
            silver = bitrixRepository.save(silver);
            secondMaster.setFirstChildId(silver.getId());
            bitrixRepository.save(secondMaster);

            Bitrix gold = new Bitrix();
            gold.setAddress(bitcoinJService.getNewWalletAddress());
            gold.setBalance(0L);
            gold.setCreatedDate(new Date());
            gold.setEmail("baron.alex1980+gold@gmail.com");
            gold.setNodes(0);
            gold.setParentId(secondMaster.getId());
            gold.setPassword(passwordEncoder.encode("Qwerty123"));
            gold.setPlan(BitrixType.GOLD);
            gold.setReferee(UUID.randomUUID().toString().replace("-", ""));
            gold.setRole(UserRole.ROLE_USER);
            gold.setStatus(UserStatus.ACTIVE);
            gold.setStep(0);
            gold.setUsername("Barbery");
            gold.setToken(UUID.randomUUID().toString().replace("-", ""));
            gold.setReference("Second Master");
            gold = bitrixRepository.save(gold);
            secondMaster.setSecondChildId(gold.getId());
            bitrixRepository.save(secondMaster);
        }
    }

    @Override
    public ResultData signup(String username, String email, String password, String referee)
    {
        ResultData result = new ResultData(false, "");
        if (!Validator.isValidEmailAddress(email))
            result.setMessage("Email is not valid. Please enter a valid email.");
        else if (!Validator.isValidPassword(password))
            result.setMessage("Please enter a password with minimum 8 characters and containing at least one uppercase, one lowercase and one number.");
        else if (bitrixRepository.findByUsername(username.trim()) != null)
            result.setMessage("This username is taken before. Please enter another one.");
        else if (bitrixRepository.findByEmail(email.trim()) != null)
            result.setMessage("This email address is exist. If you forgot your password, please go to login page and select forgot password.");
        else
        {
            Bitrix refreeUser = bitrixRepository.findByReferee(referee);
            if (refreeUser == null)
                result.setMessage("It's not a valid referee.");
            else
            {
                Bitrix user = new Bitrix();
                user.setCreatedDate(new Date());
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                user.setToken(UUID.randomUUID().toString().replace("-", ""));
                user.setReferee(UUID.randomUUID().toString().replace("-", ""));
                user.setReference(refreeUser.getUsername());
                user.setBalance(0L);
                user.setParentId(0L);
                user.setAddress(bitcoinJService.getNewWalletAddress());
                user.setStep(0);
                user.setNodes(0);
                user.setStatus(UserStatus.INVITED);
                user.setPlan(BitrixType.STARTER);
                user.setRole(UserRole.ROLE_USER);

                user = bitrixRepository.save(user);
                emailService.sendVerification(user.getEmail(), user.getToken());

                result.setSuccess(true);
                result.setMessage("Your verification email has been successfully delivered to " + user.getEmail() +
                        ". If you can't see it in your Inbox within 5 minutes, please click on send verification email again. Please also check your junk folder.");
                result.addProperty("user", user);
            }
        }


        return result;
    }

    @Override
    public ResultData login(String email, String password)
    {
        ResultData result = new ResultData(false, "");
        Bitrix user = bitrixRepository.findByEmail(email);
        if (user == null)
            result.setMessage("You do not have permission to log in.");
        else if (user.getStatus().equals(UserStatus.INVITED))
            result.setMessage("You do not have permission to log in.");
        else if (user.getStatus().equals(UserStatus.DELETED))
            result.setMessage("You do not have permission to log in.");
        else if (!passwordEncoder.matches(password, user.getPassword()))
            result.setMessage("The password you've entered is incorrect. Please try again.");
        else if (user.getStatus().equals(UserStatus.INACTIVE) || user.getStatus().equals(UserStatus.ACTIVE))
        {
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getEmail(), password);
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            result.setSuccess(true);
            result.setMessage("Welcome to Bitcoin Matrix.");
            user.setToken(jwtTokenProvider.generateToken(authentication));
            user.setRefereeURL(appUrl + "/#/Register?ref=" + user.getReferee());
            result.addProperty("user", user);


            //RUNNING THE TEST METHOD
//            if(user.getId() == 1)
//            {
//                Timer timer = new Timer();
//                timer.scheduleAtFixedRate(testJob, 0, 1000L * 60); // 1 Min
//            }
        }
        return result;
    }

    @Override
    public ResultData authenticateReferee(String referee)
    {
        Bitrix reference = bitrixRepository.findByReferee(referee);
        if (reference == null)
            return new ResultData(false, "There is not any reference for this invitation.");
        else
        {
            ResultData data = new ResultData(true, "There is a match reference number with " + referee + ".");
            data.addProperty("reference", reference.getUsername());
            return data;
        }
    }

    @Override
    public ResultData resendVerification(String email)
    {
        Bitrix user = bitrixRepository.findByEmail(email);
        emailService.sendVerification(user.getEmail(), user.getToken());
        return new ResultData(true, "Verification email resent to your email. Please also check your junk folder.");
    }

    @Override
    public ResultData confirmVerification(String token)
    {
        Bitrix user = bitrixRepository.findByToken(token);
        if (user == null)
            return new ResultData(false, "User not found.");
        user.setStatus(UserStatus.INACTIVE);
        bitrixRepository.save(user);
        return new ResultData(true, "Welcome to Bitcoin Matrix. Your registration is confirmed and you can login now with your email address and password.");
    }

    @Override
    public ResultData forgotPassword(String email)
    {
        Bitrix user = bitrixRepository.findByEmail(email);
        if (user == null)
            return new ResultData(false, "This email address does not exist or not found. Please enter the correct email address and try it again.");
        else
        {
            String[] passwordArray = UUID.randomUUID().toString().split("-");
            String password = "";
            for (int i = 0; i < passwordArray.length; i++)
                password = password.concat(i % 2 == 0 ? passwordArray[i].toUpperCase() : passwordArray[i]);
            user.setPassword(passwordEncoder.encode(password));
            bitrixRepository.save(user);
            emailService.sendNewPassword(user.getEmail(), password);
            return new ResultData(true, "Your reset password email has been successfully emailed to " + email + ".\n"
                    + "If you can't see it in your Inbox within 5 minutes, please again click on forgot password. Please also check your junk folder.");
        }
    }

    @Override
    public Long countTransactions()
    {
        return bitrixTransactionRepository.countByUser(getCurrentUser());
    }

    @Override
    public ResultData getTransactions(String filter, String sortOrder, String sortBy, int pageNumber, int pageSize)
    {
        ResultData data = new ResultData(true, "");
        List<ResultData> transactionData = new ArrayList<>();

        List<String> sorts = new ArrayList<>();
        if (sortBy.isEmpty() || sortBy.equals("updateDate"))
        {
            sorts.add("updateDate");
            sorts.add("updateTime");
        } else if (sortBy.equals("value"))
        {
            sorts.add("totalValue");
        } else
            sorts.add(sortBy);
        Sort orderBy = new Sort(sortOrder.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sorts);

        for (BitrixTransaction transaction : bitrixTransactionRepository.findByUser(getCurrentUser(), PageRequest.of(pageNumber, pageSize, orderBy)))
        {
            ResultData trData = new ResultData(true, "");
            trData.addProperty("createdDat", transaction.getCreatedDate().toString());
            trData.addProperty("createdTime", transaction.getCreatedTime().toString());

            trData.addProperty("updateDat", transaction.getUpdateDate().toString());
            trData.addProperty("updateTime", transaction.getUpdateTime().toString());

            trData.addProperty("description", transaction.getDescription());
            trData.addProperty("value", transaction.getTotalValue());
            trData.addProperty("fee", transaction.getFee());

            trData.addProperty("status", transaction.getStatus().name());
            trData.addProperty("type", transaction.getType().name());
            transactionData.add(trData);
        }
        data.addProperty("transactions", transactionData);
        return data;
    }

    @Override
    public ResultData getUserAccountData()
    {
        bitcoinJService.updateAllAcountBalances();
        ResultData result = new ResultData(true, "");
        Bitrix user = getCurrentUser();
        user.setRefereeURL(appUrl + "/#/Register?ref=" + user.getReferee());
        result.addProperty("user", user);
        return result;
    }

    @Override
    public ResultData findUserData()
    {
        ResultData resultData = new ResultData(true, "");
        Bitrix user = getCurrentUser();

        if (user.getFirstChildId() != null)
            resultData.addProperty("first", bitrixRepository.findById(user.getFirstChildId()));
        if (user.getSecondChildId() != null)
            resultData.addProperty("second", bitrixRepository.findById(user.getSecondChildId()));
        return resultData;
    }

    @Override
    public ResultData activateUserNode()
    {
        ResultData resultData = new ResultData(true, "Your new level is activated.");
        if (getCurrentUser() == null || getCurrentUser().getId() == null)
            return new ResultData(false, "User not found.");

        Bitrix user = getCurrentUser();
        Long price = getPlanPrice(user);

        if (user.getBalance() < price)
            return new ResultData(false, "Account balance is not enough.");
        else
        {
            if (user.getStep() == 0 && user.getPlan().equals(BitrixType.STARTER))
            {
                user.setStatus(UserStatus.ACTIVE);
                user.setBalance(user.getBalance() - price);
                user.setStep(user.getStep() + 1);
                createTransaction(price, "Activate level " + user.getStep(), user.getPlan() + "_" + user.getStep() + "_" + user.getId(), TransactionType.PAYMENT, user);
                user.setRefereeURL(appUrl + "/#/Register?ref=" + user.getReferee());
                user = bitrixRepository.save(user);
                Bitrix refreeUser = bitrixRepository.findByUsername(user.getReference().trim());
                Bitrix parent = findFreeNode(refreeUser.getPlan().equals(BitrixType.STARTER) ? refreeUser : getStarter());
                user.setParentId(parent.getId());
                if (parent.getFirstChildId() == null)
                    parent.setFirstChildId(user.getId());
                else
                    parent.setSecondChildId(user.getId());
                bitrixRepository.save(parent);
            } else
            {
                // Mock User
                Bitrix newNode = new Bitrix();
                newNode.setUsername(user.getUsername() + "_LEVEL_UP_" + user.getPlan() + "_" + user.getStep() + "_" + UUID.randomUUID().toString().replace("-", ""));
                newNode.setEmail(user.getEmail() + "_LEVEL_UP_" + user.getPlan() + "_" + user.getStep() + "_" + UUID.randomUUID().toString().replace("-", ""));
                newNode.setToken(UUID.randomUUID().toString().replace("-", ""));
                newNode.setReferee(UUID.randomUUID().toString().replace("-", ""));
                newNode.setParentId(user.getParentId());
                newNode.setFirstChildId(user.getFirstChildId());
                newNode.setSecondChildId(user.getSecondChildId());
                newNode.setCreatedDate(new Date());
                newNode.setPassword(passwordEncoder.encode(UUID.randomUUID().toString().replace("-", "")));
                newNode.setReference(user.getReference());
                newNode.setBalance(0L);
                newNode.setAddress(bitcoinJService.getNewWalletAddress());
                newNode.setStep(0);
                newNode.setNodes(user.getNodes());
                newNode.setStatus(UserStatus.ACTIVE);
                newNode.setPlan(BitrixType.STARTER);
                newNode.setRole(UserRole.ROLE_USER);
                newNode = bitrixRepository.save(newNode);

                Bitrix parent = bitrixRepository.findById(newNode.getParentId()).get();
                Bitrix first = bitrixRepository.findById(newNode.getFirstChildId()).get();
                Bitrix second = bitrixRepository.findById(newNode.getSecondChildId()).get();

                if (parent.getFirstChildId().equals(user.getId()))
                    parent.setFirstChildId(newNode.getId());
                else
                    parent.setSecondChildId(newNode.getId());
                bitrixRepository.save(parent);

                first.setParentId(newNode.getId());
                bitrixRepository.save(first);
                second.setParentId(newNode.getId());
                bitrixRepository.save(second);


                // Original User
                Bitrix userParent;
                if (user.getPlan().equals(BitrixType.STARTER))
                    userParent = findFreeNode(getStarter());
                else if (user.getPlan().equals(BitrixType.BRONZE))
                    userParent = findFreeNode(getBronze());
                else if (user.getPlan().equals(BitrixType.SILVER))
                    userParent = findFreeNode(getSilver());
                else
                    userParent = findFreeNode(getGold());

                user.setParentId(userParent.getId());
                if (userParent.getFirstChildId() == null)
                    userParent.setFirstChildId(user.getId());
                else
                    userParent.setSecondChildId(user.getId());
                bitrixRepository.save(userParent);

                user.setFirstChildId(null);
                user.setSecondChildId(null);
                user.setStep(user.getStep() + 1);
                user.setStatus(UserStatus.ACTIVE);
                user.setBalance(user.getBalance() - price);
                createTransaction(price, "Activate level " + user.getStep(), user.getPlan() + "_" + user.getStep() + "_" + user.getId(), TransactionType.PAYMENT, user);
                bitrixRepository.save(user);
            }
        }

        Bitrix fund = getFund();
        fund.setBalance(fund.getBalance() + price);
        bitrixRepository.save(fund);

        updateParentNode(user, 4);
        return resultData;
    }

    private void createTransaction(Long amount, String description, String txId, TransactionType type, Bitrix user)
    {
        BitrixTransaction transaction = new BitrixTransaction();
        transaction.setCreatedDate(LocalDate.now());
        transaction.setCreatedTime(LocalTime.now());
        transaction.setUpdateDate(LocalDate.now());
        transaction.setUpdateTime(LocalTime.now());
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.CONFIRMED);
        transaction.setTotalValue(amount);
        transaction.setTxId(txId);
        transaction.setType(type);
        transaction.setUser(user);
        bitrixTransactionRepository.save(transaction);
    }

    private Bitrix findFreeNode(Bitrix node)
    {
        if (node.getFirstChildId() == null || node.getSecondChildId() == null)
            return node;
        long firstNodes = countNodes(node.getFirstChildId());
        long secondNodes = countNodes(node.getSecondChildId());
        if (firstNodes <= secondNodes)
            return findFreeNode(bitrixRepository.findById(node.getFirstChildId()).get());
        else
            return findFreeNode(bitrixRepository.findById(node.getSecondChildId()).get());
    }

    private long countNodes(Long id)
    {
        if(id == null)
            return 0;
        long count = 1;
        Bitrix child = bitrixRepository.findById(id).get();
        if(child.getFirstChildId() != null)
            count = count + countNodes(child.getFirstChildId());
        if(child.getFirstChildId() != null)
            count = count + countNodes(child.getSecondChildId());
        return count;
    }

    private void updateParentNode(Bitrix user, int depth)
    {
        if (depth == 0)
            return;
        Bitrix parent = bitrixRepository.findById(user.getParentId()).get();
        parent.setNodes(parent.getNodes() + 1);
        if (parent.getId() >= 10 && parent.getNodes() >= 30)
            parent.setStatus(UserStatus.INACTIVE);
        bitrixRepository.save(parent);
        if (parent.getRole().equals(UserRole.ROLE_USER))
            updateParentNode(parent, depth - 1);
    }

    @Override
    public Bitrix getCurrentUser()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.isAuthenticated() ? bitrixRepository.findById(((UserPrincipal) auth.getPrincipal()).getId()).get() : null;
    }

    private Long getPlanPrice(Bitrix user)
    {
        Long price = 100000L;
        if (user.getPlan().equals(BitrixType.BRONZE))
            price = 500000L;
        else if (user.getPlan().equals(BitrixType.SILVER))
            price = 1000000L;
        else if (user.getPlan().equals(BitrixType.GOLD))
            price = 2000000L;
        return price;
    }


    @Override
    public Bitrix getWallet()
    {
        return bitrixRepository.findById(1L).get();
    }

    @Override
    public Bitrix getFund()
    {
        return bitrixRepository.findById(2L).get();
    }

    @Override
    public Bitrix getAdmin()
    {
        return bitrixRepository.findByRole(UserRole.ROLE_ADMIN);
    }

    @Override
    public Bitrix getStarter()
    {
        return bitrixRepository.findById(6L).get();
    }

    @Override
    public Bitrix getBronze()
    {
        return bitrixRepository.findById(7L).get();
    }

    @Override
    public Bitrix getSilver()
    {
        return bitrixRepository.findById(8L).get();
    }

    @Override
    public Bitrix getGold()
    {
        return bitrixRepository.findById(9L).get();
    }

    @Override
    public ResultData sendWithdrawalCode()
    {
        Random rnd = new Random();
        String code = (100000 + rnd.nextInt(900000)) + "";
        Bitrix user = getCurrentUser();
        user.setDescription(code);
        bitrixRepository.save(user);
        return emailService.sendWithdrawalCode(user.getEmail(), code);
    }

    @Override
    public ResultData sendBitcoin(String userId, String address, String amountValue, String userSecurityCode)
    {
        ResultData resultData = new ResultData(true, "");
        Bitrix user = getCurrentUser();
        if (user.getId().equals(Long.valueOf(userId)))
        {
            String securityCode = user.getDescription().trim();
            if (!userSecurityCode.trim().equals(securityCode))
                return new ResultData(false, "Security code is not correct.");

            Double amountVal = Double.valueOf(amountValue);

            if (amountVal < 0.0005)
                return new ResultData(false, "Amount is below the minimum (0.0005 BTC).");


            Long amount = (long) (amountVal * 100000000);
            Long balance = user.getBalance();
            Long fee = 1000L + Math.round(amount * 0.005);

            if (amount > balance - fee)
                return new ResultData(false, "Maximum amount exceeded.");

            TransactionStatus status = TransactionStatus.CONFIRMED;
            Bitrix destinationUser = bitrixRepository.findByAddress(address);
            if (destinationUser == null)
                status = bitcoinJService.forwardCoins(amount, address); //Forward coins to user external wallet
            else
            {
                // Create destination user transaction
                BitrixTransaction destinationUserTransaction = new BitrixTransaction();
                destinationUserTransaction.setUser(destinationUser);
                destinationUserTransaction.setCreatedDate(LocalDate.now());
                destinationUserTransaction.setCreatedTime(LocalTime.now());
                destinationUserTransaction.setUpdateDate(LocalDate.now());
                destinationUserTransaction.setUpdateTime(LocalTime.now());
                destinationUserTransaction.setTxId("TRANSFER-FROM-" + user.getId() + "-TO-" + destinationUser.getId() + "-" + bitrixTransactionRepository.count());
                destinationUserTransaction.setType(TransactionType.DEPOSIT);
                destinationUserTransaction.setTotalValue(amount);
                destinationUserTransaction.setStatus(status);
                destinationUserTransaction.setDescription("Transfer to your account from " + user.getUsername());
                bitrixTransactionRepository.save(destinationUserTransaction);

                //Set destination user new balance value
                destinationUser.setBalance(destinationUser.getBalance() + amount);
                bitrixRepository.save(destinationUser);
            }

            // Create user transaction
            BitrixTransaction userTransaction = new BitrixTransaction();
            userTransaction.setUser(user);
            userTransaction.setCreatedDate(LocalDate.now());
            userTransaction.setCreatedTime(LocalTime.now());
            userTransaction.setUpdateDate(LocalDate.now());
            userTransaction.setUpdateTime(LocalTime.now());
            userTransaction.setTxId("WITHDRAW-BY-" + user.getId() + "-" + bitrixTransactionRepository.count());
            userTransaction.setType(TransactionType.WITHDRAWAL);
            userTransaction.setTotalValue(amount);
            userTransaction.setFee(fee + "");
            userTransaction.setStatus(status);
            userTransaction.setDescription("Withdraw to " + address);
            bitrixTransactionRepository.save(userTransaction);

            if (!userTransaction.getStatus().equals(TransactionStatus.FAILED))
            {
                //Add fee to admin balance
                Bitrix admin = getAdmin();
                admin.setBalance(admin.getBalance() + fee);
                bitrixRepository.save(admin);

                //Set user new balance value
                Long newBalance = balance - amount - fee;
                user.setBalance(newBalance);
                resultData.addProperty("balance", user.getBalance());
                bitrixRepository.save(user);
                return resultData;
            }
            return new ResultData(false, "Error in sending Bitcoin.");
        }
        return new ResultData(false, "Error in user authentication.");
    }

    @Override
    public ResultData completeMatrix()
    {
        Bitrix user = getCurrentUser();
        Bitrix fund = getFund();
        Bitrix admin = getAdmin();
        if (user.getNodes() >= 30)
        {
            Long price = getPlanPrice(user);
            Long capital = 30 * price;
            user.setNodes(0);
            Long userShare = capital / 3;
            Long adminShare = capital / 5;

            user.setBalance(user.getBalance() + userShare);
            createTransaction(userShare, "Matrix completion in level " + user.getStep() + " [plan: " + user.getPlan() + "]", "MATRIX_" + user.getPlan() + "_" + user.getStep() + "_" + user.getId(), TransactionType.REWARD, user);

            admin.setBalance(admin.getBalance() + adminShare);
            createTransaction(adminShare, "Matrix completion in level " + user.getStep() + " [plan: " + user.getPlan() + "] by " + user.getUsername() + " (ID: " + user.getId() + ")", "MATRIX_" + user.getPlan() + "_" + user.getStep() + "_BY_USER_ID_" + user.getId(), TransactionType.REWARD, admin);

            fund.setBalance(fund.getBalance() - (userShare + adminShare));
            bitrixRepository.save(user);
            bitrixRepository.save(admin);
            bitrixRepository.save(fund);
            return new ResultData(true, "");
        }
        return new ResultData(false, "User nodes are not enough.");
    }

    @Override
    public ResultData startNewPlan()
    {
        ResultData result = this.completeMatrix();
        if (result.isSuccess())
        {
            Bitrix user = getCurrentUser();
            user.setStep(0);
            if (user.getPlan().equals(BitrixType.STARTER))
                user.setPlan(BitrixType.BRONZE);
            else if (user.getPlan().equals(BitrixType.BRONZE))
                user.setPlan(BitrixType.SILVER);
            else if (user.getPlan().equals(BitrixType.SILVER))
                user.setPlan(BitrixType.GOLD);
            else if (user.getPlan().equals(BitrixType.GOLD))
                user.setPlan(BitrixType.STARTER);
            bitrixRepository.save(user);
        }
        return result;
    }

    @Override
    public ResultData changeUserPassword(String currentPassword, String newPassword, String repeatedNewPassword)
    {
        Bitrix user = getCurrentUser();
        if (!Validator.isValidPassword(newPassword))
            return new ResultData(false, "Please enter a password with minimum 8 characters and containing at least one uppercase, one lowercase and one number.");
        if(passwordEncoder.matches(newPassword, user.getPassword()))
            return new ResultData(false, "Current password is incorrect.");
        user.setPassword(passwordEncoder.encode(newPassword));
        bitrixRepository.save(user);
        return new ResultData(true, "Your password has been changed successfully.");
    }
}
