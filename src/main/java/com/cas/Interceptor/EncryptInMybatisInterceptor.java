package com.cas.Interceptor;

import com.cas.annotation.Confidential;
import com.cas.annotation.ConfidentialType;
import com.cas.service.Desensitize;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: xianglong[1391086179@qq.com]
 * @date: 下午10:39 2021/3/15
 * @version: V1.0
 * @review: mybatis拦截器
 */
@Intercepts(
        {
                @Signature(type = ParameterHandler.class, method = "setParameters", args = {PreparedStatement.class}),
        })
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class EncryptInMybatisInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(EncryptInMybatisInterceptor.class);

    private Desensitize desensitize;

    public EncryptInMybatisInterceptor(Desensitize desensitize) {
        if (desensitize != null) {
            this.desensitize = desensitize;
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 检测 加密方式是否存在
        if (desensitize == null) {
            return invocation.proceed();
        }
        ParameterHandler parameterHandler = (ParameterHandler) invocation.getTarget();
        // 获取参数对像，即 mapper 中 paramsType 的实例
        Field parameterField = parameterHandler.getClass().getDeclaredField("parameterObject");
        parameterField.setAccessible(true);
        //取出实例
        Object parameterObject = parameterField.get(parameterHandler);
        if (parameterObject != null) {
            Class<?> parameterObjectClass = parameterObject.getClass();
            //校验该实例的类是否被@ConfidentialType所注解
            ConfidentialType confidentialType = AnnotationUtils.findAnnotation(parameterObjectClass, ConfidentialType.class);
            if (Objects.nonNull(confidentialType)) {
                //取出当前当前类所有字段，传入加密方法
                List<Field> declaredFields = new ArrayList<>();
                getFieldList(parameterObjectClass, declaredFields);
                for (Field field : declaredFields) {
                    Confidential confidential = field.getAnnotation(Confidential.class);
                    if (!Objects.isNull(confidential)) {
                        field.setAccessible(true);
                        Object object = field.get(parameterObject);
                        if (object instanceof String) {
                            String value = (String) object;
                            if (checkRegular(confidential, value)) {
                                // 满足属性校验则加密
                                field.set(parameterObject, desensitize.encryptData(value));
                            }
                        }
                    }
                }
            }
        }
        return invocation.proceed();
    }

    /**
     * 属性校验
     * 长度 > 正则 > 其他
     * @return
     * @param confidential
     * @param value
     */
    public boolean checkRegular(Confidential confidential, String value) {
        // 1、检查是否开启属性校验
        if (!confidential.value()) {
            return true;
        }

        // 2、长度校验
        if (value.length() > confidential.max()) {
            return false;
        }

        // 3、正则校验
        String regular = confidential.regular();
        // 3.1 正则为空代表不校验
        if ("".equals(regular)) {
            return true;
        }
        Pattern pattern = Pattern.compile(regular);
        Matcher matcher = pattern.matcher(value);
        // 4、通过返回true, 失败返回false
        return matcher.matches();
    }

    private void getFieldList(Class<?> clazz, List<Field> fieldList) {
        if (null == clazz) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        fieldList.addAll(Arrays.asList(fields));
        /** 处理父类字段**/
        Class<?> superClass = clazz.getSuperclass();
        if (superClass.equals(Object.class)) {
            return;
        }
        getFieldList(superClass, fieldList);
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
