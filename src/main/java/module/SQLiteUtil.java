package module;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import bouyomi.BouyomiProxy;

public class SQLiteUtil{

	private static Driver driver;
	private String url="jdbc:sqlite:";
	public SQLiteUtil(String path){
		url+=path;
	}
	static{
		try{
			Class<?> c=BouyomiProxy.module.loader.loadClass("org.sqlite.JDBC");
			driver=(Driver) c.newInstance();
		}catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
			e.printStackTrace();
			driver=null;
		}
	}
	public static String EscapeSQL(String raw) {
		StringWriter sw=new StringWriter();
		BufferedWriter bw=new BufferedWriter(sw);
		for(int index=0;index<raw.length();index++) {
			char c=raw.charAt(index);
			try{
				if(c=='\'')bw.append("\'\'");
				else if(c=='\"')bw.append("\"\"");
				else bw.append(c);
			}catch(IOException e){}//出るはずがない
		}
		try{
			bw.flush();
		}catch(IOException e){}//出るはずがない
		return sw.toString();
	}
	public ResultSet insert(String table,ArrayList<String> values) throws SQLException {
		Statement st=getStatement();
		StringBuilder sb=new StringBuilder("insert into ");
		sb.append(table).append(" values(");
		for(String s:values)sb.append(s).append(",");
		sb.deleteCharAt(sb.length()-1).append(")");
		return st.executeQuery(sb.toString());
	}
	public ResultSet createTable(String name,ArrayList<String> values) throws SQLException {
		Connection con=getConnection();
		con.prepareStatement(name).executeQuery();
		Statement st=getStatement();
		StringBuilder sb=new StringBuilder("create table ");
		sb.append(name).append("(");
		for(String s:values)sb.append(s).append(",");
		sb.deleteCharAt(sb.length()-1).append(")");
		return st.executeQuery(sb.toString());
	}
	public Connection getConnection() throws SQLException {
		return driver.connect(url,new Properties());
	}
	public Statement getStatement() throws SQLException {
		return getConnection().createStatement();
	}
}
