package com.mingri.mapper;

import com.github.pagehelper.Page;
import com.mingri.annotation.AutoFill;
import com.mingri.dto.UserPageQueryDTO;
import com.mingri.entity.User;
import com.mingri.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    @Select("select * from users where username = #{username}")
    User getByUsername(String username);

    /**
     * 插入用户数据
     * @param user
     */
    @Insert("insert into users (name, username, password, phone, sex, id_number, create_time, update_time, create_user, update_user,status) " +
            "values " +
            "(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{createTime},#{updateTime},#{createUser},#{updateUser},#{status})")
    @AutoFill(value = OperationType.INSERT)
    void insert(User employee);

    /**
     * 分页查询
     * @param userPageQueryDTO
     * @return
     */
    Page<User> pageQuery(UserPageQueryDTO userPageQueryDTO);

    /**
     * 根据主键动态修改属性
     * @param user
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(User user);

    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    @Select("select * from users where id = #{id}")
    User getById(Long id);

}
