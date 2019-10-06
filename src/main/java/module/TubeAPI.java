package module;

import static bouyomi.BouyomiProxy.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.BouyomiConection;
import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.DiscordBOT.DiscordAPI;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;

/**動画再生機能*/
public class TubeAPI implements IModule,IAutoSave{

	public static boolean nowPlayVideo;
	public static String video_host=null;
	public static int VOL=30,DefaultVol=-1;
	public static String lastPlay,lastPlayUser,lastPlayUserId,lastPlayGuildId,lastPlayChannelId;
	public static int maxHistory=64;//32個履歴を保持する
	/**履歴が入ってるリスト*/
	public static ArrayList<String> playHistory=new ArrayList<String>();
	static String HistoryFile="play.txt";
	private static long lastPlayDate;
	private static ExecutorService pool=new ThreadPoolExecutor(0,10,60L,TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());
	protected static int stopTime=480000;
	public static long lastComment=System.currentTimeMillis();
	public static class PlayVideoEvent implements BouyomiEvent{
		public String videoID;
		public PlayVideoEvent(String videoID){
			this.videoID=videoID;
		}
	}
	public static class PlayVideoTitleEvent implements BouyomiEvent{
		public String title;
		public PlayVideoTitleEvent(String s){
			title=s;
		}
	}
	public TubeAPI() {
		try{
			loadHistory();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		if(BouyomiProxy.Config.containsKey("初期音量")) {
			try{
				DefaultVol=Integer.parseInt(BouyomiProxy.Config.get("初期音量"));
			}catch(NumberFormatException e) {

			}
		}
		if(BouyomiProxy.Config.containsKey("動画サーバのアドレス")) {
			try{
				video_host=BouyomiProxy.Config.get("動画サーバのアドレス");
			}catch(NumberFormatException e) {

			}
		}
		setAutoStop();
	}
	public static synchronized boolean playTube(final BouyomiConection bc,String videoID) {
		if(videoID.indexOf('<')>=0||videoID.indexOf('>')>=0||videoID.indexOf('?')>=0)return false;
		if(System.currentTimeMillis()-lastPlayDate<5000) {
			System.out.println("前回の再生から5秒以内には再生できませんID="+videoID);
			if(bc!=null)bc.addTask.add("前回の再生から5秒以内には再生できません");
			return false;
		}
		if(videoID.indexOf(' ')>=0||videoID.indexOf('　')>=0) {
			videoID=videoID.trim();
		}
		try{
			nowPlayVideo=true;
			if(DefaultVol>=0)VOL=DefaultVol;
			lastPlayDate=System.currentTimeMillis();
			//videoID=URLEncoder.encode(videoID,"utf-8");//これ使うと動かない
			URL url=new URL("http://"+video_host+"/operation.html?"+videoID+"&vol="+VOL);
			//System.out.println(url.toString());
			url.openStream().close();
			lastPlay=videoID;
			BouyomiProxy.module.event(new PlayVideoEvent(videoID));
			if(bc!=null&&bc.user!=null&&!bc.user.isEmpty())lastPlayUser=bc.user;
			else lastPlayUser=null;
			if(bc!=null&&bc.userid!=null&&!bc.userid.isEmpty())lastPlayUserId=bc.userid;
			else lastPlayUserId=null;
			if(bc instanceof BouyomiBOTConection) {
				BouyomiBOTConection bbc=(BouyomiBOTConection)bc;
				lastPlayGuildId=bbc.server==null?"0":bbc.server.getId();
				lastPlayChannelId=bbc.event.getTextChannel().getId();
			}
			if(playHistory.size()>=maxHistory){
				playHistory.remove(maxHistory-1);
			}
			playHistory.add(0,videoID);
			if(DiscordBOT.DefaultHost!=null)DiscordBOT.DefaultHost.log("動画再生="+IDtoURL(videoID)+"\n再生者="+lastPlayUser);
			try{
				FileOutputStream fos=new FileOutputStream(HistoryFile,true);//追加モードでファイルを開く
				try{
					String d=new SimpleDateFormat("yyyy/MM/dd HH時mm分ss秒").format(new Date());
					StringBuilder s=new StringBuilder(videoID);
					s.append("\t再生時刻").append(d);
					if(bc!=null&&bc.user!=null) {
						s.append("\t").append(bc.user);
						if(bc.userid!=null)s.append("\t").append(bc.userid);
						else s.append("\t-");
					}
					s.append("\n");
					fos.write(s.toString().getBytes(StandardCharsets.UTF_8));//改行文字を追加してバイナリ化
				}finally {
					fos.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			pool.execute(new Runnable() {
				@Override
				public void run(){
					checkError(bc);
					operation("play");
				}
			});
			pool.execute(new Runnable() {
				@Override
				public void run(){
					checkTitle(bc);
				}
			});
			return true;
		}catch(IOException e){
			if(bc!=null)bc.addTask.add("再生プログラムとの通信に問題が発生しました");
			//System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}
	public static void checkTitle(BouyomiConection bc) {
		try{
			Thread.sleep(1000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		for(int i=0;i<5;i++) {
			try{
				Thread.sleep(500);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			String s=getLine("GETtitle=0");
			if(s!=null&&!s.isEmpty()&&!s.equals(lastPlay)) {
				BouyomiProxy.module.event(new PlayVideoTitleEvent(s));
				StringBuilder sb=new StringBuilder();
				sb.append("L=").append(Long.toString(System.currentTimeMillis())).append("\t");
				sb.append("U=").append(lastPlayUserId).append("\t");
				sb.append("G=").append(lastPlayGuildId).append("\t");
				sb.append("C=").append(lastPlayChannelId).append("\t");
				String d=new SimpleDateFormat("yyyy/MM/dd HH時mm分ss秒").format(new Date());
				sb.append("F=").append(d).append("\t");
				sb.append("V=").append(lastPlay).append("\t");
				sb.append("N=").append(lastPlayUser.replaceAll("\t"," ")).append("\t");
				sb.append("T=").append(s.replaceAll("\t"," "));
				saveLog(sb);
				System.out.println("動画タイトル："+s);
				DiscordAPI.chatDefaultHost(bc,"/動画タイトル："+s);
				break;
			}
		}
	}
	private static void saveLog(StringBuilder sb) {
		try{
			FileOutputStream fos=new FileOutputStream("play_title.txt",true);//追加モードでファイルを開く
			try{
				sb.append("\n");
				fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));//改行文字を追加してバイナリ化
			}finally {
				fos.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	public static void checkError(BouyomiConection bc) {
		for(int i=0;i<5;i++) {
			try{
				Thread.sleep(500);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			String s=getLine("GETerror=0");
			if(s!=null)try {
				int ec=Integer.parseInt(s);
				if(ec>0&&ec!=2) {
					System.out.println("動画再生エラー="+ec+"動画ID="+lastPlay);
					String c=Integer.toString(ec);
					StringBuilder dis=new StringBuilder("再生エラー");
					dis.append(c);
					switch(ec) {
						case 2:
							dis.append("\n/*リクエストに無効なパラメータ値が含まれています(参考)");
							break;
						case 5:
							dis.append("\n/*Youtubeのiframeプレイヤーでエラーが発生しました(参考)");
							break;
						case 100:
							dis.append("\n/*動画が見つかりません。削除されているか非公開に設定されているかもしれません(参考)");
							break;
						case 101:
						case 150:
							dis.append("\n/*動画の所有者が、埋め込み動画プレーヤーでの再生を許可していません(参考)");
							dis.append("\nhttps://atuwa.github.io/TubePlay4e/localserver/test.html で再生可能か確認できます");
							break;
						case 2500:
							dis.append("\n/*動画情報取得がタイムアウトしました");
							dis.append("\nhttps://atuwa.github.io/TubePlay4e/localserver/test.html で再生可能か確認できます");
							break;
					}
					if(!DiscordAPI.chatDefaultHost(bc, dis.toString())) {
						BouyomiProxy.talk("localhost:"+BouyomiProxy.proxy_port,"再生エラー"+c);
					}
					break;
				}
			}catch(NumberFormatException e) {

			}
		}
	}
	public static synchronized int getVol(){
		try{
			String l=getLine("GETvolume");
			if(l==null)return -1;
			int vol=(int) Double.parseDouble(l);
			return vol;
		}catch(NumberFormatException e){
			e.printStackTrace();
		}
		return -1;
	}
	public static synchronized String getLine(String op) {
		if(!op.contains("="))op+="=0";
		BufferedReader br=null;
		try{
			URL url=new URL("http://"+video_host+"/operation.html?"+op);
			InputStreamReader isr=new InputStreamReader(url.openStream());
			br=new BufferedReader(isr);//1行ずつ取得する
			return br.readLine();
		}catch(IOException e){
			e.printStackTrace();
		}finally {
			try{
				if(br!=null)br.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	private static int getTimeParm(String url) {
		String tS=extract(url,"t");
		int t=0;
		if(tS!=null) {
			Matcher m=Pattern.compile("[0-9]++").matcher(tS);
			if(!m.find())return 0;
			try{
				t=Integer.parseInt(m.group());
			}catch(NumberFormatException nfe) {

			}
		}
		return t;
	}
	public static synchronized boolean play(BouyomiConection bc,String url) {
		if(url.indexOf("https://www.youtube.com/")==0||
				url.indexOf("https://m.youtube.com/")==0||
				url.indexOf("https://youtube.com/")==0||
				url.indexOf("http://www.youtube.com/")==0||
				url.indexOf("http://m.youtube.com/")==0||
				url.indexOf("http://youtube.com/")==0) {
			String vid=extract(url,"v");
			String lid=extract(url,"list");
			if(vid!=null) {
				vid+="&t="+getTimeParm(url);
				return playTube(bc, vid);
			}
			else if(lid!=null) {
				lid+="&t="+getTimeParm(url);
				String indexS=extract(url,"index");
				int index=-1;
				if(indexS!=null) {
					indexS=indexS.substring(6);
					try{
						index=Integer.parseInt(indexS)-1;
					}catch(NumberFormatException nfe) {

					}
				}
				if(index>=0)lid+="&index="+index;
				return playTube(bc, lid);
			}else{
				bc.addTask.add("URLを解析できませんでした");
				return false;
			}
		}else if(url.indexOf("https://youtu.be/")==0||url.indexOf("http://youtu.be/")==0) {
			int t=getTimeParm(url);
			int end=url.indexOf('?');
			String vid;
			if(end>=0)vid=url.substring(17,end);
			else vid=url.substring(17);
			vid+="&t="+t;
			return playTube(bc, "v="+vid);
		}else if(url.indexOf("v=")==0) {
			return playTube(bc, url);
		}else if(url.indexOf("list=")==0) {
			return playTube(bc, url);
		}else if(url.indexOf("nico=")==0) {
			return playTube(bc, url);
		}else if(url.indexOf("sc=")==0) {
			return playTube(bc, url);
		}else if(url.indexOf("https://soundcloud.com")==0){
			String id=soundcloud(url);
			if(id!=null)try{
				Integer.parseInt(id);
				return playTube(bc, "sc="+id);
			}catch(NumberFormatException nfe) {}
			bc.addTask.add("動画アイディーを抽出できませんでした");
			System.err.println("URL解析失敗="+url);
		}else{
			if(playNico(bc, url, "sm","so","nm"))return true;
			Matcher scm = Pattern.compile("//api.soundcloud.com/tracks/[0-9]++").matcher(url);
			if(scm.find()) {
				String s=scm.group();
				Matcher scm2 = Pattern.compile("[0-9]++").matcher(s);
				if(scm2.find()) {
					url=scm2.group();
					//System.out.println("SC ID="+url);
					return playTube(bc, "sc="+url);
				}
			}
			bc.addTask.add("動画アイディーを抽出できませんでした");
			System.err.println("URL解析失敗="+url);
		}
		return false;
	}
	public static boolean playNico(BouyomiConection bc,String url,String... sm) {
		for(String s:sm) {
			Matcher m = Pattern.compile(s+"[0-9]++").matcher(url);
			if(m.find())return playTube(bc, "nico="+m.group());
		}
		return false;
	}
	public static synchronized String statusAllJson() {
		StringBuilder sb=new StringBuilder(64);//
		sb.append("{\n");
		String last;
		if(lastPlay!=null)last="\""+lastPlay+"\"";
		else last=lastPlay;
		sb.append("\"lastPlay\":").append(last).append(",\n");
		sb.append("\"stopTime\":").append(stopTime).append(",\n");
		sb.append("\"DefaultVol\":").append(DefaultVol).append(",\n");
		sb.append("\"Vol\":").append(VOL).append(",\n");
		sb.append("\"lastPlayDate\":").append(lastPlayDate).append(",\n");
		String title=getTitle();
		if(title!=null)title="\""+title+"\"";
		sb.append("\"lastPlayTitle\":").append(title).append("\n");
		sb.append("}\n");
		return sb.toString();
	}
	public static String getTitle() {
		String s=getLine("GETtitle=0");
		if(s==null||s.isEmpty()||s.equals(lastPlay))return null;
		return s;
	}
	public static String extract(String url,String name) {
		if(url==null||url.isEmpty())return null;
		StringBuilder sb=new StringBuilder(name);
		sb.append("=");
		int start=url.indexOf(new StringBuilder("?").append(sb).toString());
		if(start<0)start=url.indexOf(new StringBuilder("&").append(sb).toString());
		if(start<0)return null;
		String ss=url.substring(start+1);
		int end=ss.indexOf("&");
		if(end<0)return ss;
		return ss.substring(0,end);
	}
	/**@param op 実行するコマンド
	 * @return 正常に実行された時trueが返る*/
	public static synchronized boolean operation(String op) {
		if(!op.contains("="))op+="=0";
		try{
			URL url=new URL("http://"+video_host+"/operation.html?"+op);
			url.openStream().close();
			return true;
		}catch(IOException e){
			e.printStackTrace();
		}
		return false;
	}
	public static String IDtoURL(String id) {
		if(id.indexOf("v=")==0){//動画
			return "https://www.youtube.com/watch?"+id;
		}else if(id.indexOf("list=")==0){//プレイリスト
			return "https://www.youtube.com/playlist?"+id;
		}else if(id.indexOf("nico=")==0){//ニコニコ
			return "https://www.nicovideo.jp/watch/"+id.substring(5);
		}
		return null;//それ以外
	}
	/**ファイルから再生履歴を読み込む*/
	public static void loadHistory() throws IOException {
		FileInputStream fis=new FileInputStream(new File(HistoryFile));
		InputStreamReader isr=new InputStreamReader(fis,StandardCharsets.UTF_8);
		BufferedReader br=new BufferedReader(isr);
		try {
			while(br.ready()) {
				String line=br.readLine();
				if(line==null)break;
				int index=line.indexOf('\t');
				if(index>=0)lastPlay=line.substring(0,index);
				else lastPlay=line;
				if(playHistory.size()>=maxHistory){
					playHistory.remove(maxHistory-1);
				}
				playHistory.add(0,lastPlay);
			}
		}finally{
			br.close();
		}
	}
	private void setAutoStop(){
		if(video_host==null)return;
		new Thread("AutoVideoStop") {
			public synchronized void run() {
				while(true) {
					try{
						//wait(60000);
						Thread.sleep(60000);
						if(nowPlayVideo&&System.currentTimeMillis()-lastComment>stopTime) {
							if("NOT PLAYING".equals(getLine("status")))nowPlayVideo=false;
							else{
								System.out.println("動画自動停止");
								BouyomiProxy.talk("localhost:"+BouyomiProxy.proxy_port,"/動画停止()");
							}
						}
					}catch(InterruptedException e){
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	@Override
	public void call(Tag TAG){
		if(video_host==null)return;//再生サーバが設定されていない時
		if(!TAG.con.speak)return;
		lastComment=System.currentTimeMillis();
		BouyomiConection con=TAG.con;
		ArrayList<String> addTask=TAG.con.addTask;
		String tag=TAG.getTag("自動停止時間");
		if(tag!=null) {
			if(TAG.isAdmin()){
				if(tag.isEmpty())TubeAPI.stopTime=480000;
				else try {
					TubeAPI.stopTime=Integer.parseInt(tag);
				}catch(NumberFormatException nfe) {
					StringWriter sw=new StringWriter();
					nfe.printStackTrace(new PrintWriter(sw));
					TAG.chatDefaultHost(sw.toString());
				}
				TAG.chatDefaultHost("自動停止時間を"+TubeAPI.stopTime+"msにしました");
			}else TAG.chatDefaultHost("権限がありません");
		}
		tag=TAG.getTag("動画再生");
		if(tag!=null) {//動画再生
			//System.out.println(text);//ログに残す
			//DiscordAPI.chatDefaultHost("パラメータ="+tag);
			if(tag.isEmpty()) {
				if(operation("play")){
					String em="つづきを再生します。";
					int vol=getVol();
					if(vol>=0)em+="音量は"+vol+"です";
					addTask.add(em);
				}
			}else{
				System.out.println("動画再生（"+tag+")");//ログに残す
				if(play(con, tag)) {
					String em="動画を再生します。";
					int vol=DefaultVol<0?VOL:DefaultVol;
					if(vol>=0)em+="音量は"+vol+"です";
					addTask.add(em);
				}else addTask.add("動画を再生できませんでした");
			}
			if(con.text.isEmpty())return;//1文字も残ってない時は終わり
		}
		tag=TAG.getTag("動画タイトル");
		if(tag!=null) {
			String s=getTitle();
			if(con.mute)System.out.println(s);
			else if(s==null)TAG.chatDefaultHost("/動画タイトルが取得できませんでした");
			else TAG.chatDefaultHost("/動画タイトル："+s);
		}
		tag=TAG.getTag("動画URL");
		if(tag==null)tag=TAG.getTag("動画ＵＲＬ");//全角英文字
		if(tag!=null) {
			if(lastPlay==null)addTask.add("再生されていません");//再生中の動画情報がない時
			else if(tag.isEmpty()) {//0文字
				String url=IDtoURL(lastPlay);
				if(url==null)addTask.add("非対応形式です");
				else{
					if(lastPlayUser!=null)url="再生者："+lastPlayUser+"\n"+url;
					if(con.mute)System.out.println(url);
					else TAG.chatDefaultHost(url);
				}
			}else{
				try {
					int dc=Integer.parseInt(tag);//取得要求数
					dc=Integer.min(dc,playHistory.size());//データ量と要求数の少ない方に
					if(dc>0) {
						StringBuilder sb=new StringBuilder();
						sb.append(dc).append("件取得します/*\n");
						for(int i=0;i<dc;i++) {
							String s=playHistory.get(i);
							String url=IDtoURL(s);
							if(url==null)url=s;
							sb.append(url).append("\n");
						}
						if(con.mute)System.out.println(sb.toString());
						else TAG.chatDefaultHost(sb.toString());
					}
				}catch(NumberFormatException e) {

				}
			}
			if(con.text.isEmpty())return;//1文字も残ってない時は終わり
		}
		tag=TAG.getTag("動画ID","動画ＩＤ");//全角英文字
		if(tag!=null) {
			if(lastPlay==null)addTask.add("再生されていません");//再生中の動画情報がない時
			else if(tag.isEmpty()) {//0文字
				String url=lastPlay;
				if(lastPlayUser!=null)url="再生者："+lastPlayUser+"\n"+url;
				if(con.mute) {
					System.out.println(url);
				}else TAG.chatDefaultHost("/"+url);
			}else{
				try {
					int dc=Integer.parseInt(tag);//取得要求数
					dc=Integer.min(dc,playHistory.size());//データ量と要求数の少ない方に
					if(dc>0) {
						StringBuilder sb=new StringBuilder();
						sb.append(dc).append("件取得します/*\n");
						for(int i=0;i<dc;i++) {
							String s=playHistory.get(i);
							sb.append(s).append("\n");
						}
						if(con.mute)System.out.println(sb.toString());
						else TAG.chatDefaultHost(sb.toString());
					}
				}catch(NumberFormatException e) {

				}
			}
		}
		tag=TAG.getTag("動画停止");
		if(tag!=null){//動画停止
			if("動画停止".equals(con.text))con.text="";
			System.out.println("動画停止");//ログに残す
			if(operation("stop")){
				TubeAPI.nowPlayVideo=false;
				addTask.add("動画を停止します");
			}else addTask.add("動画を停止できませんでした");
			return;
		}
		tag=TAG.getTag("動画音量","動画音声","音量調整","音量設定");
		if(tag!=null){//動画音量
			if(tag.isEmpty()) {
				String em;
				int vol=getVol();//音量取得。取得失敗した時-1
				if(vol<0)em="音量を取得できません";
				else em="音量は"+vol+"です";
				System.out.println(em);
				if(con.mute) {
					//DiscordAPI.chatDefaultHost("/"+em);
				}else if(!TAG.chatDefaultHost(em))addTask.add(em);
			}else{
				try{
					int Nvol=-10;
					switch(tag.charAt(0)){
						case '＋':
						case '－':
						case '+':
						case '-':
							tag=tag.replace('＋','+');
							tag=tag.replace('－','-');
							Nvol=getVol();//+記号で始まる時今の音量を取得
					}
					int vol=Integer.parseInt(tag);//要求された音量
					if(Nvol==-1) {
						addTask.add("音量を変更できませんでした。音量を取得できませんでした");//失敗した時これを読む
					}else {
						if(Nvol>=0)vol=Nvol+vol;//音量が取得させていたらそれに指定された音量を足す
						if(vol>100)vol=100;//音量が100以上の時100にする
						else if(vol<0)vol=0;//音量が0以下の時0にする
						System.out.println("動画音量"+vol);//ログに残す
						VOL=vol;//再生時に使う音量をこれにする
						if(operation("vol="+vol))addTask.add("音量を"+vol+"にします");//動画再生プログラムにコマンド送信
						else addTask.add("音量を変更できませんでした。通信に失敗しました");//失敗した時これを読む
					}
				}catch(NumberFormatException e) {
					addTask.add("音量を変更できませんでした。数値を解析できません");//失敗した時これを読む
				}
			}
			if(con.text.isEmpty())return;
		}
		tag=TAG.getTag("初期音量");
		if(tag!=null) {
			if(tag.isEmpty()) {
				String em;
				if(DefaultVol<0)em="デフォルトの音量は前回の動画の音量です";
				else em="デフォルトの音量は"+DefaultVol+"です";
				System.out.println(em);
				if(con.mute) {
					//DiscordAPI.chatDefaultHost("/"+em);
				}else if(!TAG.chatDefaultHost(em))addTask.add(em);
			}else {
				try{
					int vol=Integer.parseInt(tag);//要求された音量
					if(vol<0) {
						System.out.println("初期音量 前回の動画音量");//ログに残す
						addTask.add("前に再生した時の音量を使うように設定します");//取得失敗した時これを読む
						DefaultVol=-1;//再生時に使う音量をこれにする
						Config.put("初期音量",String.valueOf(DefaultVol));
					}else {
						if(vol>100)vol=100;//音量が100以上の時100にする
						System.out.println("初期音量"+vol);//ログに残す
						DefaultVol=vol;//再生時に使う音量をこれにする
						addTask.add("次に再生する時は"+DefaultVol+"で再生します");//成功した時これを読む
						Config.put("初期音量",String.valueOf(DefaultVol));
					}
				}catch(NumberFormatException e) {

				}
			}
			if(con.text.isEmpty())return;
		}
		tag=TAG.getTag("VideoStatus","動画情報");
		if(tag!=null) {
			//con.text="";
			TAG.chatDefaultHost(statusAllJson());
		}
		tag=TAG.getTag("動画位置","動画シーク");
		if(tag!=null) {
			try {
				String s=Integer.toString(Integer.parseInt(tag));
				if(operation("seek="+s)) {
					TAG.chatDefaultHost("/動画を"+s+"秒にシークしました");
				}else TAG.chatDefaultHost("/動画シーク失敗");
			}catch(Exception e) {
				TAG.con.addTask.add("パラメータが間違ってます");
			}
		}
	}
	public static String soundcloud(String org) {
		try {
			String UA="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";
			URL url=new URL(org);
			HttpURLConnection uc=(HttpURLConnection)url.openConnection();
			uc.setInstanceFollowRedirects(false);
			uc.setRequestProperty("User-Agent",UA);
			InputStream is=uc.getInputStream();//POSTした結果を取得
			if(uc.getResponseCode()!=200)return null;
			//System.out.println("200");
			ByteArrayOutputStream bos=new ByteArrayOutputStream();
			byte[] b=new byte[1024];
			while(true) {
				int len=is.read(b);
				if(len<1)break;
				bos.write(b,0,len);
			}
			String site=bos.toString("utf-8");
			//System.out.println(site);
			String ss="<a href=\"https://api.soundcloud.com/tracks/";
			int index=site.indexOf(ss);
			String ss1=site.substring(index+ss.length());
			//System.out.println(ss1);
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<ss1.length();i++) {
				char ch=ss1.charAt(i);
				if(ch=='/')break;
				sb.append(ch);
			}
			return sb.toString();
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public void autoSave() throws IOException{

	}
	@Override
	public void shutdownHook() {
		operation("stop");
	}
}