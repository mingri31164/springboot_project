package com.mingri.entity;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: mingri31164
 * @CreateTime: 2025/1/21 17:30
 * @ClassName: LoginUser
 * @Version: 1.0
 */


/**
 * @Description: 用于security读取权限信息
 * @Author: mingri31164
 * @Date: 2025/1/24 15:57
 **/
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails{
    private SysUser sysUser;

    //查询用户权限信息
    private List<String> permissions;

    public LoginUser(SysUser sysUser, List<String> permissions) {
        this.sysUser = sysUser;
        this.permissions = permissions;
    }

    //我们把这个List写到外面这里了，注意成员变量名一定要是authorities，不然会出现奇奇怪怪的问题
    @JSONField(serialize = false) //这个注解的作用是不让下面那行的成员变量序列化存入redis，避免redis不支持而报异常
    private List<SimpleGrantedAuthority> authorities;

    @Override
    //用于返回权限信息。现在我们正在学'认证'，'权限'后面才学。所以返回null即可
    //当要查询用户信息的时候，我们不能单纯返回null，要重写这个方法，作用是封装权限信息
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //当authorities集合为空，就说明是第一次，就需要转换，当不为空就说明不是第一次，就不需要转换直接返回
        if(authorities != null){ //严谨来说这个if判断是避免整个调用链中security本地线程变量在获取用户时的重复解析，和redis存取无关
            return authorities;
        }
        //为空的话就会执行下面的转换代码
        authorities = permissions
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        //最终返回转换结果
        return authorities;
    }


    @Override
    //用于获取用户密码。由于使用的实体类是User，所以获取的是数据库的用户密码
    public String getPassword() {
        return sysUser.getPassword();
    }

    @Override
    //用于获取用户名。由于使用的实体类是User，所以获取的是数据库的用户名
    public String getUsername() {
        return sysUser.getUserName();
    }

    @Override
    //判断登录状态是否过期。把这个改成true，表示永不过期
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    //判断账号是否被锁定。把这个改成true，表示未锁定，不然登录的时候，不让你登录
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    //判断登录凭证是否过期。把这个改成true，表示永不过期
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    //判断用户是否可用。把这个改成true，表示可用状态
    public boolean isEnabled() {
        return true;
    }
}
