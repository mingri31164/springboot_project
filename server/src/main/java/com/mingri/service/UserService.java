package com.mingri.service;

import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.entity.User;

public interface UserService {

    /**
     * 员工登录
     * @param userLoginDTO
     * @return
     */
    User login(UserLoginDTO userLoginDTO);

}
