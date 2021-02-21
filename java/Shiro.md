# Shiro

权限控制框架。控制用户角色以及角色对应的权限

## 流程

* 初始化流程

  * 初始化SecurityManager
  * 初始化Realm，设置到SecurityManager中
  * 构建subject对象，以及token对象
  * 使用token登录，并开始验证subject对象拥有的权限

* 登录流程

  token-> Subject.login() -> SecurityMangaer.login()-> authenticator.authenticate()->realm.getAuthenticationInfo()

  由此可见，相关的验证信息是存在realm对象中

## Relam

* IniRealm 

  将用户信息，权限信息，存在一个ini文件中，提供给SecurityManager进行审核。

* JDBCRealm