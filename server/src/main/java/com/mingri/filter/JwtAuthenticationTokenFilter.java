package com.mingri.filter;


import cn.hutool.core.util.StrUtil;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.RedisConstant;
import com.mingri.context.BaseContext;
import com.mingri.entity.LoginUser;
import com.mingri.exception.LoginFailedException;
import com.mingri.properties.JwtProperties;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.JwtUtil;
import com.mingri.utils.RedisUtils;
import com.mingri.utils.UrlPermitUtil;
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
import java.util.HashMap;
import java.util.Map;
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
    @Autowired
    private CacheUtil cacheUtil;
    @Autowired
    private UrlPermitUtil urlPermitUtil;

    /**
     * @Description: 过滤器拦截请求
     * @Author: mingri31164
     * @Date: 2025/1/21 21:00
     **/
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(jwtProperties.getTokenName());
        String url = request.getRequestURI();

        if (!StringUtils.hasText(token)) {
            // 如果没有令牌，直接放行
            filterChain.doFilter(request, response);
            return;
        }

        if (!urlPermitUtil.isPermitUrl(url)){
            try {
                Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                String userId = (String) claims.get(JwtClaimsConstant.USER_ID);
                log.info("当前用户的id：{}", userId);
                //判断是否在其他地方登录
                String cacheToken = cacheUtil.getUserSessionCache(userId);
                if (StrUtil.isBlank(cacheToken)) {
                    throw new LoginFailedException(MessageConstant.AUTHENTICATION_FAILED);
                } else if (!cacheToken.equals(token)) {
                    throw new LoginFailedException(MessageConstant.LOGIN_IN_OTHER_PLACE);
                }
                setUserInfo(claims, url, request, response);
            } catch (Exception e) {
                throw new LoginFailedException(MessageConstant.AUTHENTICATION_FAILED);
            }
        } else {
            if (StrUtil.isNotBlank(token)) {
                try {
                    Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
                    setUserInfo(claims, url, request, response);
                } catch (Exception e) {
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    public void setUserInfo(Claims claims, String url,
                            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        String userId = (String) claims.get(JwtClaimsConstant.USER_ID);

        //TODO 待解决bug：存入map失败
        // 设置用户信息
        Map<String, Object> map = new HashMap<>();
        claims.entrySet().stream().forEach(e -> map.put(e.getKey(), e.getValue()));
        //验证角色是否有权限
        String role = (String) map.get("role");
        if (!urlPermitUtil.isRoleUrl(role, url)) {
            throw new LoginFailedException(MessageConstant.PERMISSION_NOT_EXIST);
        }
        httpServletRequest.setAttribute("userinfo", map);

        // 从 Redis 中获取用户信息
        String redisKey = RedisConstant.USER_INFO_PREFIX + userId;
        LoginUser loginUser = (LoginUser) redisUtils.get(redisKey);

        // 判断获取到的用户信息是否为空
        if (Objects.isNull(loginUser)) {
            throw new LoginFailedException(MessageConstant.USER_NOT_LOGIN);
        }

        // 将用户信息放入 SecurityContextHolder
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser,
                        null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        BaseContext.setCurrentId(Long.valueOf(userId));
    }
}
