package com.sp.dao;

import com.sp.entity.User;

public interface UserDao {

    //注册
    void addUser(User user);

    //登录
    User login(String username,String password);

    //根据用户名查询邮箱
    User getByUsername(String username);

    //修改密码
    void editPassword(User user);

}
