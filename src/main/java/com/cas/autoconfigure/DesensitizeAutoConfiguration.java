package com.cas.autoconfigure;

import com.cas.Interceptor.DecryptOutMybatisInterceptor;
import com.cas.Interceptor.EncryptInMybatisInterceptor;
import com.cas.Interceptor.SqlCostInterceptor;
import com.cas.service.Desensitize;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xiang_long
 * @version 1.0
 * @date 2022/6/8 9:24 上午
 * @desc
 */
@Configuration
@ConditionalOnProperty(prefix = "cas.desensitize", name = "enable", havingValue = "true")
public class DesensitizeAutoConfiguration {

    private final Desensitize desensitize;

    public DesensitizeAutoConfiguration(ObjectProvider<Desensitize> desensitizes) {
        this.desensitize = desensitizes.getIfAvailable();
    }

    @Bean
    public DecryptOutMybatisInterceptor decryptOutMybatisInterceptor() {
        return new DecryptOutMybatisInterceptor(desensitize);
    }

    @Bean
    public EncryptInMybatisInterceptor encryptInMybatisInterceptor() {
        return new EncryptInMybatisInterceptor(desensitize);
    }

    @Bean
    public SqlCostInterceptor sqlCostInterceptor() {
        return new SqlCostInterceptor();
    }

}
