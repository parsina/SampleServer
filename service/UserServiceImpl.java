package com.coin.app.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.coin.app.dto.data.ResultData;
import com.coin.app.model.User;
import com.coin.app.model.enums.UserStatus;
import com.coin.app.repository.RoleRepository;
import com.coin.app.repository.UserRepository;
import com.coin.app.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService
{
    @Autowired
    UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public ResultData createUser(String email, String password, String repeatedPassword)
    {

        ResultData result = new ResultData(false, "");

        boolean emailIsValid = Validator.isValidEmailAddress(email);
        boolean passworIsValid = Validator.isValidPassword(password);
        boolean passwordMatched = password.equals(repeatedPassword);

        if(!emailIsValid)
            result.setMessage("Email Invalid");
        else
        if(!passworIsValid)
            result.setMessage("Password Invalid");
        if(!passwordMatched)
            result.setMessage("Password not matched");

        if(emailIsValid && passworIsValid && passwordMatched)
        {
            User user = userRepository.findByEmail(email.toLowerCase());
            if(user != null)
            {
                result.setMessage("User Exist !");
                result.addProperty("User", user);
            }
            else
            {
                user = new User();
                user.setCreatedDate(new Date());
            }
            user.setParentId(-1L);
            user.setEmail(email.toLowerCase());
            user.setPassword(password);
            user.setStatus(UserStatus.INACTIVE);
//            userRepository.save(user);

            result.setSuccess(true);
            result.setMessage("User Created !");
            result.addProperty("User", user);
        }
        return result;
    }

    @Override
    public User activateUser(User user)
    {
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Override
    public void save(User user)
    {
//        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        userRepository.save(user);
    }

    @Override
    public User findByUsername(String username)
    {
        return userRepository.findByEmail(username);
    }

    @Override
    public List<User> findAllUsers()
    {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id)
    {
        return userRepository.findById(id).get();
    }

    @Override
    public boolean isUserExist(User user)
    {
        return userRepository.existsById(user.getId());
    }

    @Override
    public User saveUser(User user)
    {
        return userRepository.save(user);
    }

    @Override
    public User updateUser(User user)
    {
        return userRepository.save(user);
    }

    @Override
    public void deleteUserById(Long id)
    {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteAllUsers()
    {
        userRepository.deleteAll();
    }
}
