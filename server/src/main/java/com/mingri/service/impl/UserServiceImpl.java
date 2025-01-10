package com.mingri.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.mingri.constant.MessageConstant;
import com.mingri.constant.PasswordConstant;
import com.mingri.constant.StatusConstant;
import com.mingri.dto.UserDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserPageQueryDTO;
import com.mingri.entity.User;
import com.mingri.exception.AccountLockedException;
import com.mingri.exception.AccountNotFoundException;
import com.mingri.exception.PasswordErrorException;
import com.mingri.mapper.UserMapper;
import com.mingri.mapper.UserMapper;
import com.mingri.result.PageResult;
import com.mingri.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录
     *
     * @param userLoginDTO
     * @return
     */
    public User login(UserLoginDTO userLoginDTO) {
        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        User user = userMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (user == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        //对前端传过来的明文密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (user.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return user;
    }

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

        //设置当前记录的创建时间和修改时间
        //employee.setCreateTime(LocalDateTime.now());
        //employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id
        //employee.setCreateUser(BaseContext.getCurrentId());
        //employee.setUpdateUser(BaseContext.getCurrentId());

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
        // update employee set status = ? where id = ?

        /*Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);*/

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

        //employee.setUpdateTime(LocalDateTime.now());
        //employee.setUpdateUser(BaseContext.getCurrentId());

        userMapper.update(employee);
    }


}
