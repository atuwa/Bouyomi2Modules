package module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.DiscordBOT.DiscordAPI;
import bouyomi.DiscordBOT.NamedFileObject;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;
import module.TubeAPI.PlayVideoEvent;
import module.TubeAPI.PlayVideoTitleEvent;

public class Sample implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.mentions.contains("581268794794573870")) {//メンションリストに539105406107254804が含まれる場合
			if(tag.con.text.contains("サンプルモジュール")) {//「サンプルモジュール」と言うメッセージを含む場合
				String m=Util.IDtoMention(tag.con.userid);//この書き込みをしたユーザIDからメンションを生成
				tag.chatDefaultHost(m+"サンプルモジュール");//メンションとテキストを連結して投稿
			}
		}
		String s=tag.getTag("サンプルモジュール");//タグ取得
		if(s!=null) {//タグが無い時はnull
			String m=Util.IDtoMention(tag.con.userid);//この書き込みをしたユーザIDからメンションを生成
			tag.chatDefaultHost(m+s.length());//メンションとタグの内容を連結して投稿
		}
		if(tag.con.mentions.contains("581268794794573870")) {
			if(tag.con.text.equals("働け")||tag.con.text.equals("仕事しろ")) {
				tag.chatDefaultHost("やだ");
			}
		}
		String seedS=tag.getTag("ランダム文字列");
		if(seedS!=null) {
			String[] parm=seedS.isEmpty()?null:seedS.split(",");
			long seedN=0;
			if(parm!=null&&parm.length>1)try{
				seedN=Long.parseLong(parm[1]);
			}catch(NumberFormatException nfe) {
				char[] ca=parm[1].toCharArray();
				for(char c:ca)seedN+=c;
			}
			int len=10;
			if(parm!=null&&parm.length>0)try{
				len=Integer.parseInt(parm[0]);
			}catch(NumberFormatException nfe) {

			}
			Random r;
			if(seedN==0)r=new Random();
			else r=new Random(seedN);
			StringBuilder sb=new StringBuilder("/");
			for(int i=0;i<len;i++) {
				sb.append((char)r.nextInt(65514));
			}
			tag.chatDefaultHost(sb.toString());
		}
		String org=tag.getTag("文字化け");
		if(org!=null) {
			try{
				byte[] b=org.getBytes(StandardCharsets.UTF_8);
				StringBuilder sb=new StringBuilder("/Shift-JIS\n```");
				String result=new String(b,"shift-jis");
				sb.append(result);
				sb.append("```\nEUC-JP\n```");
				result=new String(b,"euc-jp");
				sb.append(result);
				sb.append("```");
				tag.chatDefaultHost(sb.toString());
			}catch(UnsupportedEncodingException e){
				e.printStackTrace();
			}
		}
		org=tag.getTag("16進数E");
		if(org!=null) {
			byte[] ba=org.getBytes(StandardCharsets.UTF_8);
			StringBuilder sb=new StringBuilder("/");
			for(byte b:ba) {
				int i=b&0x000000FF;
				String hex=Integer.toHexString(i);
				if(hex.length()<2)sb.append("0");
				sb.append(hex);
			}
			tag.chatDefaultHost(sb.toString());
		}
		org=tag.getTag("16進数D");
		if(org!=null) {
			StringReader r=new StringReader(org);
			byte[] ba=new byte[org.length()/2];
			char[] cbuf=new char[2];
			for(int i=0;i<ba.length;i++) {
				try{
					int rl=r.read(cbuf);
					if(rl<0)break;
					ba[i]=(byte) (Integer.parseInt(String.valueOf(cbuf),16)&0xFF);
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			String d=new String(ba,StandardCharsets.UTF_8);
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"\n"+d);
		}
		org=tag.getTag("サーバーアイコン取得","サーバアイコン取得");
		if(org!=null&&tag.con instanceof BouyomiBOTConection) {
			BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
			icon(bc,bc.server.getIconUrl());
		}
		org=tag.getTag("サーバーアイコンURL","サーバーアイコンurl",
				"サーバアイコンURL","サーバアイコンurl");
		if(org!=null&&tag.con instanceof BouyomiBOTConection) {
			BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
			DiscordBOT.DefaultHost.send(bc,bc.server.getIconUrl());
		}
		org=tag.getTag("ユーザーアイコン取得","ユーザアイコン取得");
		if(org!=null&&tag.con instanceof BouyomiBOTConection) {
			BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
			if(org.isEmpty())org=bc.userid;
			String url=bc.server.getMemberById(org).getUser().getEffectiveAvatarUrl();
			icon(bc,url);
		}
		org=tag.getTag("ユーザーアイコンURL","ユーザーアイコンurl",
				"ユーザアイコンURL","ユーザアイコンurl");
		if(org!=null&&tag.con instanceof BouyomiBOTConection) {
			BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
			if(org.isEmpty())org=bc.userid;
			String url=bc.server.getMemberById(org).getUser().getEffectiveAvatarUrl();
			DiscordBOT.DefaultHost.send(bc,url);
		}
		org=tag.getTag("メッセージ削除");
		if(org!=null&&tag.con instanceof BouyomiBOTConection&&tag.isAdmin()) {
			BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
			DiscordBOT.DefaultHost.getTextChannel(bc.textChannel.getId()).deleteMessageById(org).queue();
		}
	}
	private void icon(BouyomiBOTConection bc,String url) {
		ByteArrayOutputStream os=new ByteArrayOutputStream();
		DiscordBOT.DefaultHost.downloadOutputStream(url,os);
		int index=url.lastIndexOf('.');
		String name="icon";
		if(index>0&&index<url.length())name+=url.substring(index);
		ByteArrayInputStream bais=new ByteArrayInputStream(os.toByteArray());
		NamedFileObject no=new NamedFileObject(bais,name);
		DiscordBOT.DefaultHost.send(" ",bc.server,bc.textChannel,no);
	}
	@Override
	public void event(BouyomiEvent o) {
		if(o instanceof PlayVideoEvent) {
			PlayVideoEvent e=(PlayVideoEvent)o;
			//System.out.println("動画再生を検出"+e.videoID);
			if(e.videoID.equals("nico=sm14223749")) {
				//DiscordAPI.chatDefaultHost("動画停止()/*この動画は再生禁止です");
				return;
			}
		}
		if(o instanceof PlayVideoTitleEvent) {
			PlayVideoTitleEvent e=(PlayVideoTitleEvent)o;
			if("nico=sm14223749".equals(TubeAPI.lastPlay)){
				new wait("動画停止()/*この動画は再生禁止です").start();
				return;
			}
			//System.out.println("動画タイトルを取得："+e.title);
			if(e.title.contains("オカリン"))new wait("動画停止()/*タイトルに再生禁止ワードが含まれています").start();
		}
	}
	private class wait extends Thread{
		private String st;
		public wait(String s) {
			st=s;
		}
		@Override
		public void run() {
			try{
				Thread.sleep(1500);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			DiscordAPI.chatDefaultHost(TubeAPI.lastPlayGuildId,TubeAPI.lastPlayChannelId,st);
		}
	}
}