package com.zhu.controller;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.zhu.dto.WxUserDto;
import com.zhu.util.ConstantWxUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

// @CrossOrigin
@Controller
@RequestMapping("/api/user/wx")
public class WxApiController {

  @GetMapping("/login")
  public String getWxCode() throws UnsupportedEncodingException {
    String baseAuthorizeUrl = getBaseAuthorizeUrl();
    String redirectUrl = getEncodeRedirectUrl();
    // 设置%s值
    String authorizeUrl = getAuthorizeUrl(baseAuthorizeUrl, redirectUrl, "jenkin");
    // 重定向到请求微信地址里面
    return "redirect:" + authorizeUrl;
  }

  private String getAuthorizeUrl(String baseAuthorizeUrl, String redirectUrl, String state) {
    return String.format(baseAuthorizeUrl, ConstantWxUtils.WX_OPEN_APP_ID, redirectUrl, state);
  }

  private String getEncodeRedirectUrl() throws UnsupportedEncodingException {
    // 对redirect_url进行URLEncoder编码
    String redirectUrl = ConstantWxUtils.WX_OPEN_REDIRECT_URL;
    redirectUrl = URLEncoder.encode(redirectUrl, "utf-8");
    return redirectUrl;
  }

  private String getBaseAuthorizeUrl() {
    return "https://open.weixin.qq.com/connect/oauth2/authorize"
        + "?appid=%s"
        + "&redirect_uri=%s"
        + "&response_type=code"
        + "&scope=snsapi_userinfo"
        + "&state=%s"
        + "#wechat_redirect";
  }

  @GetMapping("/index")
  public String test(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    //request.getRequestDispatcher("login.html").forward(request, response);
    //response.sendRedirect("login.html");
    return "index";
  }

  @GetMapping("/test")
  public String getQrCode() {
    return "test";
  }

  @GetMapping("/callback")
  public void callback(@RequestParam("code") String code, @RequestParam("state") String state,
      HttpServletResponse response)
      throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    try {
      /** 将code值作为请求参数，发起获取accsess_token和openid的请求 */
      String baseAccessTokenUrl = getBaseAccessTokenUrl();
      String accessTokenUrl =
          getAccessTokenUrl(
              baseAccessTokenUrl, ConstantWxUtils.WX_OPEN_APP_ID,
              ConstantWxUtils.WX_OPEN_APP_SECRET, code
          );
      ResponseEntity<String> accessTokenInfo =
          restTemplate.getForEntity(accessTokenUrl, String.class);

      System.out.println(
          "access token header" + accessTokenInfo.getHeaders() + "\n" + "access token body"
              + accessTokenInfo.getBody());
      HashMap accessTokenMap = getInfoMap(accessTokenInfo.getBody());
      String access_token = (String) accessTokenMap.get("access_token");
      String openid = (String) accessTokenMap.get("openid");
      /** 将accsess_token和openid作为参数，发起获取用户信息的请求 */
      String baseUserInfoUrl = getBaseUserInfoUrl();
      String userInfoUrl = getUserInfoUrl(baseUserInfoUrl, access_token, openid);
      ResponseEntity<String> userInfo = restTemplate.getForEntity(userInfoUrl, String.class);
      System.out.println(
          "user info header" + userInfo.getHeaders() + "\n" + "user info body" + userInfo
              .getBody());
      WxUserDto wxUserDto = JSON.parseObject(userInfo.getBody(), WxUserDto.class);
      wxUserDto.setHeadimgurl(wxUserDto.getHeadimgurl().replaceAll("\\\\",""));
      System.out.println("userInfo : "+wxUserDto.toString());
      /** 方法回调结束后的跳转地址 */
      //response.addCookie(new Cookie("user", "aaa"));
      String redirectUrl = "http://www.epa.com:3000/login" + "/" + wxUserDto.getOpenid() + "/" + wxUserDto.getNickname();
      System.out.println(redirectUrl);
      response.sendRedirect(redirectUrl);
      // return "redirect:http://www.epa.com:3000/";
    } catch (Exception e) {
      throw new Exception("方法回调出现错误");
    }
  }

  private String getCreateButtonToken(RestTemplate restTemplate) {
    /** 获取access_token，用于发起创建公众号自定义菜单请求，此处的access_token与上面的不一样 */
    String baseTokenUrl = getBaseTokenUrl();
    String tokenUrl =
        getTokenUrl(
            baseTokenUrl, ConstantWxUtils.WX_OPEN_APP_ID, ConstantWxUtils.WX_OPEN_APP_SECRET);
    ResponseEntity<String> tokenInfo = restTemplate.getForEntity(tokenUrl, String.class);
    System.out.println(tokenInfo.getHeaders() + "\n" + tokenInfo.getBody());
    HashMap tokenInfoMap = getInfoMap(tokenInfo.getBody());
    return (String) tokenInfoMap.get("access_token");
  }

  private void setButtonInPublicAccount(RestTemplate restTemplate, String token) {
    /** 发起创建公众号自定义菜单请求 */
    String baseCreateButtonUrl = getBaseCreateButtonUrl();
    String createButtonUrl = String.format(baseCreateButtonUrl, token);
    String buttonJson = getButtonJson();
    ResponseEntity<String> createButtonInfo =
        restTemplate.postForEntity(createButtonUrl, buttonJson, String.class);
    System.out.println(createButtonInfo.getHeaders() + "\n" + createButtonInfo.getBody());
  }

  private String getBaseUserInfoUrl() {
    return "https://api.weixin.qq.com/sns/userinfo" + "?access_token=%s" + "&openid=%s";
  }

  private String getUserInfoUrl(String baseUserInfoUrl, String accessToken, String openId) {
    return String.format(baseUserInfoUrl, accessToken, openId);
  }

  private String getButtonJson() {
    return "{\"button\": [{\"type\": \"view\", \"name\": \"OOCL\", \"key\": \"EMPLOAN_HTML_LOAN\",\"url\": \"http://2wcrat.natappfree.cc/api/user/wx/test\"}]}";
  }

  private String getBaseCreateButtonUrl() {
    return "https://api.weixin.qq.com/cgi-bin/menu/create" + "?access_token=%s";
  }

  private HashMap getInfoMap(String infoBody) {
    Gson gson = new Gson();
    HashMap infoMap = gson.fromJson(infoBody, HashMap.class);
    return infoMap;
  }

  private String getAccessTokenUrl(
      String baseAccessTokenUrl, String appId, String appSecret, String code) {
    return String.format(baseAccessTokenUrl, appId, appSecret, code);
  }

  private String getBaseAccessTokenUrl() {
    return "https://api.weixin.qq.com/sns/oauth2/access_token"
        + "?appid=%s"
        + "&secret=%s"
        + "&code=%s"
        + "&grant_type=authorization_code";
  }

  private String getTokenUrl(String baseTokenUrl, String wxOpenAppId, String wxOpenAppSecret) {
    return String.format(baseTokenUrl, wxOpenAppId, wxOpenAppSecret);
  }

  private String getBaseTokenUrl() {
    return "https://api.weixin.qq.com/cgi-bin/token"
        + "?grant_type=client_credential"
        + "&appid=%s"
        + "&secret=%s";
  }

}
