package com.mingri.service.impl;

import com.mingri.constant.MailConstant;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.RedisConstant;
import com.mingri.dto.user.SysUserLoginDTO;
import com.mingri.dto.user.SysUserRegisterDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.SysUser;
import com.mingri.enumeration.UserStatus;
import com.mingri.exception.EmailErrorException;
import com.mingri.exception.LoginFailedException;
import com.mingri.exception.RegisterFailedException;
import com.mingri.mapper.SysMenuMapper;
import com.mingri.mapper.SysUserMapper;
import com.mingri.service.ISysUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mingri.utils.CacheUtil;
import com.mingri.utils.RedisUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-22
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService, UserDetailsService {

    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private CacheUtil cacheUtil;

    public void register(SysUserRegisterDTO sysUserRegisterDTO) {

        // 邮箱重复处理
        if (lambdaQuery().eq(SysUser::getEmail, sysUserRegisterDTO.getEmail()).exists()) {
            throw new RegisterFailedException(MessageConstant.EMAIL_EXIST);
        }
        // 根据邮箱生成Redis键名
        String redisKey = MailConstant.CAPTCHA_CODE_KEY_PRE + sysUserRegisterDTO.getEmail();
        // 尝试从Redis获取现有的验证码
        Object oldCode = redisUtils.get(redisKey);
        if (oldCode == null){
            throw new EmailErrorException
                    (MessageConstant.PLEASE_GET_VERIFICATION_CODE_FIRST);
        }
        // 检查用户名是否已存在
        if (lambdaQuery().eq(SysUser::getUserName, sysUserRegisterDTO.getUserName()).exists()) {
            throw new RegisterFailedException(MessageConstant.ACCOUNT_EXIST);
        }

        if (sysUserRegisterDTO.getEmailCode() == null ||
                sysUserRegisterDTO.getEmailCode().isEmpty() ||
                !oldCode.equals(sysUserRegisterDTO.getEmailCode())){
            throw new EmailErrorException
                    (MessageConstant.EMAIL_VERIFICATION_CODE_ERROR);
        }
        SysUser sysUser = new SysUser();
        //对象属性拷贝
        BeanUtils.copyProperties(sysUserRegisterDTO, sysUser);
        //passwordEncoder加密
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        //设置账号的状态，默认正常状态 0表示正常 1表示锁定
        sysUser.setStatus(UserStatus.NORMAL);
        save(sysUser);
    }


    /**
     * @Description: 重写security过滤器中的方法，改为从数据库查询用户信息
     * @Author: mingri31164
     * @Date: 2025/1/22 22:57
     **/
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询数据库中的数据
        SysUser sysUser = lambdaQuery().eq(SysUser::getUserName, username).one();
        if(Objects.isNull(sysUser)){
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }

        //把查询到的user结果，封装成UserDetails类型，然后返回。
        //但是由于UserDetails是个接口，所以我们需要先新建LoginUser类，作为UserDetails的实现类

        // 查询用户权限信息
        //权限集合，在LoginUser类做权限集合的转换
//        List<String> list = new ArrayList<>(Arrays.asList("test","admin","user"));
        List<String> list = sysMenuMapper.selectPermsByUserId(sysUser.getId());

        //把查询到的user结果，封装成UserDetails类型返回
        return new LoginUser(sysUser,list); //这里传了第二个参数，表示的是权限信息
    }


    /**
     * @Description: 登录（security）
     * @Author: mingri31164
     * @Date: 2025/1/21 18:33
     **/

    public SysUser login(SysUserLoginDTO userLoginDTO) {
        // 用户在登录页面输入的用户名和密码
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUserName(), userLoginDTO.getPassword());

        try {
            // 获取 AuthenticationManager 的 authenticate 方法来进行用户认证
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);

            // 认证成功后，提取 LoginUser
            LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
            SysUser sysUser = loginUser.getSysUser();
            // 检查用户状态
            if (sysUser.getStatus().equals(UserStatus.FREEZE)) {
                throw new LoginFailedException(MessageConstant.ACCOUNT_LOCKED);
            }

            // 把完整的用户信息存入 Redis，其中 userid 作为 key
            redisUtils.set(RedisConstant.USER_INFO_PREFIX +
                    sysUser.getId().toString(), loginUser);
            return sysUser;

        } catch (AuthenticationException e) {
            // 认证失败处理
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }
    }


    /**
     * @Description: 用户退出登录
     * @Author: mingri31164
     * @Date: 2025/1/21 21:36
     **/
    public void logout() {
        //获取我们在JwtAuthenticationTokenFilter类写的SecurityContextHolder对象中的用户id
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        //loginUser是我们在domain目录写好的实体类
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //获取用户id
        Long userid = loginUser.getSysUser().getId();

        //根据用户id，删除redis中的token值，注意我们的key是被 login: 拼接过的，所以下面写完整key的时候要带上 longin:
        String key = RedisConstant.USER_INFO_PREFIX + userid.toString();
        redisUtils.del(key);
        cacheUtil.clearUserSessionCache(String.valueOf(userid));
    }
}
