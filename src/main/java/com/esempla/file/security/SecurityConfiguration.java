package com.esempla.file.security;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final DataSource dataSource;

    public SecurityConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource)
                .authoritiesByUsernameQuery("select user_data.login, user_roles.user_role\n" +
                        "from user_data\n" +
                        "inner join user_roles\n" +
                        "on user_data.id = user_roles.user_id where login = ?")
                .usersByUsernameQuery("select login, password, true FROM user_data where login = ?")
                .passwordEncoder(new BCryptPasswordEncoder())
        ;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //it does matter the order of antMatchers
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/api/users").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api/users/**").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/api/documents").hasAuthority(AuthoritiesConstants.ADMIN)
                /*.antMatchers(HttpMethod.DELETE).hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/users").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/user/{id}").hasAuthority(AuthoritiesConstants.ADMIN)
                .antMatchers("/comments").authenticated()
                .antMatchers("/comment/{id}").authenticated()
                .antMatchers("/posts").authenticated()
                .antMatchers("/post/{id}").authenticated()*/
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
        ;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
