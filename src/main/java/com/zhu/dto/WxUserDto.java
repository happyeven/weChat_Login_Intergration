package com.zhu.dto;

/**
 * @Author ZHUDO2
 * @Date 9/10/2020 11:46 AM
 * @Version 1.0
 */
public class WxUserDto {

  String openid;
  String nickname;
  Integer sex;
  String headimgurl;

  public WxUserDto() {
  }

  public String getOpenid() {
    return openid;
  }

  public void setOpenid(String openid) {
    this.openid = openid;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public Integer getSex() {
    return sex;
  }

  public void setSex(Integer sex) {
    this.sex = sex;
  }

  public String getHeadimgurl() {
    return headimgurl;
  }

  public void setHeadimgurl(String headimgurl) {
    this.headimgurl = headimgurl;
  }

  public WxUserDto(String openid, String nickname, Integer sex, String headimgurl) {
    this.openid = openid;
    this.nickname = nickname;
    this.sex = sex;
    this.headimgurl = headimgurl;
  }
}
