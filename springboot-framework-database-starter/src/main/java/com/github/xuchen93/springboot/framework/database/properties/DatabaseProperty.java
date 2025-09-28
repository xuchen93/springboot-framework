package com.github.xuchen93.springboot.framework.database.properties;

import com.zaxxer.hikari.HikariConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties("xuchen93.framework.database")
public class DatabaseProperty {

	private Map<String, DBProperty> multiDatasourceMap = new HashMap<>();


	@Data
	public static class DBProperty {
		/*---------------------------------数据源配置---------------------------------*/
		/**
		 * 必须得有一个primary
		 */
		private boolean primary = false;
		/**
		 * 是否输出日志。对应 StdOutImpl
		 */
		private boolean log = false;
		/**
		 * Datasource name to use if "generate-unique-name" is false. Defaults to "testdb"
		 * when using an embedded database, otherwise null.
		 */
		private String name;

		/**
		 * Fully qualified name of the connection pool implementation to use. By default, it
		 * is auto-detected from the classpath.
		 */
		private Class<? extends DataSource> type;

		/**
		 * Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.
		 */
		private String driverClassName;

		/**
		 * JDBC URL of the database.
		 */
		private String url;

		/**
		 * Login username of the database.
		 */
		private String username;

		private String password;

		private HikariConfig hikari = new HikariConfig();

		/*---------------------------------数据源配置---------------------------------*/
		/*---------------------------------扫码路径配置---------------------------------*/
		/**
		 * mapper文件路径。例如：classpath:mapper/art/*.xml
		 */
		private List<String> mapperLocations = new ArrayList<>();

		/**
		 * 扫描的包路径。例如：com.xxx.service.table.art.dao
		 */
		private List<String> scanPackages = new ArrayList<>();
		/*---------------------------------扫码路径配置---------------------------------*/


	}
}
