package com.mingri.controller.user;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mingri.constant.JwtClaimsConstant;
import com.mingri.dto.SysUserDTO;
import com.mingri.dto.SysUserLoginDTO;
import com.mingri.dto.SysUserRegisterDTO;
import com.mingri.entity.LoginUser;
import com.mingri.entity.PageQuery;
import com.mingri.entity.SysUser;
import com.mingri.properties.JwtProperties;
import com.mingri.result.Result;
import com.mingri.service.ISysUserService;
import com.mingri.utils.JwtUtil;
import com.mingri.result.PageResult;
import com.mingri.vo.SysInfoVO;
import com.mingri.vo.SysUserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author mingri31164
 * @since 2025-01-22
 */
@RestController
@Slf4j
@Api(tags = "用户相关接口")
@RequestMapping("/sys-user")
public class SysUserController {

    @Autowired
    private ISysUserService iSysUserService;
    @Autowired
    private JwtProperties jwtProperties;


    /**
     * 登录
     * @param userLoginDTO
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<SysUserLoginVO> login(@RequestBody SysUserLoginDTO userLoginDTO) {
        log.info("用户登录：{}", userLoginDTO);

        LoginUser loginUser = iSysUserService.login(userLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, loginUser.getSysUser().getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        SysUserLoginVO userLoginVO = SysUserLoginVO.builder()
                .id(loginUser.getSysUser().getId())
                .userName(loginUser.getUsername())
                .name(loginUser.getSysUser().getNickName())
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
    public Result register(@RequestBody SysUserRegisterDTO userRegisterDTO){
        log.info("新增用户：{}",userRegisterDTO);
        iSysUserService.register(userRegisterDTO);
        return Result.success();
    }


    /**
     * 退出
     * @return
     */
    @ApiOperation("退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        iSysUserService.logout();
        return Result.success();
    }


    /**
     * 用户分页查询
     * @param query
     * @return
     */
    @PostMapping("/page")
    @ApiOperation("用户分页查询")
    @Cacheable(cacheNames = "userPageCache")
// @PreAuthorize("hasAnyAuthority('admin')")
    public PageResult<SysInfoVO> page(@RequestBody PageQuery query) {
        // 1. 分页查询
        Page<SysUser> result = iSysUserService.page(query.toMpPage("update_time", false));
        // 2. 封装并返回
        return PageResult.of(result, SysInfoVO.class);
    }


    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询用户信息")
    public Result<SysUser> getById(@PathVariable Long id){
        SysUser sysUser = iSysUserService.getById(id);
        return Result.success(sysUser);
    }

    /**
     * 编辑用户信息
     * @param userDTO
     * @return
     */
    @PutMapping
    @ApiOperation("编辑用户信息")
    public Result update(@RequestBody SysUserDTO userDTO){
        log.info("编辑用户信息：{}", userDTO);
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(userDTO, sysUser);
        iSysUserService.updateById(sysUser);
        return Result.success();
    }

}
