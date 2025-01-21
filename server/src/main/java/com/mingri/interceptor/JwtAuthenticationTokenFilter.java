package com.mingri.interceptor;


import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.RedisConstant;
import com.mingri.context.BaseContext;
import com.mingri.entity.LoginUser;
import com.mingri.exception.LoginFailedException;
import com.mingri.properties.JwtProperties;
import com.mingri.utils.JwtUtil;
import com.mingri.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private RedisUtils redisUtils;

//    /**
//     * 校验jwt
//     *
//     * @param request
//     * @param response
//     * @param handler
//     * @return
//     * @throws Exception
//     */
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        //判断当前拦截到的是Controller的方法还是其他资源
//        if (!(handler instanceof HandlerMethod)) {
//            //当前拦截到的不是动态方法，直接放行
//            return true;
//        }
//
//        //1、从请求头中获取令牌
//        String token = request.getHeader(jwtProperties.getUserTokenName());
//
//        //2、校验令牌
//        try {
//            log.info("jwt校验:{}", token);
//            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
//            Object claims_userid = claims.get(JwtClaimsConstant.USER_ID);
//            log.info("jwt校验通过: {}",claims_userid);
//            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
//            log.info("当前用户的id：{}", userId);
//            BaseContext.setCurrentId(userId);
//            //3、通过，放行
//            return true;
//        } catch (Exception ex) {
//            //4、不通过，响应401状态码
//            response.setStatus(401);
//            return false;
//        }
//    }


    /**
     * @Description: 过滤器拦截请求
     * @Author: mingri31164
     * @Date: 2025/1/21 21:00
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 获取 token
        String token = request.getHeader(jwtProperties.getUserTokenName());

        // 判空
        if (!StringUtils.hasText(token)) {
            // 如果没有令牌，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        // 校验令牌
        Long userId;
        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            log.info("当前用户的id：{}", userId);
            BaseContext.setCurrentId(userId);
        } catch (Exception e) {
            // 解析失败
            throw new LoginFailedException(MessageConstant.TOKEN_ERROR);
        }

        // 从 Redis 中获取用户信息
        String redisKey = RedisConstant.USER_INFO_PREFIX + userId;
        LoginUser loginUser = (LoginUser) redisUtils.get(redisKey);

        // 判断获取到的用户信息是否为空
        if (Objects.isNull(loginUser)) {
            throw new LoginFailedException(MessageConstant.USER_NOT_LOGIN);
        }

        // 将用户信息放入 SecurityContextHolder
        //TODO 获取权限信息封装到Authentication中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 继续请求
        filterChain.doFilter(request, response);
    }
}
