package module;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import bouyomi.IModule;
import bouyomi.Tag;

public class MoviePlayHistory implements IModule{
	@Override
	public void call(Tag tag){
		String seed=tag.getTag("ランダム動画");
		if(seed!=null) {
			Random rnd;
			if(seed.isEmpty())rnd=new SecureRandom();
			else rnd=new Random(seed.hashCode());
			int index=rnd.nextInt(TubeAPI.playHistory.size());
			String s=TubeAPI.playHistory.get(index);
			tag.chatDefaultHost("/"+TubeAPI.playHistory.size()+"件の履歴からランダムに選択された動画："+TubeAPI.IDtoURL(s));
		}
		String word=tag.getTag("履歴動画検索");
		if(word!=null) {
			try{
				tag.chatDefaultHost(searchHistory(null,word));
			}catch(IOException e){
				e.printStackTrace();
				tag.chatDefaultHost("/ファイル操作例外");
			}
		}
	}
	public String searchHistory(String user,String word) throws IOException {
		if(word.isEmpty())return "検索ワードを指定してください";
		FileInputStream fis=new FileInputStream(new File("play_title.txt"));
		InputStreamReader isr=new InputStreamReader(fis,StandardCharsets.UTF_8);
		BufferedReader br=new BufferedReader(isr);
		StringBuilder sb=new StringBuilder();
		try {
			long uid=user==null?0:Long.parseLong(user);
			ArrayList<String> titles=new ArrayList<String>();
			ParseValue pv=new ParseValue();
			while(br.ready()) {
				String line=br.readLine();
				if(line==null)break;
				parse(pv,line.split("\t"));
				if(uid==0||uid==pv.user) {
					if(pv.title.indexOf(word)>=0) {
						if(!titles.contains(pv.title)) {
							sb.append(TubeAPI.IDtoURL(pv.videoID)).append(" ").append(pv.title).append("\n");
							titles.add(pv.title);
						}
					}
				}
				if(sb.length()>1000)return "ヒット件数が多すぎます("+titles.size()+"件)";
			}
		}finally{
			br.close();
		}
		return sb.length()==0?"/見つかりませんでした":"/"+sb.toString();
	}
	@SuppressWarnings("unused")
	private class ParseValue{
		/**Lパラメータ<br>System.currentTimeMillis()で取得された再生時刻*/
		private long time;
		/**Fパラメータ<br>成形済の再生時刻*/
		private String time_format;
		/**Vパラメータ<br>動画ID*/
		private String videoID;
		/**Nパラメータ<br>再生時の再生者ニックネーム*/
		private String nick;
		/**Uパラメータ<br>再生者のユーザID*/
		private long user;
		/**Gパラメータ<br>再生した場所(サーバ/ギルド)*/
		private long guild;
		/**Cパラメータ<br>再生したテキストチャンネル*/
		private long channel;
		/**Tパラメータ<br>動画のタイトル*/
		private String title;
	}
	private void parse(ParseValue pv,String[] line) {
		for(String s:line) {
			if(s.indexOf('L')==0) {
				try{
					pv.time=Long.parseLong(s.substring(2));
				}catch(NumberFormatException nfe) {
					pv.time=-1;
				}
			}else if(s.indexOf('F')==0) {
				pv.time_format=s.substring(2);
			}else if(s.indexOf('V')==0) {
				pv.videoID=s.substring(2);
			}else if(s.indexOf('N')==0) {
				pv.nick=s.substring(2);
			}else if(s.indexOf('U')==0) {
				try{
					pv.user=Long.parseLong(s.substring(2));
				}catch(NumberFormatException nfe) {}
			}else if(s.indexOf('G')==0) {
				try{
					pv.guild=Long.parseLong(s.substring(2));
				}catch(NumberFormatException nfe) {}
			}else if(s.indexOf('C')==0) {
				try{
					pv.channel=Long.parseLong(s.substring(2));
				}catch(NumberFormatException nfe) {}
			}else if(s.indexOf('T')==0) {
				pv.title=s.substring(2);
			}
		}
	}
}