package com.github.xuchen93.springboot.framework.database.configuration;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.github.xuchen93.springboot.framework.database.properties.DatabaseProperty;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 多数据源Bean注册器
 */
@Slf4j
@Component(MultiDBAutoRegister.REGISTER_BEAN_NAME)
public class MultiDBAutoRegister implements ImportBeanDefinitionRegistrar, BeanFactoryAware {

	public static final String REGISTER_BEAN_NAME = "multiDBAutoRegister";

	private ConfigurableEnvironment environment;

	private final PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();


	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.environment = beanFactory.getBean(ConfigurableEnvironment.class);
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
		DatabaseProperty databaseProperty = Binder.get(this.environment)
				.bind("xuchen93.framework.database", DatabaseProperty.class)
				.orElseThrow(() -> new IllegalArgumentException("未配置xuchen93.framework.database数据源信息"));
		if (databaseProperty.getMultiDatasourceMap().isEmpty()) {
			throw new RuntimeException("未配置数据源信息");
		}
		boolean existPrimary = databaseProperty.getMultiDatasourceMap().values().stream().anyMatch(DatabaseProperty.DBProperty::isPrimary);// 检查是否存在默认数据源
		if (!existPrimary) {
			databaseProperty.getMultiDatasourceMap().values().stream().findFirst().ifPresent(dbConfig -> dbConfig.setPrimary(true)); // 设置默认数据源
		}
		// 遍历所有数据源配置，逐个注册
		for (Map.Entry<String, DatabaseProperty.DBProperty> entry : databaseProperty.getMultiDatasourceMap().entrySet()) {
			String dsName = "sf_" + entry.getKey(); // 数据源名称（唯一标识）
			DatabaseProperty.DBProperty dbConfig = entry.getValue(); // 数据源详细配置

			log.info("[xuchen93-framework]开始注册数据源: {}", entry.getKey());
			try {
				// 1. 注册数据源属性配置
				String dsPropsBeanName = registerDataSourceProperties(registry, dsName, dbConfig);

				// 2. 注册JDBC连接详情
				String jdbcDetailsBeanName = registerJdbcConnectionDetails(registry, dsName, dsPropsBeanName, dbConfig);

				// 3. 注册数据源（Hikari）
				String dataSourceBeanName = registerDataSource(registry, dsName, dsPropsBeanName, jdbcDetailsBeanName, dbConfig);

				// 4. 注册SqlSessionFactory
				String sqlSessionFactoryBeanName = registerSqlSessionFactory(registry, dsName, dataSourceBeanName, dbConfig);

				// 5. 注册SqlSessionTemplate
				registerSqlSessionTemplate(registry, dsName, sqlSessionFactoryBeanName, dbConfig);

				// 6. 注册事务管理器
				registerTransactionManager(registry, dsName, dataSourceBeanName, dbConfig);

				// 7. 注册Mapper扫描器
				registerMapperScanner(registry, dsName, sqlSessionFactoryBeanName, dbConfig);

				log.info("[xuchen93-framework]数据源[{}]注册完成", entry.getKey());
			} catch (Exception e) {
				log.error("[xuchen93-framework]数据源[{}]注册失败", entry.getKey(), e);
				throw new BeanDefinitionStoreException("注册数据源失败: " + entry.getKey(), e);
			}
		}
	}

	/**
	 * 注册数据源属性配置（DataSourceProperties）
	 */
	private String registerDataSourceProperties(BeanDefinitionRegistry registry, String dsName, DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "DataSourceProperties";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceProperties.class);

		// 手动设置配置（覆盖自动绑定，确保优先级）
		builder.addPropertyValue("driverClassName", dbConfig.getDriverClassName());
		builder.addPropertyValue("url", dbConfig.getUrl());
		builder.addPropertyValue("username", dbConfig.getUsername());
		builder.addPropertyValue("password", dbConfig.getPassword());
		builder.addPropertyValue("name", dbConfig.getName());
		builder.addPropertyValue("type", dbConfig.getType());
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册JDBC连接详情（JdbcConnectionDetails）
	 */
	private String registerJdbcConnectionDetails(BeanDefinitionRegistry registry, String dsName, String dsPropsBeanName, DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "JdbcConnectionDetails";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(JdbcConnectionDetails.class);
		builder.setFactoryMethodOnBean("createJdbcConnectionDetails", MultiDBAutoRegister.REGISTER_BEAN_NAME);
		builder.addConstructorArgReference(dsPropsBeanName); // 依赖当前数据源的DataSourceProperties
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册数据源（HikariDataSource）
	 */
	private String registerDataSource(BeanDefinitionRegistry registry, String dsName,
									  String dsPropsBeanName, String jdbcDetailsBeanName,
									  DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "DataSource";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSource.class);
		builder.setFactoryMethodOnBean("createDataSource", MultiDBAutoRegister.REGISTER_BEAN_NAME);
		builder.addConstructorArgReference(dsPropsBeanName);
		builder.addConstructorArgReference(jdbcDetailsBeanName);
		builder.addConstructorArgValue(dbConfig.getHikari()); // 传递Hikari配置
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册SqlSessionFactory
	 */
	private String registerSqlSessionFactory(BeanDefinitionRegistry registry, String dsName,
											 String dataSourceBeanName, DatabaseProperty.DBProperty dbConfig) throws Exception {
		String beanName = dsName + "SqlSessionFactory";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MybatisSqlSessionFactoryBean.class);

		// 设置数据源
		builder.addPropertyReference("dataSource", dataSourceBeanName);
		// 设置VFS
		builder.addPropertyValue("vfs", SpringBootVFS.class);
		// 分页插件
		MybatisPlusInterceptor plusInterceptor = new MybatisPlusInterceptor();
		plusInterceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
		MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
		mybatisConfiguration.setMapUnderscoreToCamelCase(true);
		mybatisConfiguration.addInterceptor(plusInterceptor);
		if (dbConfig.isLog()) {
			mybatisConfiguration.setLogImpl(StdOutImpl.class);
		}
		builder.addPropertyValue("configuration", mybatisConfiguration);
		// 设置MyBatis XML映射文件
		Resource[] resources = dbConfig.getMapperLocations().stream().flatMap(location -> {
			try {
				return Stream.of(resourceLoader.getResources(location));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).toArray(Resource[]::new);
		builder.addPropertyValue("mapperLocations", resources);
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册SqlSessionTemplate
	 */
	private String registerSqlSessionTemplate(BeanDefinitionRegistry registry, String dsName, String sqlSessionFactoryBeanName, DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "SqlSessionTemplate";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(SqlSessionTemplate.class);
		builder.addConstructorArgReference(sqlSessionFactoryBeanName);
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册事务管理器
	 */
	private String registerTransactionManager(BeanDefinitionRegistry registry, String dsName, String dataSourceBeanName, DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "TransactionManager";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(DataSourceTransactionManager.class);
		builder.addConstructorArgReference(dataSourceBeanName);
		builder.setPrimary(dbConfig.isPrimary());
		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
		return beanName;
	}

	/**
	 * 注册Mapper扫描器（绑定当前数据源的SqlSessionFactory）
	 */
	private void registerMapperScanner(BeanDefinitionRegistry registry, String dsName,
									   String sqlSessionFactoryBeanName, DatabaseProperty.DBProperty dbConfig) {
		String beanName = dsName + "MapperScanner";
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperScannerConfigurer.class);

		// 绑定当前数据源的SqlSessionFactory
		builder.addPropertyValue("sqlSessionFactoryBeanName", sqlSessionFactoryBeanName);

		// 设置Mapper接口扫描包（支持多个包）
		List<String> basePackages = dbConfig.getScanPackages();
		if (basePackages == null || basePackages.isEmpty()) {
			throw new IllegalArgumentException("数据源[" + dsName + "]未配置mapperScan（Mapper接口扫描包）");
		}
		builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));

		// 标记为基础设施Bean（spring-native支持）
		builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		builder.setPrimary(dbConfig.isPrimary());

		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	// ------------------------------ 静态工厂方法 ------------------------------

	/**
	 * 创建JdbcConnectionDetails（适配Spring Boot版本）
	 */
	@SneakyThrows
	public static JdbcConnectionDetails createJdbcConnectionDetails(DataSourceProperties dataSourceProperties) {
		// 反射创建PropertiesJdbcConnectionDetails（避免直接依赖内部类）
		Class<? extends JdbcConnectionDetails> clazz = (Class<JdbcConnectionDetails>)
				ClassUtils.forName("org.springframework.boot.autoconfigure.jdbc.PropertiesJdbcConnectionDetails",
						ClassUtils.getDefaultClassLoader());
		return BeanUtils.instantiateClass(clazz.getDeclaredConstructor(DataSourceProperties.class), dataSourceProperties);
	}

	/**
	 * 创建数据源（应用Hikari配置）
	 */
	public static DataSource createDataSource(DataSourceProperties properties,
											  JdbcConnectionDetails connectionDetails,
											  HikariConfig hikariConfig) {
		// 构建Hikari数据源
		HikariDataSource dataSource = DataSourceBuilder.create(properties.getClassLoader())
				.type(HikariDataSource.class)
				.driverClassName(connectionDetails.getDriverClassName())
				.url(connectionDetails.getJdbcUrl())
				.username(connectionDetails.getUsername())
				.password(connectionDetails.getPassword())
				.build();

		// 应用Hikari配置（优先级：配置文件 > 默认值）
		if (hikariConfig != null) {
//			dataSource.setConnectionTimeout(hikariConfig.getConnectionTimeout());
//			dataSource.setIdleTimeout(hikariConfig.getIdleTimeout());
//			dataSource.setMaxLifetime(hikariConfig.getMaxLifetime());
//			dataSource.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
//			dataSource.setMinimumIdle(hikariConfig.getMinimumIdle());
//			dataSource.setValidationTimeout(hikariConfig.getValidationTimeout());
//			dataSource.setLeakDetectionThreshold(hikariConfig.getLeakDetectionThreshold());
			BeanUtil.copyProperties(hikariConfig, dataSource, CopyOptions.create().ignoreNullValue());
			if (!StringUtils.hasText(hikariConfig.getPoolName())) {
				dataSource.setPoolName(properties.getName() != null ? properties.getName() : "pool-" + System.currentTimeMillis());
			}
		}
		return dataSource;
	}

}
