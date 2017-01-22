package io.transwarp.thread;

import io.transwarp.connUtil.JDBCUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import org.apache.log4j.Logger;

public class WorkThread implements Runnable{
	
	private static final Logger LOG = Logger.getLogger(WorkThread.class);
	
	private Connection conn = null;
	private String[] sqls;
	private long stopTime = -1;
	private String output;
		
	/**
	 * 建立查询线程
	 * @param sqls 查询语句集合
	 * @param stopTime 程序运行时间
	 * @param url jdbc连接串
	 * @param username ldap认证用户名
	 * @param password ldap认证密码
	 */
	public WorkThread(String[] sqls, long stopTime, String output, Connection conn) {
		this.conn  = conn;
		this.output = output;
		this.sqls = sqls;
		this.stopTime = stopTime;
	}
	
	@Override
	public void run() {
		/* 获取执行句柄 */
		Statement stat = null;
		try {
			stat = this.conn.createStatement();
		}catch(Exception e) {
			LOG.error("get statement error : " + e.getMessage());
			return;
		}
		
		/* 执行环境设置语句 */
		try {
			stat.execute("set ngmr.exec.mode = " + JDBCUtil.config.get("mode"));
			stat.execute("set ngmr.sql.print.console = false");
			stat.execute("set inceptor.skip.optimizer = true");
			stat.execute("set ngmr.metacache = true");
		} catch (Exception e) {
			LOG.error("set environment error : " + e.getMessage());
		}
		
		boolean permanent = false;
		if(stopTime == -1) {
			permanent = true;
		}
		int sqlNum = this.sqls.length;
		Random rand = new Random();
		while(true) {
			long start = System.currentTimeMillis();
			/* 从执行sql集合中随机选取执行语句 */
			try {
				stat.execute(sqls[rand.nextInt(sqlNum)]);
			} catch (SQLException e) {
				LOG.error("execute sql is error : " + e.getMessage());
			}
			if(output.equals("true")) {
				try {
					ResultSet rs = stat.getResultSet();
					/* 获取列名 */
					ResultSetMetaData rsmd = rs.getMetaData();
					int size = rsmd.getColumnCount();
					StringBuffer result = new StringBuffer();
					for(int i = 1; i <= size; i++) {
						if(i != 1) result.append(",");
						result.append(rsmd.getColumnName(i));
					}
					/* 获取数据 */
					while(rs.next()) {
						result.append("\n");
						for(int i = 1; i <= size; i++) {
							if(i != 1) result.append(",");
							result.append(rs.getString(i));
						}
					}
					System.out.println(result.toString());
				}catch(Exception e) {
					LOG.error("get result of sql is error : " + e.getMessage());
				}
			}
			long end = System.currentTimeMillis();
			long cost = end - start;
			stopTime -= cost;
			CountThread.count.addAndGet(1);
			CountThread.latency.addAndGet(cost);
			if(!permanent && stopTime <= 0) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}
	}

}
