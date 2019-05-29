package module;

import java.io.IOException;
import java.util.HashMap;

import bouyomi.BouyomiProxy;
import bouyomi.Counter;
import bouyomi.DiscordAPI;
import bouyomi.DiscordBOT;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;

public class Memo implements IModule,IAutoSave{

	private HashMap<String,String> data=new HashMap<String,String>();
	private boolean saved=true;
	{
		try{
			BouyomiProxy.load(data,"Memo.txt");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void call(Tag tag){
		String u=tag.con.userid;
		if(u==null)return;
		if(tag.isAdmin()) {
			String del=tag.getTag("メモ抹消");
			if(del!=null) {
				data.put(del,"メモは管理者により抹消されました");
				saved=false;
				DiscordAPI.chatDefaultHost(tag,Counter.getUserName(del)+"のメモを抹消しました");
			}
		}
		String get=tag.getTag("メモ取得");
		if(get!=null)getMemo(tag,get);
		String t=tag.getTag("memo");
		if(t==null)return;
		if(t.equals("消去")) {
			data.remove(u);
			saved=false;
			tag.con.addTask.add("メモを消去しました");
			DiscordBOT.DefaultHost.log(tag.con.user+"がメモを消去");
		}else if(t.length()>100) {
			DiscordAPI.chatDefaultHost(tag,"100文字制限を超えています");
		}else if(t.isEmpty()) {
			getMemo(tag, u);
		}else {
			data.put(u,t);
			saved=false;
			tag.con.addTask.add("メモしました");
			DiscordBOT.DefaultHost.log(tag.con.user+"が"+t+"をメモ");
		}
	}
	private void getMemo(Tag tag,String u){
		String name=Counter.getUserName(u);
		if(name==null)return;
		StringBuilder sb=new StringBuilder(name);
		sb.append("(").append(u).append(")");
		sb.append(" のメモ：\n/*");
		sb.append(data.getOrDefault(u,"null"));
		DiscordAPI.chatDefaultHost(tag,sb.toString());
	}
	@Override
	public void autoSave(){
		if(!saved)try{
			BouyomiProxy.save(data,"Memo.txt");
			saved=true;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void shutdownHook() {
		try{
			BouyomiProxy.save(data,"Memo.txt");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}