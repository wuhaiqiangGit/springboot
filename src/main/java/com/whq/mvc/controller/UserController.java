package com.whq.mvc.controller;

import com.github.pagehelper.PageInfo;
import com.whq.mvc.common.BusinessException;
import com.whq.mvc.common.CommonErrorCode;
import com.whq.mvc.entity.User;
import com.whq.mvc.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/add")
    public int addUser(User user){
        return userService.addUser(user);
    }

    @ResponseBody
    @GetMapping("/getUser/{id}")
    public User getSingleUser(@PathVariable(value = "id") int id){
        if (id > 10){
            throw new BusinessException(CommonErrorCode.E_100101);
        }
        log.info("getSingleUser:::{}",id);
        return userService.selectUserById(id);
    }

    @ResponseBody
    @RequestMapping(value = "/getAllUsers/{pageNo}/{pageSize}")
    public PageInfo<User> getAllUsers(@PathVariable("pageNo") int pageNo, @PathVariable("pageSize") int pageSize){
        log.info("getAllUsers:::{}/{}",pageNo,pageSize);
        return userService.findAllUser(pageNo,pageSize);
    }


    @RequestMapping("/getUserParam")
    public String login(){
        log.info("success");
        return "login";
    }
}
