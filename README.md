### mybatis 属性加解密插件
    平时我们遇到需求，需要对某几个敏感字段进行加密或者解密，你还在针对业务点进行加解密嘛，那就太low了，试试这款加解密插件吧。
    业务代码0侵入，只需要配置注解即可实现加解密

### 使用方式
    0、此jar未上传公共私服，请下载本地仓库进行引用，项目已配置本地上传组件，使用gradle的upload上传本地
    
**引用方式：implementation 'com.cas:mybatis-desensitization:0.0.1'**

    1、配置文件开启
    在application.yml 中配置开启加解密插件
    
```yaml
cas:
  desensitize:
    ## 是否开启加解密插件
    enable: true
```
    2、实现 com.cas.service.Desensitize 接口，里面是加解密方法的具体实现，如果不实现，插件将不处理
```java
@Configuration
public class DesensiteConfig implements Desensitize {

    @Override
    public String encryptData(String s) {
        return s + "123";
    }

    @Override
    public String decryptData(String s) {
        return s + "456";
    }
}
```

    3、在需要加密的类和字段上打注解，类上使用@ConfidentialType ，属性上使用@Confidential
```java
@ConfidentialType
public class Account {

    private String id;
    /**
    * 确认要校验，长度最长为20，正则表达式满足："^\\d{1,10}$"
    */
    @Confidential(value = true, max = 20, regular = "^\\d{1,10}$")
    private String userId;
    private Float balance;
    // ...
}
```
    这样你在传入userId的时候就会自动通过你实现的接口进行加解密。进而节约你的时间和成本
## 如果帮助到您，请给我点一个start，谢谢
![](.README_images/1a93b674.png)