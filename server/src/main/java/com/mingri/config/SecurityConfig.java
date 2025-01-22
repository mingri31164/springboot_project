package com.mingri.config;


import com.mingri.filter.JwtAuthenticationTokenFilter;
import com.mingri.handler.AccessDeniedHandlerImpl;
import com.mingri.handler.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig  {

	@Autowired
	private JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
	@Autowired
	private AuthenticationEntryPointImpl authenticationEntryPoint;
	@Autowired
	private AccessDeniedHandlerImpl accessDeniedHandler;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authManager(HttpSecurity http) throws Exception {
		AuthenticationManagerBuilder authenticationManagerBuilder =
				http.getSharedObject(AuthenticationManagerBuilder.class);
		return authenticationManagerBuilder.build();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf().disable() // 关闭 CSRF
				.sessionManagement().
				sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 使用无状态会话
				.and()
				.authorizeRequests()
				.antMatchers("/sys-user/login","/sys-user/register",
						"/common/*",
						"/v2/api-docs",
						"/swagger-resources/configuration/ui",
						"/swagger-resources",
						"/swagger-resources/configuration/security",
						"/doc.html",
						"/swagger-ui.html",
						"/webjars/**").anonymous() // 接口允许匿名访问（已登录不可访问，未登录可以）
				.anyRequest().authenticated(); // 其他请求需要认证

		//把token校验过滤器添加到过滤器链中
		http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

		//配置认证失败处理器
		http.exceptionHandling()
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler);

		//配置跨域
		http.cors();

		return http.build();

	}
}
