package com.example.demo;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserRepository userRepository;

    @RequestMapping("/list")
    public List< User> getAllUsers(){
        return userRepository.findAll();
    }
    @RequestMapping("/byId/{id}")
    public  User byId(@PathVariable("id")long id){
        return userRepository.findUserByIdEquals(id);
    }
}
