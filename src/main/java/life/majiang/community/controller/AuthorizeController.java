package life.majiang.community.controller;

import life.majiang.community.dto.AccessTokenDTO;
import life.majiang.community.dto.GithubUser;
import life.majiang.community.mapper.UserMapper;
import life.majiang.community.model.User;
import life.majiang.community.provider.GithubProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class AuthorizeController {
//github 认证 跳转  登入
    //Autowired 它可以对类成员变量、方法及构造函数进行标注，完成自动装配的工作
    @Autowired
    private GithubProvider githubProvider;
    //把私密属性 卸载application.pro里  用value调用增加安全性 属性不能多打空格
    @Autowired
    private UserMapper usermapper;

    @Value("${github.client.id}")
    private String clientid;
    @Value("${github.client.secret}")
    private String clientSecret;
    @Value("${github.redirect.url}")
    private String redirectUri;

    @GetMapping("/callback")
    public String callback(@RequestParam(name="code")String code,
                           @RequestParam(name="state") String state,
                           HttpServletRequest request ,
                           HttpServletResponse response){

        //okhttp 处理get post请求  赋值方法
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
       accessTokenDTO.setClient_id(clientid);
        accessTokenDTO.setClient_secret(clientSecret);
       accessTokenDTO.setRedirect_uri(redirectUri);
        accessTokenDTO.setCode(code);
        accessTokenDTO.setState(state);
        String accessToken=githubProvider.getAccessToken(accessTokenDTO);
        GithubUser githubUser=githubProvider.getUser(accessToken);
       if (githubUser !=null){
           //写入cookies
           User user =new User();
           String token = UUID.randomUUID().toString();
           user.setToken(token);
           user.setName(githubUser.getName());
           user.setAccountId(String.valueOf(githubUser.getId()));
           user.setGmtCreate(System.currentTimeMillis());
           user.setGmtModified(user.getGmtCreate());
           //登入成功显示我 不成功显示登入

           //插入数据库
           usermapper.insert(user);

           //登录成功 写cookie 和session
            response.addCookie(new Cookie("token",token));


           return "redirect:/";
       }else{
           //登入失败 重新登入
           return "redirect:/";
        }
    }
}
