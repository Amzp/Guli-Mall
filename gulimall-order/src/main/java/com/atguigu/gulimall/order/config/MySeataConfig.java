package com.atguigu.gulimall.order.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class MySeataConfig {

    @Resource
    private DataSourceProperties dataSourceProperties;


    /**
     * 创建并配置数据源。
     *
     * @param dataSourceProperties 数据源配置属性。
     * @return 配置好的数据源代理对象。
     */
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        // 使用DataSourceProperties配置来构建Hikari数据源实例
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();

        // 如果配置中提供了数据源名称，则设置数据源的池名称
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }

        // 返回一个代理数据源，用于可能的进一步封装或安全控制
        return new DataSourceProxy(dataSource);
    }


}
