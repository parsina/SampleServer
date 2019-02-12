package com.coin.app.security;

import com.coin.app.model.Bitrix;
import com.coin.app.repository.BitrixRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService
{
    @Autowired
//    private UserRepository userRepository;
    private BitrixRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException
    {
//        User user = userRepository.findByEmail(usernameOrEmail);
        Bitrix user = userRepository.findByEmail(usernameOrEmail);
        return UserPrincipal.create(user);
    }

    // This method is used by JWTAuthenticationFilter
    @Transactional
    public UserDetails loadUserById(Long id)
    {
//        User user = userRepository.findById(id).orElseThrow( () -> new UsernameNotFoundException("کاربر مورد نظر پیدا نشد. "));
        Bitrix user = userRepository.findById(id).orElseThrow( () -> new UsernameNotFoundException("کاربر مورد نظر پیدا نشد. "));
        return UserPrincipal.create(user);
    }
}
