package io.transwarp.connUtil;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * JDBC连接
 */
public class JDBCUtil {

    private final static Logger LOG = Logger.getLogger(JDBCUtil.class);
    public static Configuration config = HBaseConfiguration.create();
    
    //加载配置文件
    static {
        config = HBaseConfiguration.create();
        try {
            config.addResource(new FileInputStream(new File("config/env.xml")));
//            config.addResource(new FileInputStream(new File("conf/hbase-site.xml")));
        }catch(Exception e) {
            LOG.error("load config file error : " + e.getMessage());
        }
        /* 加载classname */
        String className = config.get("classname");
    	if(className == null) {
    		LOG.error("class name is null");
    	}
    	try {
    		Class.forName(className);
    	}catch(Exception e) {
    		LOG.error("load class name error : " + e.getMessage());
    	}
    }
    
    public static Connection getConnection(String url) {
    	return getConnection(url, null, null);
    }
    
    public static Connection getConnection(String url, String username, String password) {
    	LOG.debug("get conn url is : " + url);

    	/* 获取连接 */
    	Connection conn = null;
    	try {
    		if(username == null || username.equals("")) {
    			conn = DriverManager.getConnection(url);
    		}else {
    			conn = DriverManager.getConnection(url, username, password);
    		}
    	}catch(Exception e) {
    		LOG.error("get jdbc connection error : " + e.getMessage());
    	}
    	return conn;
    }

    
}

