package module;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import bouyomi.BouyomiConection;
import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.DiscordAPI;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class Alarm implements IModule,IAutoSave{

	private final long sleep=5*60*1000;
	/**k=id	v=時刻*/
	private HashMap<String,String> map=new HashMap<String,String>();
	/**k=id	v=message*/
	private HashMap<String,String> messages=new HashMap<String,String>();
	private boolean savedmap;
	public Alarm() {
		try{
			BouyomiProxy.load(map,"Alarm.txt");
			savedmap=true;
		}catch(IOException e){
			e.printStackTrace();
		}
		new AlarmThread().start();
	}
	@Override
	public void call(Tag tag){
		String s=tag.getTag("アラーム取り消し","アラーム取消","アラーム取り消","アラーム取消し");
		if(s!=null) {
			String key=makeKey(tag);
			messages.remove(key);
			String v=map.remove(key);
			if(v==null)tag.chatDefaultHost(tag.con.user+"のアラームは設定されていません");
			else tag.chatDefaultHost(tag.con.user+"のアラームを取り消しました");
		}
		s=tag.getTag("アラーム取得");
		if(s!=null) {
			get(tag,s);
		}
		s=tag.getTag("アラーム");
		if(s!=null) {
			if(s.isEmpty()) {
				//get(tag.con,null);
			}else main(s,tag);
		}
		s=tag.getTag("アラームリスト");
		if(s!=null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
			StringBuilder sb=new StringBuilder("/");
			for(Entry<String, String> es:map.entrySet()) {
				sb.append("\n");
				long l=Long.parseLong(es.getValue());
				String s0=tag.getUserName(es.getKey().split(",")[0]);
				s0+=sdf.format(new Date(l));
				sb.append(s0);
			}
			if(map.isEmpty())sb.append("無し");
			tag.chatDefaultHost(sb.toString());
		}
	}
	private String makeKey(Tag tag){
		return tag.con.userid+","+tag.getGuild().getId()+","+tag.getTextChannel().getId();
	}
	private void get(Tag con,String id) {
		String name=con.con.user;
		if(id==null||id.isEmpty())id=con.con.userid;
		else if(con.con.userid.equals(id));
		else name=con.getUserName(id);
		String key=makeKey(con);
		String v=map.get(key);
		if(v==null)con.chatDefaultHost("/"+name+"のアラームは設定されていません");
		else{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
			String l=sdf.format(new Date(Long.parseLong(v)));
			String m=messages.get(key);
			if(m==null)m="";
			else m="。メッセージ："+m;
			con.chatDefaultHost("/"+name+"のアラームは"+l+"に設定されています"+m);
		}
	}
	private void main(String s, Tag tag) {
		//DiscordAPI.chatDefaultHost("アラーム機能が呼び出されました パラメータ文字列="+s);
		SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy年MM月");
		SimpleDateFormat sdf1 = new SimpleDateFormat("dd日");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
		//sdf.setTimeZone(TimeZone.getTimeZone("UTC+9"));
		try{
			long nd=System.currentTimeMillis();
			Format f=new Format(s);
			String d=null;
			if(s.indexOf("明日の")==0) {
				s=s.substring(3);
				f.s=s;
				d=sdf1.format(new Date(nd+24*60*60*1000L));
			}
			if(s.indexOf("明後日の")==0) {
				s=s.substring(4);
				f.s=s;
				d=sdf1.format(new Date(nd+2*24*60*60*1000L));
			}
			if(d==null)d=f.a(tag.con, "日");
			String h=f.a(tag.con, "時");
			String m=f.a(tag.con, "分");
			String message=null;
			if(f.s!=null&&!f.s.isEmpty())message=f.s;
			Date date = sdf.parse(sdf0.format(new Date())+d+h+m);
			//date.getTime(),sdf.format(date), con.userid
			System.out.println("時間指定は正常に処理されました\n結果="+sdf.format(date));
			StringBuilder sb=new StringBuilder("/");
			sb.append(sdf.format(date)).append("に");
			//if(message!=null)sb.append(" ").append(message).append(" を");
			sb.append("メンションします");
			tag.chatDefaultHost(sb.toString());
			String key=makeKey(tag);
			//System.out.println("登録ID="+key);
			if(message!=null)messages.put(key,message);
			map.put(key,Long.toString(date.getTime()));
			call();
			savedmap=false;
		}catch(Exception e){
			e.printStackTrace();
			Util.chatException(tag,null,e);
		}
	}
	private class AlarmThread2 extends Thread{
		private long sleep;
		private String id;
		public String gid,cid;
		public AlarmThread2(long l,String s,String nick) {
			super("アラーム"+nick);
			sleep=l;
			id=s;
		}
		@Override
		public void run() {
			try{
				if(sleep>0)Thread.sleep(sleep);
				String key=id+","+gid+","+cid;
				if(map.containsKey(key)) {
					StringBuilder sb=new StringBuilder(Util.IDtoMention(id));
					sb.append("アラーム");
					String mes=messages.get(key);
					if(mes!=null)sb.append("\n").append(mes);
					DiscordBOT.DefaultHost.send(gid,cid,sb.toString());
				}
				map.remove(key);
				messages.remove(key);
				savedmap=false;
				System.out.println(getName()+"終了");
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}
	private class AlarmThread extends Thread{
		public AlarmThread() {
			super("アラーム");
		}
		@Override
		public void run() {
			while(true) {
				try{
					call();
					Thread.sleep(sleep);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}
	private void call(){
		long now=System.currentTimeMillis();
		for(Entry<String, String> es:map.entrySet()) {
			long l=Long.parseLong(es.getValue());
			//SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
			//System.out.println(sdf.format(new Date(l)));
			//System.out.println(sdf.format(new Date(now)));
			//System.out.println(Counter.getUserName(es.getKey())+(l-now)+"<"+sleep);
			if(l-now<sleep) {
				String[] k=es.getKey().split(",");
				//System.out.println("スレッド起動");
				String name=k[0];
				String nick=name;
				if(DiscordBOT.DefaultHost!=null)nick=DiscordBOT.DefaultHost.getNick(k[1],name);
				if(nick==null)nick=name;
				AlarmThread2 as=new AlarmThread2(l-now, name,nick);
				as.gid=k[1];
				as.cid=k[2];
				as.start();
			}
		}
	}
	private class Format {
		public String s;
		public Format(String t) {
			s=t;
		}
		private String a(BouyomiConection con,String t) {
			int hi=s.indexOf(t);
			if(hi>=0) {
				String h=s.substring(0,hi);
				s=s.substring(hi+1);
				if(h.length()<1) {
					h="00";
				}else if(h.length()<2) {
					h="0"+h;
				}else if(h.length()>2) {
					DiscordAPI.chatDefaultHost(con, t+" 指定文字数が不正です処理対象\n結果="+h);
					return null;
				}
				//DiscordAPI.chatDefaultHost(t+" 指定は正常に処理されました\n結果="+h);
				return h+t;
			}
			return "00"+t;
		}
	}
	@Override
	public void autoSave() throws IOException{
		if(savedmap)return;
		shutdownHook();
	}
	public void shutdownHook() {
		try{
			BouyomiProxy.save(map,"Alarm.txt");
			BouyomiProxy.save(messages,"AlarmMessages.txt");
			savedmap=true;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}