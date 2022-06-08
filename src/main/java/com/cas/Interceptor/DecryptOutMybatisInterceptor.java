package com.cas.Interceptor;

import cn.hutool.extra.spring.SpringUtil;
import com.cas.annotation.Confidential;
import com.cas.annotation.ConfidentialType;
import com.cas.service.Desensitize;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AnnotationUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * @author: xianglong[1391086179@qq.com]
 * @date: 下午10:39 2021/3/15
 * @version: V1.0
 * @review: mybatis拦截器
 */
@Intercepts(
        {
                @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}),
        })
@Import(cn.hutool.extra.spring.SpringUtil.class)
public class DecryptOutMybatisInterceptor implements Interceptor {
    private static final Logger log = LoggerFactory.getLogger(DecryptOutMybatisInterceptor.class);

    private Desensitize desensitize;

    public DecryptOutMybatisInterceptor(Desensitize desensitize) {
        if (desensitize != null) {
            this.desensitize = desensitize;
        }
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 检测加解密方法是否存在
        if (desensitize == null) {
            return invocation.proceed();
        }

        Object result = invocation.proceed();
        if (result instanceof Collection) {
            List<Object> resList = new ArrayList<>();
            Collection objList = (Collection) result;
            for (Object obj : objList) {
                resList.add(desensitize(obj));
            }
            return resList;
        } else {
            return desensitize(result);
        }
    }

    private Object desensitize(Object object) throws InvocationTargetException, IllegalAccessException {
        Class<?> aClass = object.getClass();
        ConfidentialType confidentialType = AnnotationUtils.findAnnotation(aClass, ConfidentialType.class);
        if (confidentialType != null) {
            Field[] fields = aClass.getDeclaredFields();

            for (Field field : fields) {
                Confidential confidential = field.getAnnotation(Confidential.class);
                if (confidential == null) {
                    continue;
                }
                PropertyDescriptor ps = BeanUtils.getPropertyDescriptor(object.getClass(), field.getName());
                if (ps.getReadMethod() == null || ps.getWriteMethod() == null) {
                    continue;
                }
                Object val = ps.getReadMethod().invoke(object);
                if (val != null) {
                    // 获取db中的加密数据
                    Object db = ps.getReadMethod().invoke(object);
                    if (db instanceof String) {
                        String mobile = String.valueOf(db);
                        ps.getWriteMethod().invoke(object, desensitize.decryptData(mobile));
                    }
                }
            }
        }
        return object;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ResultSetHandler) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
