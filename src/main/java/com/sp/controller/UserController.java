package com.sp.controller;

import com.sp.entity.User;
import com.sp.service.UserService;
import com.sp.utils.SendEmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@RequestMapping("/userController")
public class UserController {

    @Autowired
    private UserService userService;


    @RequestMapping("/success")
    public String toSuccess() {
        return "success";
    }

    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/toRegister")
    public String toRegister() {
        return "register";
    }

    @RequestMapping("/toForgetPassword")
    public String toForgetPassword() {
        return "forgetPassword";
    }


    //发送验证码
    @RequestMapping("/sendCode")
    public void sendCode(String email, HttpSession session) {
        //随机生成4位数验证码
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            sb.append((int) (Math.random() * 10));
        }
        System.out.println(sb.toString());

        //设置标题
        String title = "邮箱验证码";

        try {
            //发送邮件
            SendEmailUtils.sendEmail(title,sb.toString(),email);
        } catch (Exception e) {
            //当邮箱不存在时。。。
            e.printStackTrace();
        }

        //将验证码存储进Session
        session.setAttribute("CODE",sb.toString());
        System.out.println("发送成功！");
    }


    //注册
    @RequestMapping("/addUser")
    public String addUser(User user, HttpServletRequest request) {
        //获取邮箱的真实验证码
        String code = (String) request.getSession().getAttribute("CODE");

        //首先判断前台输入的验证码是否正确
        if(!code.equals(user.getCode())) {
            //如果不正确
            return "redirect:/userController/toRegister";
        }

        //如果正确，那就注册成功
        userService.addUser(user);
        return "redirect:/userController/toLogin";
    }


    //登录
    @RequestMapping("/login")
    public String login(String username,String password) {
        User user = userService.login(username, password);

        if(user == null) {
            //密码或账号错误
            return "redirect:/userController/toLogin";
        }
        //登录成功！
        return "redirect:/userController/success";
    }

    private Integer USER_ID;

    //找回密码
    @RequestMapping("/forgetPassword")
    @ResponseBody
    public String forgetPassword(String username,HttpServletRequest request) {
        User user = userService.getByUsername(username);

        //该用户名不存在
        if(user == null) {
            return "false";
        }


        String email = user.getEmail();
        String title = "找回密码";
        //localhost/userController/toEditPassword/"+user.getId()+"
        String content = "<h2>点击以下链接，即可设置新密码</h2>" +
                "<a href='http://127.0.0.1/userController/toEditPassword/"+user.getId()+"'>设置新密码</a>";

        try {
            SendEmailUtils.sendEmail(title,content,email);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        USER_ID = user.getId();  //将要修改密码的用户的id赋值给全局变量
        System.out.println("找回密码邮件发送成功！");
        return "true";
    }


    //跳转至修改密码界面
    @RequestMapping("/toEditPassword/{id}")
    public String toEditPassword(@PathVariable Integer id, Model model,HttpServletRequest request) {
        model.addAttribute("id",id);
        return "editPassword";
    }


    //修改新密码
    @RequestMapping("/editPassword")
    public void editPassword(User user, HttpServletResponse response) throws ServletException, IOException {
        userService.editPassword(user);
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write("密码修改成功！3秒后跳转至登录页面。。。。。。。。");
        response.setHeader("refresh", "3;url=/userController/toLogin");
    }


}
