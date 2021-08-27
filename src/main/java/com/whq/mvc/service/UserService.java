package com.whq.mvc.service;

import com.github.pagehelper.PageInfo;
import com.whq.mvc.entity.User;

public interface UserService {

    int addUser(User user);

    PageInfo<User> findAllUser(int pageNum, int pageSize);

    User selectUserById(int userId);
}
