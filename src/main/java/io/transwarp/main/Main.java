package io.transwarp.main;

import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import io.transwarp.connUtil.JDBCUtil;
import io.transwarp.thread.CountThread;
import io.transwarp.thread.WorkThread;

public class Main {
	
	public static void main(String[] args) {
		Main main = new Main();
		main.start();
	}

	private static final Logger LOG = Logger.getLogger(Main.class);
	
	public Main() {
		CountThread.count.set(0);
		CountThread.latency.set(0);
	}
	
	public void start() {
		/* 建立线程池 */
		ExecutorService threadPool = Executors.newCachedThreadPool();
		/* 获取host */
		String host = JDBCUtil.config.get("hosts");
		String[] hosts = host.split(";");
		int hostNum = hosts.length;
		/* 获取sql语句 */
		String[] sqls = JDBCUtil.config.get("sqls", "").split(";");
		/* 获取运行时间 */
		long runtime = JDBCUtil.config.getLong("runtime", -1);
		/* 获取是否输出结果 */
		String output = JDBCUtil.config.get("output");
		/* 根据安全认证模式构建jdbc连接，并建立查询线程 */
		String security = JDBCUtil.config.get("security");
		int threadNum = JDBCUtil.config.getInt("threadNum", 5);
		for(int i = 0; i < threadNum; i++) {
			Connection conn = null;
			StringBuffer url = new StringBuffer("jdbc:hive2://");
			url.append(hosts[i%hostNum]).append(":").append(JDBCUtil.config.get("port", "10000")).append("/").append(JDBCUtil.config.get("database", "default"));
			if(security.equals("ldap")) {
				conn = JDBCUtil.getConnection(url.toString(), JDBCUtil.config.get("username", "hive"), JDBCUtil.config.get("password", "123456"));
			}else if(security.equals("kerberos")) {
				url.append(";").append("principal=").append(JDBCUtil.config.get("principal")).append(";")
					.append("authentication=kerberos;").append("kuser=").append(JDBCUtil.config.get("kuser", "hive")).append(";")
					.append("keytab=").append(JDBCUtil.config.get("keytab", "/etc/inceptorsql1/hive.keytab")).append(";").append("krb5conf=").append(JDBCUtil.config.get("krbconf", "/etc/krb5.conf"));
				conn = JDBCUtil.getConnection(url.toString());
			}else {
				conn = JDBCUtil.getConnection(url.toString());
			}
			LOG.debug("url is " + url.toString());
			threadPool.submit(new WorkThread(sqls, runtime, output, conn));
			LOG.info("build thread success, host : " + hosts[i%hostNum]);
		}
		threadPool.submit(new CountThread(JDBCUtil.config.getLong("interval", 1) * 1000, runtime));
		
	}
}
