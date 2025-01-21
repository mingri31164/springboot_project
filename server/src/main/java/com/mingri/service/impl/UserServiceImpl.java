package com.mingri.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mingri.constant.*;
import com.mingri.dto.*;
import com.mingri.dto.UserLoginDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.User;
import com.mingri.exception.*;
import com.mingri.mapper.UserMapper;
import com.mingri.result.PageResult;
import com.mingri.service.UserService;
import com.mingri.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.security.core.userdetails.UserDetailsService;


import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class UserServiceImpl implements UserService,UserDetailsService{

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private PasswordEncoder passwordEncoder;



    /**
     * 用户登录（md5）
     *
     * @param userLoginDTO
     * @return
     */
//    public User login(UserLoginDTO userLoginDTO) {
//        String username = userLoginDTO.getUsername();
//        String password = userLoginDTO.getPassword();
//
//        //1、根据用户名查询数据库中的数据
//        User user = userMapper.getByUsername(username);
//
//        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
//        if (user == null) {
//            //账号不存在
//            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
//        }
//
//        //TODO 后续将账号不存在和密码错误归为一种异常
//        //密码比对
//        //对前端传过来的明文密码进行md5加密处理
//        password = DigestUtils.md5DigestAsHex(password.getBytes());
//        if (!password.equals(user.getPassword())) {
//            //密码错误
//            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
//        }
//
//        if (user.getStatus() == StatusConstant.DISABLE) {
//            //账号被锁定
//            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
//        }
//
//        //3、返回实体对象
//        return user;
//    }

    /**
     * 新增用户
     *
     * @param userDTO
     */
    public void save(UserDTO userDTO) {
        User employee = new User();

        //对象属性拷贝
        BeanUtils.copyProperties(userDTO, employee);

        //设置账号的状态，默认正常状态 1表示正常 0表示锁定
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        userMapper.insert(employee);
    }

    /**
     * 分页查询
     *
     * @param userPageQueryDTO
     * @return
     */
    public PageResult pageQuery(UserPageQueryDTO userPageQueryDTO) {
        // select * from employee limit 0,10
        //开始分页查询
        PageHelper.startPage(userPageQueryDTO.getPage(), userPageQueryDTO.getPageSize());

        Page<User> page = userMapper.pageQuery(userPageQueryDTO);

        long total = page.getTotal();
        List<User> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 启用禁用用户账号
     *
     * @param status
     * @param id
     */
    public void startOrStop(Integer status, Long id) {
        User employee = User.builder()
                .status(status)
                .id(id)
                .build();

        userMapper.update(employee);
    }

    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    public User getById(Long id) {
        User employee = userMapper.getById(id);
        employee.setPassword("****");
        return employee;
    }

    /**
     * 编辑用户信息
     * @param userDTO
     */
    public void update(UserDTO userDTO) {
        User employee = new User();
        BeanUtils.copyProperties(userDTO, employee);
        userMapper.update(employee);
    }

    public void register(UserRegisterDTO userRegisterDTO) {

        //TODO 邮箱重复处理
//        if (userMapper.existsByEmail(userRegisterDTO.getEmail())) {
//            throw new RegisterFailedException(MessageConstant.ACCOUNT_EXIST);
//        }
        // 根据邮箱生成Redis键名
        String redisKey = MailConstant.CAPTCHA_CODE_KEY_PRE + userRegisterDTO.getEmail();
        // 尝试从Redis获取现有的验证码
        Object oldCode = redisUtils.get(redisKey);
        if (oldCode == null){
            throw new EmailErrorException
                    (MessageConstant.PLEASE_GET_VERIFICATION_CODE_FIRST);
        }
        // 检查用户名是否已存在
        if (userMapper.existsByUsername(userRegisterDTO.getUsername())) {
            throw new RegisterFailedException(MessageConstant.ACCOUNT_EXIST);
        }

        if (userRegisterDTO.getEmailCode() == null ||
                userRegisterDTO.getEmailCode().isEmpty() ||
                !oldCode.equals(userRegisterDTO.getEmailCode())){
            throw new EmailErrorException
                    (MessageConstant.EMAIL_VERIFICATION_CODE_ERROR);
        }
        User user = new User();
        //对象属性拷贝
        BeanUtils.copyProperties(userRegisterDTO, user);
        //密码加密
        //MD5加密
//        user.setPassword(DigestUtils.md5DigestAsHex
//                (user.getPassword().getBytes()));

        //passwordEncoder加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        //设置账号的状态，默认正常状态 1表示正常 0表示锁定
        user.setStatus(StatusConstant.ENABLE);

        userMapper.insert(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //根据用户名查询数据库中的数据
        User user = userMapper.getByUsername(username);
        if(Objects.isNull(user)){
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }

        //把查询到的user结果，封装成UserDetails类型，然后返回。
        //但是由于UserDetails是个接口，所以我们需要先新建LoginUser类，作为UserDetails的实现类
        return new LoginUser(user);
    }


    /**
     * @Description: 登录（security）
     * @Author: mingri31164
     * @Date: 2025/1/21 18:33
     **/

    public LoginUser login(UserLoginDTO userLoginDTO) {
        // 用户在登录页面输入的用户名和密码
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(), userLoginDTO.getPassword());

        try {
            // 获取 AuthenticationManager 的 authenticate 方法来进行用户认证
            Authentication authenticate = authenticationManager.authenticate(authenticationToken);

            // 认证成功后，提取 LoginUser
            LoginUser loginUser = (LoginUser) authenticate.getPrincipal();

            // 检查用户状态
            if (loginUser.getUser().getStatus().equals(StatusConstant.DISABLE)) {
                throw new LoginFailedException(MessageConstant.ACCOUNT_LOCKED);
            }

            // 把完整的用户信息存入 Redis，其中 userid 作为 key
            redisUtils.set(RedisConstant.USER_INFO_PREFIX +
                    loginUser.getUser().getId().toString(), loginUser);
            return loginUser;

        } catch (AuthenticationException e) {
            // 认证失败处理
            throw new LoginFailedException(MessageConstant.LOGIN_ERROR);
        }
    }
}
