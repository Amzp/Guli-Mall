package com.atguigu.gulimall.ware.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
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
     * 创建并配置数据源。该方法配置了应用的数据源，使用HikariCP作为连接池实现。
     * 注意：为了确保事务的正常回滚，需要将返回的数据源包装成DataSourceProxy并设置为主数据源。
     *
     * @param dataSourceProperties 数据源配置属性，用于构建数据源。
     * @return 配置好的数据源实例，被Spring管理的一个Bean。
     */
    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {

        // 使用DataSourceProperties配置来构建一个HikariDataSource实例
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();

        // 如果配置了数据源名称，则设置给HikariDataSource的池名称
        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }

        // 返回一个包装了真实数据源的DataSourceProxy实例，用于支持Spring的事务管理
        return new DataSourceProxy(dataSource);
    }

}