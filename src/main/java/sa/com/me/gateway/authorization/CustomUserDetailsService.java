package sa.com.me.gateway.authorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import sa.com.me.core.model.User;
import sa.com.me.gateway.client.UserClient;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Let people login with either username
        User user = userClient.getUserByUsername(username);

        return UserPrincipal.create(user);
    }
}