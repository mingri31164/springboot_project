package com.mingri.controller.user;

import com.mingri.constant.JwtClaimsConstant;
import com.mingri.context.BaseContext;
import com.mingri.dto.UserDTO;
import com.mingri.dto.UserLoginDTO;
import com.mingri.dto.UserPageQueryDTO;
import com.mingri.dto.UserRegisterDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.User;
import com.mingri.properties.JwtProperties;
import com.mingri.result.PageResult;
import com.mingri.result.Result;
import com.mingri.service.UserService;
import com.mingri.utils.JwtUtil;
import com.mingri.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        LoginUser loginUser = userService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, loginUser.getUser().getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(loginUser.getUser().getId())
                .userName(loginUser.getUsername())
                .name(loginUser.getUser().getName())
                .token(token)
                .build();

        return Result.success(userLoginVO);
    }


    /**
     * @Description: 用户注册
     * @Author: mingri31164
     * @Date: 2025/1/20 18:13
     **/
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO){
        log.info("新增用户：{}",userRegisterDTO);
        userService.register(userRegisterDTO);
        return Result.success();
    }


    /**
     * 退出
     * @return
     */
    @ApiOperation("用户退出")
    @PostMapping("/logout")
    public Result<String> logout() {
        BaseContext.removeCurrentId();
        return Result.success();
    }


    /**
     * 新增用户
     * @param userDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增用户")
    public Result save(@RequestBody UserDTO userDTO){
        log.info("新增用户：{}",userDTO);
        userService.save(userDTO);
        return Result.success();
    }

    /**
     * 用户分页查询
     * @param userPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("用户分页查询")
    @Cacheable(cacheNames = "userPageCache")
    public Result<PageResult> page(UserPageQueryDTO userPageQueryDTO){
        log.info("用户分页查询，参数为：{}", userPageQueryDTO);
        PageResult pageResult = userService.pageQuery(userPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用用户账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用用户账号")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用禁用用户账号：{},{}",status,id);
        userService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户信息")
    public Result<User> getById(@PathVariable Long id){
        User employee = userService.getById(id);
        return Result.success(employee);
    }

    /**
     * 编辑用户信息
     * @param userDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑用户信息")
    public Result update(@RequestBody UserDTO userDTO){
        log.info("编辑用户信息：{}", userDTO);
        userService.update(userDTO);
        return Result.success();
    }

}
