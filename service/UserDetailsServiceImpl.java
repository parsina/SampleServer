package com.coin.app.service;

import java.util.ArrayList;
import java.util.List;

import com.coin.app.model.User;
import com.coin.app.repository.RoleRepository;
import com.coin.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserDetailsServiceImpl implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException
    {
        User user = this.userRepository.findByEmail(email);

        if (user == null)
        {
            System.out.println("User not found! " + email);
            throw new UsernameNotFoundException("User " + email + " was not found in the database");
        }

        System.out.println("Found User: " + user);

        // [ROLE_USER, ROLE_ADMIN,..]
//        List<String> roleNames = this.roleRepository.getRoleNames(user.getUserId());
        List<String> roleNames = new ArrayList<>();
        roleNames.add("ADMIN");
        roleNames.add("TRADER");

        List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
        if (roleNames != null)
        {
            for (String role : roleNames)
            {
                // ROLE_USER, ROLE_ADMIN,..
                GrantedAuthority authority = new SimpleGrantedAuthority(role);
                grantList.add(authority);
            }
        }

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(user.getEmail(),  user.getPassword(), grantList);

        return userDetails;
    }
}
