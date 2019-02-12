package com.coin.app.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimerTask;
import java.util.UUID;

import com.coin.app.model.Bitrix;
import com.coin.app.model.BitrixTransaction;
import com.coin.app.model.enums.BitrixType;
import com.coin.app.model.enums.TransactionStatus;
import com.coin.app.model.enums.TransactionType;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.coin.app.repository.BitrixRepository;
import com.coin.app.repository.BitrixTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class Job extends TimerTask
{
    @Value("${client.url}")
    private String appUrl;

    @Autowired
    private BitrixService bitrixService;

    @Autowired
    private BitcoinJService bitcoinJService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BitrixRepository bitrixRepository;

    @Autowired
    private BitrixTransactionRepository bitrixTransactionRepository;

    @Override
    public void run()
    {
        Bitrix user = new Bitrix();
        user.setAddress(bitcoinJService.getNewWalletAddress());
        user.setBalance(bitcoinJService.getWalletBalance());
        user.setCreatedDate(new Date());
        int count = bitrixRepository.findAll().size() - 8;
        String username = "User" + ( count < 10 ? "000" + count : count < 100 ? "00" + count : count < 1000 ? "0" + count : count);
        user.setUsername(username);
        user.setEmail("baron.alex1980+" + username + "@gmail.com");
        user.setNodes(0);
        user.setPassword(passwordEncoder.encode("Qwerty123"));
        user.setPlan(BitrixType.STARTER);
        user.setReferee(UUID.randomUUID().toString().replace("-", ""));
        user.setRole(UserRole.ROLE_USER);
        user.setStatus(UserStatus.ACTIVE);
        user.setBalance(0L);
        user.setStep(1);
        user.setToken(UUID.randomUUID().toString().replace("-", ""));
        user.setReference("Baron");
        user.setRefereeURL(appUrl + "/#/Register?ref=" + user.getReferee());
        Bitrix parent = findFreeNode(bitrixRepository.findById(10L).get());
        user.setParentId(parent.getId());
        user = bitrixRepository.save(user);

        if (parent.getFirstChildId() == null)
            parent.setFirstChildId(user.getId());
        else
            parent.setSecondChildId(user.getId());
        bitrixRepository.save(parent);

        createTransaction(100000L, "Activate level " + user.getStep(), user.getPlan() + "_" + user.getStep() + "_" + user.getId(), TransactionType.PAYMENT, user);

        Bitrix fund = bitrixRepository.findById(2L).get();
        fund.setBalance(fund.getBalance() + 100000L);
        bitrixRepository.save(fund);

        updateParentNode(user, 4);

        System.out.println(">>> Activating new user: " + username + "         [id : " + user.getId() + "     Parent id : " + user.getParentId() + "]       Time : " + LocalTime.now());

        int min = LocalTime.now().getMinute();

        if(min == 10)
            activateUser();
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



    private void activateUser()
    {
        Bitrix fund = bitrixRepository.findById(2L).get();
        Bitrix admin = bitrixRepository.findById(3L).get();
        Long price = 100000L;

        for (Bitrix user : bitrixRepository.findAll())
        {
            if (user.getNodes() >= 30 && user.getPlan().equals(BitrixType.STARTER))
            {
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

                activateUserNode(user);
            }
        }
    }

    private void activateUserNode(Bitrix user)
    {
        if (user == null || user.getId() == null)
            return;

        if(user.getPlan() != BitrixType.STARTER)
            return;
        Long price = 100000L;

        if (user.getBalance() < price)
            return;
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
                Bitrix parent = bitrixRepository.findById(6L).get();
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

                Bitrix parent =  bitrixRepository.findById(6L).get();
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
//                if (user.getPlan().equals(BitrixType.STARTER))
                    userParent = bitrixRepository.findById(6L).get();
//                else if (user.getPlan().equals(BitrixType.BRONZE))
//                    userParent = findFreeNode(getBronze());
//                else if (user.getPlan().equals(BitrixType.SILVER))
//                    userParent = findFreeNode(getSilver());
//                else
//                    userParent = findFreeNode(getGold());

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

        Bitrix fund = bitrixRepository.findById(2L).get();
        fund.setBalance(fund.getBalance() + price);
        bitrixRepository.save(fund);
        updateParentNode(user, 4);
    }

}
