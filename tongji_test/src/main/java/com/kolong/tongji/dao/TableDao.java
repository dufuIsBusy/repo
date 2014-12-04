package com.kolong.tongji.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.kolong.tongji.factory.ConnectionFactory;
import com.kolong.tongji.factory.ConnectionPool;
import com.kolong.tongji.util.JsonUtil;
import com.kolong.tongji.vo.Entity;
import com.kolong.tongji.vo.Table;
import com.kolong.tongji.vo.WeiBo;

public class TableDao {
	private static final Logger logger = Logger.getLogger(TableDao.class);
	private static final ConnectionFactory factory = new ConnectionFactory();
	private static final String dbName = "data_0008";
	
	public static void main(String[] args) throws ParseException {
		// List<Table> tables = new TableDao().getUUU();
		// for(Table t : tables) {
		// System.out.print(t.getUserCount() + "    ");
		// System.out.print(t.getSite_name() + "     ");
		// System.out.println(t.getDomain_1() );
		// }
		GregorianCalendar rq = new GregorianCalendar(2014, 4, 21, 0, 0, 0);

		GregorianCalendar calendar1 = new GregorianCalendar(rq.get(Calendar.YEAR), rq.get(Calendar.MONTH), rq.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		GregorianCalendar calendar2 = new GregorianCalendar(rq.get(Calendar.YEAR), rq.get(Calendar.MONTH), rq.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
		long c1 = calendar1.getTimeInMillis();
		long c2 = calendar2.getTimeInMillis();

		System.out.println(c1 / 1000);
		System.out.println(c2 / 1000);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = "2014-5-21 00:00:00";
		Date endDate = df.parse(date);
		long cc2 = endDate.getTime();
		long interval = 1000 * 60 * 60 * 24;
		long cc1 = (c2 - interval) / 1000;
		System.out.println(cc1 + "--" + cc2);
		
	}

	/**
	 * ��������Ի�ȡ�����б�
	 * 
	 * @param userId ��ҵid
	 * @param sim_id �����id
	 * @param pageNo  ҳ��
	 * @param pageSize ÿҳ��ʾ��������¼
	 * @return ���������б�
	 */
	public List<Table> getTitle(String userId, String simId, int pageNo, int pageSize) {
		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		
		String sql = "select id, site_name,site_url,url, created, title ,user_id,uuid from reports_{userId} where sim_id = {sim_id} ORDER BY created desc limit {startRowNo},{pageSize}";
		sql = sql.replace("{userId}", userId).replace("{sim_id}", simId).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitle(String userId, String sim_id, int pageNo):" + sql);
		}
		List<Table> tables = new ArrayList<Table>();
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setSite_name(rs.getString("site_name"));
						table.setSite_url(rs.getString("site_url"));
						table.setUrl(rs.getString("url"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setUser_id(rs.getInt("user_id"));
						table.setUuid(rs.getString("uuid"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	/**
	 * ��ȡ�ܼ�¼��(ȫ��ҵ)
	 * @param c1
	 * @param c2
	 * @return �ܼ�¼��
	 */
	public int getCount(long c1, long c2,boolean isFilter) {
		
		List<String> ids = queryUserIds();
		// ����sql
		StringBuilder sb = new StringBuilder("select count(id) from (");
		sb.append("select * from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append("(SELECT id,title FROM `reports_"+ ids.get(i) +"` where created between {c1} and {c2} )");
		}
		
		sb.append(") as t  ");
		if(isFilter) {
			sb.append("GROUP BY t.title");
		}
		sb.append("	) as tt");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2);
		
		if(logger.isDebugEnabled()) {
			logger.debug("getCount:" + sql);
		}
		
		Connection dataConn = factory.getConn(dbName);
		
		int totalCount = 0;
		try {
			QueryRunner runner = new QueryRunner();
			totalCount = runner.query(dataConn, sql, new ResultSetHandler<Integer>() {
				
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(!rs.next()) {
						return 0;
					}
					return rs.getInt(1);
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return totalCount;
	}
	/**
	 * ȫ��ҵĳ����վ��Ӧ�ķ���������
	 * @param c1
	 * @param c2
	 * @param domain_1
	 * @param isFilter true ���˱��⣬ false������
	 * @return
	 */
	public int getDomain_1TitleCount(long c1, long c2, String domain_1, boolean isFilter) {
		
		List<String> ids = queryUserIds();
		// ����sql
		StringBuilder sb = new StringBuilder("select count(id) from (");
		sb.append("select * from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append("(SELECT id,title FROM `reports_"+ ids.get(i) +"` where created between {c1} and {c2} and domain_1 = '{domain_1}' )");
		}
		
		sb.append(") as t  ");
		if(isFilter) {
			sb.append("GROUP BY t.title");
		}
		sb.append("	) as tt");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2).replace("{domain_1}", domain_1);
		
		if(logger.isDebugEnabled()) {
			logger.debug("getDomain_1TitleCount:" + sql);
		}
		
		Connection dataConn = factory.getConn(dbName);
		
		int totalCount = 0;
		try {
			QueryRunner runner = new QueryRunner();
			totalCount = runner.query(dataConn, sql, new ResultSetHandler<Integer>() {
				
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(!rs.next()) {
						return 0;
					}
					return rs.getInt(1);
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return totalCount;
	}
	/**
	 * ��ȡ�ܼ�¼����ϸ����ҵ��
	 * @param userId ��ҵid
	 * @param sim_id ������id
	 * @return �ܼ�¼��
	 */
	public int getCountFor20(long c1, long c2, String userId,boolean isFilter) {
		
		String sql = "select count(t.id) from ("
				+ "select id from reports_{userId} where created between {c1} and {c2} ";
		
		if(isFilter) {
			sql += " GROUP BY title";
		}
		sql +=") as t;";
		
		sql = sql.replace("{c1}",c1+"").replace("{c2}",c2+"").replace("{userId}", userId);
		if (logger.isDebugEnabled()) {
			logger.debug("getCountFor20():" + sql);
		}
		Connection dataConn = factory.getConn(dbName);
		
		int totalCount = 0;
		try {
			QueryRunner runner = new QueryRunner();
			totalCount = runner.query(dataConn, sql, new ResultSetHandler<Integer>() {
				
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(!rs.next()) {
						return 0;
					}
					return rs.getInt(1);
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return totalCount;
	}
	/**
	 * ϸ����ҵĳһ����վ�ķ���������
	 * @param c1
	 * @param c2
	 * @param userId
	 * @param isFilter
	 * @return
	 */
	public int getDomain_1TitleCountFor20(long c1, long c2, String domain_1, String userId,boolean isFilter) {
		
		String sql = "select count(t.id) from ("
				+ "select id from reports_{userId} where created between {c1} and {c2} and domain_1 = '{domain_1}'";
				
		if(isFilter) {
			sql += " GROUP BY title";
		}
		sql +=") as t;";
		
		sql = sql.replace("{c1}",c1+"").replace("{c2}",c2+"").replace("{userId}", userId).replace("{domain_1}", domain_1);
		if (logger.isDebugEnabled()) {
			logger.debug("getDomain_1TitleCountFor20():" + sql);
		}
		Connection dataConn = factory.getConn(dbName);
		
		int totalCount = 0;
		try {
			QueryRunner runner = new QueryRunner();
			totalCount = runner.query(dataConn, sql, new ResultSetHandler<Integer>() {
				
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(!rs.next()) {
						return 0;
					}
					return rs.getInt(1);
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return totalCount;
	}
	/**
	 * ��ȡ�ܼ�¼��
	 * @param userId ��ҵid
	 * @param sim_id ������id
	 * @return �ܼ�¼��
	 */
	public int getTitleCount(String userId, String simId) {
		
		String sql = "select count(id) from reports_{userId} where sim_id = {sim_id}";
		sql = sql.replace("{userId}", userId).replace("{sim_id}", simId);
		if (logger.isDebugEnabled()) {
			logger.debug("getTitle(String userId, String sim_id):" + sql);
		}
		Connection dataConn = factory.getConn(dbName);
		
		int totalCount = 0;
		try {
			QueryRunner runner = new QueryRunner();
			totalCount = runner.query(dataConn, sql, new ResultSetHandler<Integer>() {
				
				@Override
				public Integer handle(ResultSet rs) throws SQLException {
					if(!rs.next()) {
						return 0;
					}
					return rs.getInt(1);
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return totalCount;
	}
	/**
	 * ��ȡȫ��ҵ�������б�ĳһ����վ��
	 * 
	 * @param c1
	 * @param c2
	 * @param domain_1
	 * @return
	 */
	public List<Table> getTitleTest(long c1, long c2, int pageNo, int pageSize, String domain_1, Boolean isFilter) {
		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		
		List<String> ids = queryUserIds();
		StringBuilder sb = new StringBuilder();
		sb.append("select t.id ,t.user_id,t.site_name,t.site_cls, t.url, t.domain_1,t.created,t.similar_count,t.uuid, t.`view`, t.title from ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append("UNION All");
			}
			sb.append("(SELECT id,user_id ,site_name,site_cls,url,domain_1, created, title ,similar_count,uuid, view FROM `reports_" + ids.get(i))
				.append("` where created between {c1} and {c2} and domain_1 = '{domain_1}')");
		}
		sb.append(") t");
		if(isFilter) {
			sb.append(" GROUP BY t.title ");
		}
		sb.append(" ORDER BY t.similar_count desc LIMIT {startRowNo},{pageSize};");
		
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{domain_1}", domain_1).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleTest():" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setSite_cls(rs.getInt("site_cls"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setUuid(rs.getString("uuid"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	/**
	 * ��ȡȫ��ҵ�������б�(������վ)
	 * 
	 * @param c1
	 * @param c2
	 * @param domain_1
	 * @return
	 */
	public List<Table> getTitle(long c1, long c2, int pageNo, int pageSize, Boolean isFilter) {

		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		
		List<String> ids = queryUserIds();
		StringBuilder sb = new StringBuilder();
		sb.append("select t.id ,t.user_id,t.site_name,t.site_cls, t.url, t.domain_1,t.created,t.similar_count,t.uuid, t.`view`, t.title from ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append("UNION All");
			}
			sb.append("(SELECT id,user_id ,site_name,site_cls,url,domain_1, created, title ,similar_count,uuid, view FROM `reports_" + ids.get(i))
				.append("` where created between {c1} and {c2} )");
		}
		sb.append(") t");
		if(isFilter) {
			sb.append(" GROUP BY t.title ");
		}
		sb.append(" ORDER BY t.similar_count desc LIMIT {startRowNo},{pageSize};");
		
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitle():" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setUuid(rs.getString("uuid"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * ��ȡϸ����ҵ�������б�ĳһ����վ��
	 * 
	 * @param c1
	 * @param c2
	 * @param domain_1
	 * @return
	 */
	public List<Table> getTitleTestFor20(long c1, long c2, int pageNo, int pageSize, String domain_1, String userId,Boolean isFilter) {
		
		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		String sql = "SELECT id,user_id, site_name, site_cls,domain_1, created, title , view, similar_count,uuid, url FROM `reports_{userId}` where created between {c1} and {c2} and domain_1 = '{domain_1}' ";
		if(isFilter) {
			sql += " GROUP BY title ";
		}
		sql += "ORDER BY similar_count desc LIMIT {startRowNo},{pageSize};";
		
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{userId}", userId).replace("{domain_1}", domain_1).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleTestFor20():" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setSite_cls(rs.getInt("site_cls"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setUuid(rs.getString("uuid"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	
	/**
	 * ��ȡϸ����ҵ�������б�������վ��
	 * 
	 * @param c1
	 * @param c2
	 * @param domain_1
	 * @return
	 */
	public List<Table> getTitleFor20(long c1, long c2, int pageNo, int pageSize, String userId,Boolean isFilter) {

		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		String sql = "SELECT id,user_id, site_name, site_cls,domain_1, created, title , view, similar_count,uuid, url FROM `reports_{userId}` where created between {c1} and {c2} ";
		if(isFilter) {
			sql += " GROUP BY title ";
		}
		sql += "ORDER BY similar_count desc LIMIT {startRowNo},{pageSize};";
		
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{userId}", userId).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleFor20():" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setUuid(rs.getString("uuid"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * ������д��б�
	 * 
	 * @return
	 */
	public List<String> getMins() {

		Connection conn = ConnectionPool.getConn();

		List<String> mins = null;
		try {
			QueryRunner runner = new QueryRunner();
			mins = runner.query(conn, "select id, word from sensitive_word", new ResultSetHandler<List<String>>() {

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> mins = new ArrayList<String>();
					while (rs.next()) {
						mins.add(rs.getString("word"));
					}

					return mins;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			ConnectionPool.closeConn(conn);
		}

		return mins;
	}

	/**
	 * ȫ��ҵ-��ȡ�������ת�����������������б�
	 * 
	 * @param c1
	 * @param c2
	 * @param top
	 *            ����ǰ������
	 * @param orderType
	 *            �����ֶ�����
	 * @return
	 */
	public List<Table> getTitlePTest(long c1, long c2, int pageNo, int pageSize, String orderType,Boolean isFilter) {
		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		
		List<String> ids = queryUserIds();
		// ����sql
		StringBuilder sb = new StringBuilder("select * from ( ");
		sb.append("select * from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append(" (SELECT id,sim_id, user_id,site_name, title , view,similar_count,uuid, reply,url,created FROM `reports_"+ids.get(i)+"` where created between {c1} and {c2} )");
		}
		
		sb.append(") as t ORDER BY t.{orderType} DESC ")
			.append(") as tt  ");
			if(isFilter) {
				sb.append(" GROUP BY tt.title ");
			}
		sb.append("ORDER BY tt.{orderType} desc LIMIT {startRowNo},{pageSize}");
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{orderType}", orderType).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitlePTest:" + isFilter + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setView(rs.getInt("view"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	public List<Table> getTitleP(long c1, long c2, int top, String orderType) {

		List<String> ids = queryUserIds();
		String sql = getSQLTitleP(ids, top, orderType);

		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleP:" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setTitle(rs.getString("title"));
						table.setView(rs.getInt("view"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * 20����ҵ-��ȡ�������ת�����������������б�
	 * @param c1
	 * @param c2
	 * @param top
	 * @param orderType
	 * @param userId
	 * @return
	 */
	public List<Table> getTitlePFor20Test(long c1, long c2, int pageNo, int pageSize, String orderType, String userId,Boolean isFilter) {
		
		if(pageNo <= 0) return new ArrayList<Table>();
		int startRowNo = (pageNo-1)*pageSize;
		String sql = "select * from ( "
						+"SELECT id, user_id,site_name, title , view,similar_count,uuid, reply,url,created FROM `reports_{userId}` where created between {c1} and {c2}  ORDER BY  {orderType}  desc"
						+") as t  ";
		if(isFilter) {
			sql += " GROUP BY t.title ";
		}
		sql += "ORDER BY t.{orderType} DESC limit {startRowNo},{pageSize};";
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{userId}", userId).replace("{orderType}", orderType).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleP:" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setUuid(rs.getString("uuid"));
						table.setTitle(rs.getString("title"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setView(rs.getInt("view"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	public List<Table> getTitlePFor20(long c1, long c2, int top, String orderType, String userId) {

		String sql = "SELECT id, user_id,site_name, title , view,similar_count,uuid, reply,url,created FROM `reports_" + userId + "` where created between {c1} and {c2} and id=sim_id ORDER BY  " + orderType + "  desc LIMIT " + top;
		
//		if(pageNo <= 0) return new ArrayList<Table>();
//		int startRowNo = (pageNo-1)*pageSize;
//		String sql2 = "select * from ( "
//						+"SELECT id, user_id,site_name, title , view,similar_count,uuid, reply,url,created FROM `reports_{userId}` where created between {c1} and {c2}  ORDER BY  {orderType}  desc"
//						+") as t GROUP BY t.title ORDER BY t.{orderType} DESC limit {startRowNo},{pageSize};";

//		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{userId}", userId).replace("{orderType}", orderType).replace("{startRowNo}", startRowNo+"").replace("{pageSize}", pageSize+"");
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");
		if (logger.isDebugEnabled()) {
			logger.debug("getTitleP:" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setUuid(rs.getString("uuid"));
						table.setTitle(rs.getString("title"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setView(rs.getInt("view"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * ȫ��ҵ-���д���������б�snatch_time ----> created ��
	 * @param c1
	 * @param c2
	 * @return
	 */
	public List<Table> getArticleList(long c1, long c2) {
		
		List<String> ids = queryUserIds();
		String sql = getSQLTitleP(ids);
		
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");
		if (logger.isDebugEnabled()) {
			logger.debug("getArticleList:" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setUuid(rs.getString("uuid"));
						table.setTitle(rs.getString("title"));
						table.setView(rs.getInt("view"));
						table.setReprint(0);
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						table.setDescription(rs.getString("description"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	
	/**
	 * ϸ����ҵ-���д���������б�snatch_time ----> created ��
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public List<Table> getArticleListFor20(long c1, long c2, String userId) {
		
		String sql = "SELECT id, user_id,site_name,title,view,reply,url,uuid, description,created FROM `reports_" + userId + "` where created between {c1} and {c2} and id=sim_id group by title ORDER BY id";
		
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");
		if (logger.isDebugEnabled()) {
			logger.debug("getArticleList" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUser_id(rs.getInt("user_id"));
						table.setUuid(rs.getString("uuid"));
						table.setTitle(rs.getString("title"));
						table.setView(rs.getInt("view"));
						table.setReprint(0);
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setReply(rs.getInt("reply"));
						table.setUrl(rs.getString("url"));
						table.setDescription(rs.getString("description"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return tables;
	}
	/**
	 * ȫ��ҵ�����-���д���������б�snatch_time ----> created ��
	 * <p>1�����������ڳ��ָ���ʵ������б�
	 * <p>2����������ڳ��ָ���ʵ������б�
	 * <p>3�����������ڳ������дʵ������б�
	 * <p>4����������ڳ������дʵ������б�
	 * <p> �����б�����Խ�������
	 * @param c1
	 * @param c2
	 * @return
	 */
	public List<Table> getSidesTitle(long c1, long c2,String name,String top) {

		List<String> ids = queryUserIds();
		// ����sql
		StringBuilder sb = new StringBuilder("select id,user_id,title,url,uuid,similar_count,site_name,created from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append("select id,user_id,title,url,uuid,similar_count,site_name,created from reports_" + ids.get(i) + " where description like '%{name}%' and created BETWEEN {c1} and {c2} and id = sim_id");
		}
		
		sb.append(") as t ORDER BY t.similar_count desc LIMIT {top}");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2).replace("{name}", name).replace("{top}", top);

		if (logger.isDebugEnabled()) {
			logger.debug("getSidesTitle:" + sql);
		}
		List<Table> tables = new ArrayList<Table>();
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setTitle(rs.getString("title"));
						table.setSite_name(rs.getString("site_name"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * ϸ����ҵ�����-���д���������б�snatch_time ----> created ��
	 * <p>1�����������ڳ��ָ���ʵ������б�
	 * <p>2����������ڳ��ָ���ʵ������б�
	 * <p>3�����������ڳ������дʵ������б�
	 * <p>4����������ڳ������дʵ������б�
	 * <p> �����б�����Խ�������
	 * @param c1
	 * @param c2
	 * @return
	 */
	public List<Table> getSidesTitleFor20(long c1, long c2,String name,String top, String userId) {

		String sql = "select id,user_id,title,url,uuid,similar_count,site_name,created from reports_{userId} where description like '%{name}%' and created BETWEEN {c1} and {c2} and id = sim_id ORDER BY similar_count desc LIMIT {top}";

		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "").replace("{userId}", userId).replace("{name}", name).replace("{top}", top);
		if (logger.isDebugEnabled()) {
			logger.debug("getSidesTitleFor20:" + sql);
		}
		List<Table> tables = new ArrayList<Table>();
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setTitle(rs.getString("title"));
						table.setSimilar_count(rs.getInt("similar_count"));
						table.setUrl(rs.getString("url"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * #ȫ��ҵ�²��뱨��ʳƷ��ȫ����վ����ʡ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getCountryCount(String rq1, String rq2) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select tt.province_id, area.`name`,count(tt.province_id) as sCount from (");
		sb.append("select s.domain_1, s.province_id from ")
		.append("(select DISTINCT domain_1 from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}') AS t ")
		.append("INNER JOIN (SELECT DISTINCT domain_1,province_id from info_v2.sites) AS s USING(domain_1)")
		.append(") as tt RIGHT JOIN (select id,name from info_v2.areas where pid = 0) as area ON (area.id = tt.province_id) GROUP BY area.id;");
		String sql = sb.toString();
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getCountryCount:" + sql);
		}
		
		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("sCount"));
						entity.setName(rs.getString("name"));
						entity.setCount2(rs.getInt("province_id"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return entities;
	}
	/**
	 * #ϸ����ҵ�²��뱨��ʳƷ��ȫ����վ����ʡ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getCountryCountFor20(String rq1, String rq2,String userId) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select tt.province_id, area.`name`,count(tt.province_id) as sCount from (");
		sb.append("select s.domain_1, s.province_id from ")
			.append("(select DISTINCT domain_1 from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}' and userId = {userId}) AS t ")
			.append("INNER JOIN (SELECT DISTINCT domain_1,province_id from info_v2.sites) AS s USING(domain_1)")
			.append(") as tt RIGHT JOIN (select id,name from info_v2.areas where pid = 0) as area ON (area.id = tt.province_id) GROUP BY area.id;");
		String sql = sb.toString();
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getCountryCountFor20:" + sql);
		}
		
		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("sCount"));
						entity.setName(rs.getString("name"));
						entity.setCount2(rs.getInt("province_id"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return entities;
	}
	
	/**
	 * ȫ��ҵ��������Դ����
	 * @param c1 ��ʼʱ��
	 * @param c2 ����ʱ��
	 * @return ���ؿ����飬���м���ֵ������
	 */
	public List<Entity> getSite_cls(long c1, long c2) {
		List<String> ids = queryUserIds();
		Connection dataConn = factory.getConn(dbName);
		// ����sql
		StringBuilder sb = new StringBuilder("select site_cls,count(site_cls) as sCount from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append(" SELECT site_cls FROM `reports_"+ ids.get(i) +"` where created BETWEEN {c1} and {c2}");
		}
		
		sb.append(") as t GROUP BY t.site_cls");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getSite_cls:" + sql);
		}
		
		List<Entity> entities = new ArrayList<Entity>();
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(dataConn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setId(rs.getInt("site_cls"));
						entity.setCount(rs.getInt("sCount"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return entities;
	}
	/**
	 * ϸ����ҵ��������Դ����
	 * @param c1 ��ʼʱ��
	 * @param c2 ����ʱ��
	 * @return ���ؿ����飬���м���ֵ������
	 */
	public List<Entity> getSite_clsFor20(long c1, long c2, String userId) {
		Connection dataConn = factory.getConn(dbName);
		// ����sql
		StringBuilder sb = new StringBuilder("SELECT site_cls, count(id) as sCount FROM `reports_{userId}` where created BETWEEN {c1} and {c2} GROUP BY site_cls");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getSite_clsFor20:" + sql);
		}
		
		List<Entity> entities = new ArrayList<Entity>();
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(dataConn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setId(rs.getInt("site_cls"));
						entity.setCount(rs.getInt("sCount"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return entities;
	}
	/**
	 * ȫ��ҵ�ۻ������,�ۻ�ת�������ۻ���������Դ����
	 * @param c1 ��ʼʱ��
	 * @param c2 ����ʱ��
	 * @param type ���������ת����������������֮һ
	 * @return ���ؿ����飬���м���ֵ������
	 */
	public List<Entity> getVSROfSite_cls(long c1, long c2,String type) {
		List<String> ids = queryUserIds();
		Connection dataConn = factory.getConn(dbName);
		// ����sql
		StringBuilder sb = new StringBuilder("select site_cls,sum(`{type}`) as sCount from (");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append(" SELECT site_cls,`{type}` FROM `reports_"+ ids.get(i) +"` where created BETWEEN {c1} and {c2}");
		}
		
		sb.append(") as t GROUP BY t.site_cls");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2).replace("{type}", type);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getVSROfSite_cls:" + sql);
		}
		
		List<Entity> entities = new ArrayList<Entity>();
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(dataConn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setId(rs.getInt("site_cls"));
						entity.setCount(rs.getInt("sCount"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return entities;
	}
	/**
	 * ϸ����ҵ�ۻ������,�ۻ�ת�������ۻ���������Դ����
	 * @param c1 ��ʼʱ��
	 * @param c2 ����ʱ��
	 * @param type ���������ת����������������֮һ
	 * @return ���ؿ����飬���м���ֵ������
	 */
	public List<Entity> getVSROfSite_clsFor20(long c1, long c2,String type, String userId) {
		Connection dataConn = factory.getConn(dbName);
		// ����sql
		StringBuilder sb = new StringBuilder("SELECT site_cls, sum(`{type}`) as sCount FROM `reports_{userId}` where created BETWEEN {c1} and {c2} GROUP BY site_cls");
		String sql = sb.toString();
		sql = sql.replace("{c1}", ""+c1).replace("{c2}", ""+c2).replace("{type}", type).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getVSROfSite_clsFor20:" + sql);
		}
		
		List<Entity> entities = new ArrayList<Entity>();
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(dataConn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setId(rs.getInt("site_cls"));
						entity.setCount(rs.getInt("sCount"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}
		
		return entities;
	}
	
	/**
	 * ĳһ�����д��ڷ��������ڵ�һ���仯��������粻�ϸ�(ȫ��ҵ)
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getMinCountForOne(String c1, String c2, String minWord) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select * from  (");
		sb.append("(select sum(min_count) as totalCount,rq from min_count where rq BETWEEN '{c1}' and '{c2}' and min_word = '{minWord}' GROUP BY rq order by rq)")
		.append("UNION ALL (select totalCount,rq from count_0 where rq BETWEEN '{c1}' and '{c2}')").append(") t GROUP BY t.rq;");
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{minWord}", minWord);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getMinCountForOne:" + sql);
		}
		
		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("totalCount"));
						entity.setRq(rs.getDate("rq"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return entities;
	}
	
	/**
	 * ĳһ�����д��ڷ��������ڵ�һ���仯��������粻�ϸ�(ϸ����ҵ)
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getMinCountForOneFor20(String c1, String c2, String minWord, String userId) {
		Connection conn = ConnectionPool.getConn();
		
		// ����sql
		StringBuilder sb = new StringBuilder("select * from  (");
		sb.append("(select sum(min_count) as totalCount,rq from min_count where rq BETWEEN '{c1}' and '{c2}' and min_word = '{minWord}' and userId = {userId} GROUP BY rq order by rq)")
		.append("UNION ALL (select totalCount,rq from count_0 where rq BETWEEN '{c1}' and '{c2}')").append(") t GROUP BY t.rq;");
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{minWord}", minWord).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getMinCountForOneFor20:" + sql);
		}
		
		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("totalCount"));
						entity.setRq(rs.getDate("rq"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return entities;
	}
	/**
	 * ĳһ��������ڷ��������ڵ�һ���仯������������(ȫ��ҵ)
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getSidesCountForOne(String c1, String c2, String sideWord) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select * from  (");
		sb.append("(select sum(side_count) as totalCount,rq from side_count where rq BETWEEN '{c1}' and '{c2}' and side_word = '{sideWord}' GROUP BY rq order by rq)")
				.append("UNION ALL (select totalCount,rq from count_0 where rq BETWEEN '{c1}' and '{c2}')").append(") t GROUP BY t.rq;");
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{sideWord}", sideWord);

		if (logger.isDebugEnabled()) {
			logger.debug("getSidesCountForOne:" + sql);
		}

		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {

				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("totalCount"));
						entity.setRq(rs.getDate("rq"));
						entities.add(entity);
					}

					return entities;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}

		return entities;
	}

	/**
	 * ĳһ��������ڷ��������ڵ�һ���仯������������(ϸ����ҵ)
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getSidesCountForOneFor20(String c1, String c2, String sideWord, String userId) {
		Connection conn = ConnectionPool.getConn();
		
		// ����sql
		StringBuilder sb = new StringBuilder("select * from  (");
		sb.append("(select sum(side_count) as totalCount,rq from side_count where rq BETWEEN '{c1}' and '{c2}' and side_word = '{sideWord}' and userId = {userId} GROUP BY rq order by rq)")
				.append("UNION ALL (select totalCount,rq from count_0 where rq BETWEEN '{c1}' and '{c2}')").append(") t GROUP BY t.rq;");
		String sql = sb.toString();
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{sideWord}", sideWord).replace("{userId}", userId);

		if (logger.isDebugEnabled()) {
			logger.debug("getSidesCountForOneFor20:" + sql);
		}

		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {

				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setCount(rs.getInt("totalCount"));
						entity.setRq(rs.getDate("rq"));
						entities.add(entity);
					}

					return entities;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}

		return entities;
	}

	/**
	 * #��v��֤��δ��֤���û�����ϸ����ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @return List<WeiBo>
	 */
	public List<WeiBo> getVerifiedCountFor20(String c1, String c2,String userId) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select user_verified ,count(uid) as v_count from (");
		sb.append("select DISTINCT uid,user_verified from weibo_data where rq BETWEEN '{c1}' and '{c2}' and userId = {userId}")
			.append(") as t GROUP BY t.user_verified;");
		String sql = sb.toString();
		
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getVerifiedCountFor20:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setV_count(rs.getInt("v_count"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * #��v��֤��δ��֤���û�����ȫ��ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @return List<WeiBo>
	 */
	public List<WeiBo> getVerifiedCount(String c1, String c2) {
		Connection conn = ConnectionPool.getConn();
		// ����sql
		StringBuilder sb = new StringBuilder("select user_verified ,count(uid) as v_count from (");
		sb.append("select DISTINCT uid,user_verified from weibo_data where rq BETWEEN '{c1}' and '{c2}' ")
			.append(") as t GROUP BY t.user_verified;");
		String sql = sb.toString();
		
		sql = sql.replace("{c1}", c1).replace("{c2}", c2);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getVerifiedCount:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setV_count(rs.getInt("v_count"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * #������top10��΢�����������Ƽ��Ƿ��V(���ת������ͬ��������������)--ϸ����ҵ
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<WeiBo> getCommCountTop10For20(String c1, String c2, String top,String userId) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select wid,uid,comm,rt,`user`,user_verified,sour,userId,text,rq from weibo_data where rq BETWEEN '{c1}' and '{c2}' AND userId = {userId}  ORDER BY comm desc,rt DESC limit {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getRtCountTop10For20:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setWid(rs.getString("wid"));
						weiBo.setUid(rs.getString("uid"));
						weiBo.setComm(rs.getInt("comm"));
						weiBo.setRt(rs.getInt("rt"));
						weiBo.setUser(rs.getString("user"));
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setSour(rs.getString("sour"));
						weiBo.setText(rs.getString("text"));
						weiBo.setUserId(rs.getInt("userId"));
						weiBo.setRq(rs.getDate("rq"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * #������top10��΢�����������Ƽ��Ƿ��V(���ת������ͬ��������������)--ȫ��ҵ
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<WeiBo> getCommCountTop10(String c1, String c2, String top) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select wid,uid,comm,rt,`user`,user_verified,sour,userId,text,rq from weibo_data where rq BETWEEN '{c1}' and '{c2}'  ORDER BY comm DESC,rt desc limit {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getCommCountTop10:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setWid(rs.getString("wid"));
						weiBo.setUid(rs.getString("uid"));
						weiBo.setComm(rs.getInt("comm"));
						weiBo.setRt(rs.getInt("rt"));
						weiBo.setUser(rs.getString("user"));
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setSour(rs.getString("sour"));
						weiBo.setUserId(rs.getInt("userId"));
						weiBo.setRq(rs.getDate("rq"));
						weiBo.setText(rs.getString("text"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * #ת����top10��΢�����������Ƽ��Ƿ��V(���ת������ͬ��������������)--ϸ����ҵ
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<WeiBo> getRtCountTop10For20(String c1, String c2, String top,String userId) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select wid,uid,comm,rt,`user`,user_verified,sour,userId,text,rq from weibo_data where rq BETWEEN '{c1}' and '{c2}' AND userId = {userId}  ORDER BY rt DESC,comm desc limit {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top).replace("{userId}", userId);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getRtCountTop10For20:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setWid(rs.getString("wid"));
						weiBo.setUid(rs.getString("uid"));
						weiBo.setComm(rs.getInt("comm"));
						weiBo.setRt(rs.getInt("rt"));
						weiBo.setUser(rs.getString("user"));
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setSour(rs.getString("sour"));
						weiBo.setUserId(rs.getInt("userId"));
						weiBo.setRq(rs.getDate("rq"));
						weiBo.setText(rs.getString("text"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * #ת����top10��΢�����������Ƽ��Ƿ��V(���ת������ͬ��������������)--ȫ��ҵ
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<WeiBo> getRtCountTop10(String c1, String c2, String top) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select wid,uid,comm,rt,`user`,user_verified,sour,userId,text,rq from weibo_data where rq BETWEEN '{c1}' and '{c2}'  ORDER BY rt DESC,comm desc limit {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getRtCountTop10:" + sql);
		}
		
		List<WeiBo> weiBos = null;
		try {
			QueryRunner runner = new QueryRunner();
			weiBos = runner.query(conn, sql, new ResultSetHandler<List<WeiBo>>() {
				
				@Override
				public List<WeiBo> handle(ResultSet rs) throws SQLException {
					List<WeiBo> weiBos = new ArrayList<WeiBo>();
					while (rs.next()) {
						WeiBo weiBo = new WeiBo();
						weiBo.setWid(rs.getString("wid"));
						weiBo.setUid(rs.getString("uid"));
						weiBo.setComm(rs.getInt("comm"));
						weiBo.setRt(rs.getInt("rt"));
						weiBo.setUser(rs.getString("user"));
						weiBo.setUser_verified(rs.getInt("user_verified"));
						weiBo.setSour(rs.getString("sour"));
						weiBo.setText(rs.getString("text"));
						weiBo.setUserId(rs.getInt("userId"));
						weiBo.setRq(rs.getDate("rq"));
						weiBos.add(weiBo);
					}
					
					return weiBos;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return weiBos;
	}
	/**
	 * ������д��б�ȫ��ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getMinCountTop5(String c1, String c2, String top) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select min_word,sum(min_count) as totalCount from lexicon.min_count where rq BETWEEN '{c1}' and '{c2}' GROUP BY min_word ORDER BY sum(min_count) DESC LIMIT {top}";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top);
		
		if (logger.isDebugEnabled()) {
			logger.debug("getMinCountTop5:" + sql);
		}
		
		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {
				
				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setName(rs.getString("min_word"));
						entity.setCount(rs.getInt("totalCount"));
						entities.add(entity);
					}
					
					return entities;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}
		
		return entities;
	}
	
	/**
	 * ������д��б�ϸ����ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getMinCountTop5For20(String c1, String c2, String top, String userId) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select min_word,sum(min_count) as totalCount from min_count where rq BETWEEN '{c1}' and '{c2}' and userId = {userId} GROUP BY min_word ORDER BY sum(min_count) DESC LIMIT {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top).replace("{userId}", userId);

		if (logger.isDebugEnabled()) {
			logger.debug("getMinCountTop5For20:" + sql);
		}

		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {

				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setName(rs.getString("min_word"));
						entity.setCount(rs.getInt("totalCount"));
						entities.add(entity);
					}

					return entities;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}

		return entities;
	}
	
	/**
	 * ��ø�����б�ȫ��ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getSidesCountTop5(String c1, String c2, String top) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select side_word,sum(side_count) as totalCount from lexicon.side_count where rq BETWEEN '{c1}' and '{c2}' GROUP BY side_word ORDER BY sum(side_count) DESC LIMIT {top}";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top);

		if (logger.isDebugEnabled()) {
			logger.debug("getSidesCountTop5:" + sql);
		}

		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {

				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setName(rs.getString("side_word"));
						entity.setCount(rs.getInt("totalCount"));
						entities.add(entity);
					}

					return entities;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}

		return entities;
	}

	/**
	 * ��ø�����б�ϸ����ҵ��
	 * 
	 * @param c1
	 *            ��ʼ����
	 * @param c2
	 *            ��������
	 * @param top
	 *            ǰ����
	 * @return List<Entity>
	 */
	public List<Entity> getSidesCountTop5For20(String c1, String c2, String top, String userId) {
		Connection conn = ConnectionPool.getConn();
		String sql = "select side_word,sum(side_count) as totalCount from side_count where rq BETWEEN '{c1}' and '{c2}' and userId = {userId} GROUP BY side_word ORDER BY sum(side_count) DESC LIMIT {top};";
		sql = sql.replace("{c1}", c1).replace("{c2}", c2).replace("{top}", top).replace("{userId}", userId);

		if (logger.isDebugEnabled()) {
			logger.debug("getSidesCountTop5For20:" + sql);
		}

		List<Entity> entities = null;
		try {
			QueryRunner runner = new QueryRunner();
			entities = runner.query(conn, sql, new ResultSetHandler<List<Entity>>() {

				@Override
				public List<Entity> handle(ResultSet rs) throws SQLException {
					List<Entity> entities = new ArrayList<Entity>();
					while (rs.next()) {
						Entity entity = new Entity();
						entity.setName(rs.getString("side_word"));
						entity.setCount(rs.getInt("totalCount"));
						entities.add(entity);
					}

					return entities;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(conn);
		}

		return entities;
	}

	/**
	 * ��ø��������б�
	 * 
	 * @return
	 */
	public List<Table> getSidesContTop5(long c1, long c2, String top) {

		List<String> ids = queryUserIds();
		String sql = getSQLSidesContTop5(ids, top);

		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");
		if (logger.isDebugEnabled()) {
			logger.debug("getSidesContTop5:" + sql);
		}
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setTitle(rs.getString("title"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setUrl(rs.getString("url"));
						table.setSide_wrong_count(rs.getInt("side_wrong_count"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	public List<Table> getSidesContTop5For20(long c1, long c2, String top, String userId) {

		String sql = "select id,user_id,title,url,uuid,side_wrong_count,site_name,created from  `reports_" + userId
				+ "` where created between {c1} and {c2} and id=sim_id GROUP BY title ORDER BY side_wrong_count desc limit " + top;
		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");

		if (logger.isDebugEnabled()) {
			logger.debug(sql);
		}

		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setId(rs.getInt("id"));
						table.setUuid(rs.getString("uuid"));
						table.setUser_id(rs.getInt("user_id"));
						table.setTitle(rs.getString("title"));
						table.setSite_name(rs.getString("site_name"));
						table.setPubdate(JsonUtil.convertToDate(rs.getInt("created")));
						table.setUrl(rs.getString("url"));
						table.setSide_wrong_count(rs.getInt("side_wrong_count"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	/**
	 * �����վӰ����(ȫ��ҵ)
	 * 
	 * @return List<Table>
	 */
	public List<Table> getSite_yxl(String rq1, String rq2) {
		
		String sql = getSQLSite_yxl();
		
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2);
		if (logger.isDebugEnabled()) {
			logger.debug("getSite_yxl:" + sql);
		}
		List<Table> tables = null;
		Connection conn = ConnectionPool.getConn();
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(conn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setGrade(rs.getString("grade"));
						table.setSCount(rs.getInt("sCount"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			ConnectionPool.closeConn(conn);
		}
		
		return tables;
	}
	
	public List<Table> getSite_yxlFor20(String rq1, String rq2, String userId) {
		
		StringBuilder sb = new StringBuilder("select CONCAT(lowgrade, '-', highgrade) grade, count(tt.domain_1) sCount from ");
		sb.append("(")
		.append("select s.domain_1, s.site_yxl from ")
		.append("(select DISTINCT domain_1 from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}' and userId = {userId}) AS t ")
		.append("	INNER JOIN (SELECT DISTINCT domain_1,site_yxl from info_v2.sites) AS s USING(domain_1) ")
		.append(") as tt RIGHT JOIN lexicon.pr_grade p on (tt.site_yxl BETWEEN p.lowgrade and p.highgrade) GROUP BY lowgrade,highgrade ORDER BY grade desc;");
		String sql = sb.toString();
		
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2).replace("{userId}", userId);
		if (logger.isDebugEnabled()) {
			logger.debug("getSite_yxlFor20:" + sql);
		}
		List<Table> tables = null;
		Connection conn = ConnectionPool.getConn();
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(conn, sql, new ResultSetHandler<List<Table>>() {
				
				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setGrade(rs.getString("grade"));
						table.setSCount(rs.getInt("sCount"));
						tables.add(table);
					}
					
					return tables;
				}
				
			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			ConnectionPool.closeConn(conn);
		}
		
		return tables;
	}
	/**
	 * ����prֵ�ȼ���÷�����ǰ10����վ�б�(ȫ��ҵ)
	 * 
	 * @return List<Table>
	 */
	public List<Table> getSitesTop10(String rq1, String rq2, String pr, String top) {

		String pr1 = "";
		String pr2 = "";
		String[] prs = pr.split("-");
		if(prs.length>=2) {
			pr1 = prs[0];
			pr2 = prs[1];
		}

		StringBuilder sb = new StringBuilder("select tt.domain_1,main.`name`,tt.userCount from ");
		sb.append("(")
			.append("select s.domain_1, s.site_yxl,t.userCount from (")
			.append("select domain_1,sum(userCount) as userCount from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}' GROUP BY domain_1")
			.append(") as t INNER JOIN ")
			.append("(SELECT DISTINCT domain_1,site_yxl from info_v2.sites where site_project = 1) AS s USING(domain_1) ")
			.append("where s.site_yxl BETWEEN {pr1} and {pr2} ORDER BY t.userCount desc LIMIT {top}")
			.append(") as tt INNER JOIN info_v2.mainsites as main USING(domain_1) ORDER BY tt.userCount DESC ;");
		String sql = sb.toString();
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2).replace("{pr1}", pr1).replace("{pr2}", pr2).replace("{top}", top);
		if (logger.isDebugEnabled()) {
			logger.debug("getSitesTop10:" + sql);
		}
		
		List<Table> tables = null;
		Connection conn = ConnectionPool.getConn();
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(conn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setSite_name(rs.getString("name"));
						table.setDomain_1(rs.getString("domain_1"));
						table.setUserCount(rs.getInt("userCount"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			ConnectionPool.closeConn(conn);
		}

		return tables;
	}

	/**
	 * ����prֵ�ȼ���÷�����ǰ10����վ�б�(ϸ����ҵ)
	 * @param pr ��վprֵ�ȼ�
	 * @return List<Table>
	 */
	public List<Table> getSitesTop10For20(String rq1, String rq2, String pr, String userId,String top) {

		String pr1 = "";
		String pr2 = "";
		String[] prs = pr.split("-");
		if(prs.length>=2) {
			pr1 = prs[0];
			pr2 = prs[1];
		}

		StringBuilder sb = new StringBuilder("select tt.domain_1,main.`name`,tt.userCount from ");
		sb.append("(")
			.append("select s.domain_1, s.site_yxl,t.userCount from (")
			.append("select domain_1,sum(userCount) as userCount from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}' and userId = {userId} GROUP BY domain_1")
			.append(") as t INNER JOIN ")
			.append("(SELECT DISTINCT domain_1,site_yxl from info_v2.sites where site_project = 1) AS s USING(domain_1) ")
			.append("where s.site_yxl BETWEEN {pr1} and {pr2} ORDER BY t.userCount desc LIMIT {top}")
			.append(") as tt INNER JOIN info_v2.mainsites as main USING(domain_1) ORDER BY tt.userCount DESC ;");
		String sql = sb.toString();
		sql = sql.replace("{rq1}", rq1).replace("{rq2}", rq2).replace("{pr1}", pr1).replace("{pr2}", pr2).replace("{top}", top).replace("{userId}", userId);
		if (logger.isDebugEnabled()) {
			logger.debug("getSitesTop10For20:" + sql);
		}
		
		List<Table> tables = null;
		Connection conn = ConnectionPool.getConn();
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(conn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setSite_name(rs.getString("name"));
						table.setDomain_1(rs.getString("domain_1"));
						table.setUserCount(rs.getInt("userCount"));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			ConnectionPool.closeConn(conn);
		}

		return tables;
	}

	public List<Table> getUUU(long c1, long c2, int top) {

		List<String> ids = queryUserIds();
		String sql = getSQL(ids, top);

		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");

		if (logger.isDebugEnabled()) {
			logger.debug(sql);
		}

		if(logger.isDebugEnabled()) {
			logger.debug(sql);
		}
		
		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setUserCount(rs.getInt(1));
						table.setSite_name(rs.getString(2));
						table.setDomain_1(rs.getString(3));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	public List<Table> getUUUFor20(long c1, long c2, int top, String userId) {

		StringBuilder sb = new StringBuilder("select ids,name, domain_1 from (");
		sb.append("SELECT count(id) as ids, site_name,domain_1 FROM `reports_" + userId + "` where created between {c1} and {c2} GROUP BY domain_1 ORDER BY count(id) desc LIMIT  " + top)
		.append(") t INNER JOIN info_v2.mainsites USING(domain_1) ORDER BY ids desc");
		String sql = sb.toString();

		sql = sql.replace("{c1}", c1 + "").replace("{c2}", c2 + "");

		if (logger.isDebugEnabled()) {
			logger.debug("getUUUFor20:"+ sql);
		}

		List<Table> tables = null;
		Connection dataConn = factory.getConn(dbName);
		try {
			QueryRunner runner = new QueryRunner();
			tables = runner.query(dataConn, sql, new ResultSetHandler<List<Table>>() {

				@Override
				public List<Table> handle(ResultSet rs) throws SQLException {
					List<Table> tables = new ArrayList<Table>();
					while (rs.next()) {
						Table table = new Table();
						table.setUserCount(rs.getInt(1));
						table.setSite_name(rs.getString(2));
						table.setDomain_1(rs.getString(3));
						tables.add(table);
					}

					return tables;
				}

			});
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			factory.close(dataConn);
		}

		return tables;
	}

	public List<String> queryUserIds() {
		String [] str = {"1500","1501","1502","1503","1504","1505","1506","1507","1508","1509","1510","1511","1512","1513","1514","1515","1516","1517","1518","1519"};
		List<String> ids = new ArrayList<String>();
		for(String id : str) {
			ids.add(id);
		}
		return ids;
		
	}

	public List<Entity> queryUserNames() {
		List<Entity> entities = new ArrayList<Entity>();
		
		String[][] str = {
				{"����ʳƷ","1500"},
				{"��ͷʳƷ","1501"},
				{"����","1502"},
				{"��ζƷ","1503"},
				{"����","1504"},
				{"����Ʒ","1505"},
				{"����Ʒ","1506"},
				{"��Ҷ","1507"},
				{"�ǹ���","1508"},
				{"���༰����Ʒ��","1509"},
				{"������","1510"},
				{"��ʳ��","1511"},
				{"ʳ����","1512"},
				{"ˮ��Ʒ��","1513"},
				{"����","1514"},
				{"����Ʒ","1515"},
				{"����ʳƷ","1516"},
				{"����ʳƷ","1517"},
				{"ʳƷ��Ӽ�","1518"},
				{"����ʳƷ","1519"}
				};
		
		for(String[] name : str) {
			Entity ent = new Entity();
			ent.setName(name[0]);
			ent.setUrl(name[1]);
			entities.add(ent);
		}
		return entities;

	}

	private String getSQL(List<String> ids, int top) {
		StringBuilder sb = new StringBuilder("select ids,name, domain_1 from (");
		sb.append("select count(t.id) as ids ,t.site_name, t.domain_1  from ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append("UNION All");
			}
			sb.append("(SELECT id, site_name,domain_1 FROM `reports_" + ids.get(i) + "` where created between {c1} and {c2})");
		}

		sb.append(") t GROUP BY t.domain_1 ORDER BY count(t.id) desc limit ").append(top)
		.append(") t INNER JOIN info_v2.mainsites USING(domain_1) ORDER BY ids desc");

		return sb.toString();
	}

	private String getSQLTitleP(List<String> ids, int top, String orderType) {
		StringBuilder sb = new StringBuilder();
		sb.append("select t.id ,t.site_name,t.user_id, t.`view`,t.similar_count, t.reply, t.title, t.url,t.uuid,t.created from  ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append("UNION All");
			}
			sb.append("(SELECT id, site_name, user_id,title , view,similar_count, reply,url,uuid,created FROM `reports_" + ids.get(i) + "` where created between {c1} and {c2} and id=sim_id)");
		}

		sb.append(") t ORDER BY t." + orderType + " desc LIMIT " + top);

		return sb.toString();
	}

	private String getSQLTitleP(List<String> ids) {
		StringBuilder sb = new StringBuilder();
		sb.append("select t.id ,t.user_id,t.site_name, t.`view`, t.reply, t.title,t.url, t.description,t.uuid,t.created from  ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append("UNION All");
			}
			sb.append("(SELECT id, user_id,site_name, title ,url, view, reply,description,uuid,created FROM `reports_" + ids.get(i) + "` where created between {c1} and {c2} and id=sim_id)");
		}

		sb.append(") t group by t.title ");

		return sb.toString();
	}

	private String getSQLSite_yxl() {
		StringBuilder sb = new StringBuilder("select CONCAT(lowgrade, '-', highgrade) grade, count(tt.domain_1) sCount from ");
		sb.append("(")
			.append("select s.domain_1, s.site_yxl from ")
			.append("(select DISTINCT domain_1 from domain_1_count where rq BETWEEN '{rq1}' and '{rq2}') AS t ")
			.append("	INNER JOIN (SELECT DISTINCT domain_1,site_yxl from info_v2.sites) AS s USING(domain_1) ")
			.append(") as tt RIGHT JOIN lexicon.pr_grade p on (tt.site_yxl BETWEEN p.lowgrade and p.highgrade) GROUP BY lowgrade,highgrade ORDER BY grade desc;");

		return sb.toString();
	}

	private String getSQLSidesContTop5(List<String> ids, String top) {
		StringBuilder sb = new StringBuilder();
		sb.append("select DISTINCT id,uuid,user_id,title, url,side_wrong_count,site_name,created from  ").append("(");
		for (int i = 0; i < ids.size(); i++) {
			if (i > 0) {
				sb.append(" UNION All ");
			}
			sb.append(" select id,uuid,user_id,title, url,side_wrong_count,site_name,created from `reports_" + ids.get(i) + "` where created between {c1} and {c2} and id=sim_id ");
		}

		sb.append(") t GROUP BY t.title ORDER BY side_wrong_count desc limit " + top);

		return sb.toString();
	}

	public ConnectionFactory getFactory() {
		return factory;
	}

}
