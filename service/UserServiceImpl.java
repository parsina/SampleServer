package com.coin.app.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.coin.app.common.DeviceProvider;
import com.coin.app.dto.data.ResultData;
import com.coin.app.model.User;
import com.coin.app.model.enums.UserRole;
import com.coin.app.model.enums.UserStatus;
import com.coin.app.repository.UserRepository;
import com.coin.app.security.JwtTokenProvider;
import com.coin.app.security.UserPrincipal;
import com.coin.app.security.payload.JwtAuthenticationResponse;
import com.coin.app.service.mail.EmailService;
import com.coin.app.util.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    @Override
    public ResultData createUser(String email, String password, String repeatedPassword)
    {
        ResultData result = new ResultData(false, "");

        boolean emailIsValid = Validator.isValidEmailAddress(email);
        boolean passworIsValid = Validator.isValidPassword(password);
        boolean passwordMatched = password.equals(repeatedPassword);

        if (!emailIsValid)
            result.setMessage("Email Invalid");
        else if (!passworIsValid)
            result.setMessage("Password Invalid");
        if (!passwordMatched)
            result.setMessage("Password not matched");

        if (emailIsValid && passworIsValid && passwordMatched)
        {
            User user = userRepository.findByEmail(email.toLowerCase());
            if (user != null)
            {
                result.setSuccess(true); // Should be removed
                result.setMessage("User Exist !");
                result.addProperty("userEmail", user.getEmail()); // Should be removed
                return result;
            }
            user = new User();
            user.setCreatedDate(new Date());
            user.setParentId(-1L);
            user.setEmail(email.toLowerCase());
            user.setPassword(passwordEncoder.encode(password));
            user.setStatus(UserStatus.INACTIVE);
            user.setRole(UserRole.ROLE_USER);
            user.setConfirmationToken(UUID.randomUUID().toString());
            userRepository.save(user);
            result.setSuccess(true);
            result.setMessage("User Created !");
            result.addProperty("userEmail", user.getEmail());
        }
        return result;
    }

    public ResultData confirmRegistration(String token)
    {
        ResultData result = new ResultData(false, "");

        User user = userRepository.findByConfirmationToken(token);
        if (user != null)
        {
            if (user.getStatus().equals(UserStatus.ACTIVE))
            {
                result.setMessage("User is enabled before");
                return result;
            }

            user.setStatus(UserStatus.ACTIVE);
            userRepository.save(user);
            result.setSuccess(true);
            result.setMessage("User activation is finished");
            result.addProperty("id", user.getId());
            result.addProperty("email", user.getEmail());
        }
        return result;
    }

    @Override
    public ResultData login(String email, String password)
    {
        ResultData result = new ResultData(false, "");
        User user = userRepository.findByEmail(email);
        if (user == null)
        {
            result.setMessage("Email is incorrect !");
            return result;
        } else if (user.getStatus().equals(UserStatus.INACTIVE))
        {
            result.setMessage("User is not Active");
            return result;
        } else if (user.getStatus().equals(UserStatus.DELETED))
        {
            result.setMessage("User is Deleted");
            return result;
        } else if (!passwordEncoder.matches(password, user.getPassword()))
        {
            result.setMessage("Password is incorrect !");
            return result;
        } else
        {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtTokenProvider.generateToken(authentication);
//            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));


            result.setSuccess(true);
            result.setMessage("User loged in !");
            result.addProperty("id", user.getId());
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
    public User activateUser(User user)
    {
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Override
    public void save(User user)
    {
//        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
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
