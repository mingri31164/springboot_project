package com.mingri.service;

import com.mingri.dto.UserDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserPageQueryDTO;
import com.mingri.entity.User;
import com.mingri.result.PageResult;

public interface UserService {

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

    /**
     * 新增用户
     * @param userDTO
     */
    void save(UserDTO userDTO);

    /**
     * 分页查询
     * @param userPageQueryDTO
     * @return
     */
    PageResult pageQuery(UserPageQueryDTO userPageQueryDTO);

    /**
     * 启用禁用用户账号
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 编辑用户信息
     * @param userDTO
     */
    void update(UserDTO userDTO);

}
