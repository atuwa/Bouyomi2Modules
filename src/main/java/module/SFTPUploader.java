package module;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.DiscordBOT.DiscordAPI;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.Message.Attachment;

/**
 * @author tool-taro.com
 * @see https://qiita.com/tool-taro/items/a3f016a58e3e32675858
 */
public class SFTPUploader implements IModule,Runnable,IAutoSave{
	public static final long keepConection=1*60*1000L;
	public ArrayList<FileData> queue=new ArrayList<FileData>();
	private boolean conection;
	//サーバ設定
	private String host,user,password,dir;
	private ArrayList<String> whiteList=new ArrayList<String>();
	private boolean whiteList_seved=false;
	private boolean log=false;
	public SFTPUploader(){
		host=BouyomiProxy.Config.get("SFTPサーバ");
		user=BouyomiProxy.Config.get("SFTPユーザ");
		password=BouyomiProxy.Config.get("SFTPパスワード");
		dir=BouyomiProxy.Config.get("SFTPディレクトリ");
		try{
			BouyomiProxy.load(whiteList,"SFTP転送許可ユーザリスト.txt");
			whiteList_seved=true;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public void main()
			throws JSchException,SftpException,FileNotFoundException,IOException{
		if(log)System.out.println("SFTPサーバに接続します...");
		JSch jsch;
		Session session=null;
		ChannelSftp channel=null;
		BufferedInputStream bin=null;

		try{
			//接続
			jsch=new JSch();
			int tab=host.indexOf(':');
			if(tab<0||tab+1>host.length()) {
				session=jsch.getSession(user,host,22);
			}else {
				String key=host.substring(0,tab);
				String val=host.substring(tab+1);
				session=jsch.getSession(user,key,Integer.parseInt(val));
			}
			//known_hostsのチェックをスキップ
			session.setConfig("StrictHostKeyChecking","no");
			session.setPassword(password);
			session.connect();

			channel=(ChannelSftp) session.openChannel("sftp");
			channel.connect();
			if(log)System.out.println("SFTPサーバに接続成功");
			//ディレクトリ移動
			if(log)System.out.println("ターゲットディレクトリに移動します...");
			channel.cd(dir);
			if(log)System.out.println("移動成功\n転送ループに突入しました");
			long t=0;
			while(true) {
				if(!queue.isEmpty())synchronized(queue){
					if(log)System.out.println("転送キューは"+queue.size()+"件です\n0件目を取り出します...");
					FileData fd=queue.get(0);
					if(log)System.out.println("0件目をキューから除去します...");
					queue.remove(0);
					try {
						if(log)System.out.println("ファイル名重複を検証します...");
						int index=fd.name.lastIndexOf('.');
						String suffix=".png";
						String nb;
						if(index>0&&index<fd.name.length()) {
							suffix=fd.name.substring(index);
							nb=fd.name.substring(0,index);
						}else nb=fd.name;
						for(int i=1;isExist(channel,fd.name);i++){
							fd.name=nb+"_"+i+suffix;
						}
						if(log)System.out.println("ファイル名"+fd.name+"で転送を開始します...");
						//アップロード
						channel.put(fd.is,fd.name);
					}finally {
						fd.Final();
					}
					if(log)System.out.println("転送を完了しました");
					t=0;
				}
				try{
					Thread.sleep(500);
					t+=500;
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				if(t>keepConection)break;
			}
			/*
			//ダウンロード
			bin = new BufferedInputStream(channel.get(fileName));
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte[] buf = new byte[1024];
			int length;
			while (true) {
			    length = bin.read(buf);
			    if (length == -1) {
			        break;
			    }
			    bout.write(buf, 0, length);
			}
			//標準出力
			System.out.format("ダウンロードしたファイル=%1$s", new String(bout.toByteArray(), StandardCharsets.UTF_8));
			*/
		}finally{
			if(log)System.out.println("接続を切断します...");
			try {
				if(bin!=null)try{
					bin.close();
				}catch(IOException e){}
				if(channel!=null)try{
					channel.disconnect();
				}catch(Exception e){}
				if(session!=null)try{
					session.disconnect();
				}catch(Exception e){}
				if(log)System.out.println("正常に切断しました");
			}finally {
				conection=false;
			}
		}
	}
	private boolean isExist(ChannelSftp channel,String targetFilePath) {
		if(log)System.out.println("ファイル名"+targetFilePath+"を検証します...");
		try {
			channel.lstat(targetFilePath);
		} catch (SftpException e) {
			if(log)System.out.println("ファイル名"+targetFilePath+"は存在しません");
			return false;
		}
		if(log)System.out.println("ファイル名"+targetFilePath+"は存在します");
		return true;
    }
	public static class FileData{
		public long size;
		public InputStream is;
		public String name;
		public FileData(long s,InputStream i,String n) {
			size=s;
			is=i;
			name=n;
		}
		public void Final(){}
	}
	private FileData getData(Tag tag, BouyomiBOTConection bc,Attachment a){
		ByteArrayOutputStream os=new ByteArrayOutputStream(a.getSize());
		bc.bot.downloadOutputStream(a.getUrl(),os);
		InputStream is =new ByteArrayInputStream(os.toByteArray());
		os.reset();
		try{
			BufferedImage image=ImageIO.read(is);
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			int index=a.getFileName().lastIndexOf('.');
			String suffix="png";
			if(index>0&&index+1<a.getFileName().length())suffix=a.getFileName().substring(index+1);
			if(!ImageIO.write(image,suffix,os))throw new IOException();
			try{
				Thread.sleep(1000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}catch(Exception e){
			tag.chatDefaultHost("/画像検証失敗(非対応データです)");
			try{
				is.close();
			}catch(IOException e1){}
			return null;
		}
		is =new ByteArrayInputStream(os.toByteArray());
		return new FileData(a.getSize(),is,bc.userid+"_"+a.getFileName());
	}
	private boolean check(String... list) {
		for(String s:list) {
			if(s==null||s.isEmpty())return true;
		}
		return false;
	}
	private boolean isAdmin(String userid) {
		return userid.equals("543670181734645781")||//妖花ちゃん
				userid.equals("544529530866368522")||//黄泉
				userid.equals("440102258143920128");//天狗さん
	}
	@Override
	public void call(Tag tag){
		String s=tag.getTag("SFTPログ設定");
		if(s!=null&&isAdmin(tag.con.userid)) {
			log=!log;
			tag.chatDefaultHost("/ログを出力"+(log?"する":"しない")+"ようにしました");
		}
		s=tag.getTag("SFTP許可");
		if(s!=null&&isAdmin(tag.con.userid)) {
			if(s.isEmpty()){
				tag.chatDefaultHost("ユーザIDを指定してください");
			}else try{
				Long.parseLong(s);
				if(whiteList.contains(s)) {
					tag.chatDefaultHost(tag.getUserName(s)+"さんは登録済です");
				}else {
					whiteList.add(s);
					whiteList_seved=false;
					tag.chatDefaultHost(tag.getUserName(s)+"さんを登録しました");
				}
			}catch(NumberFormatException e) {
				tag.chatDefaultHost("ユーザIDを指定してください");
			}
		}
		s=tag.getTag("SFTP拒否");
		if(s!=null&&(isAdmin(tag.con.userid)||whiteList.contains(s))) {
			if(s.isEmpty()){
				if(!isAdmin(tag.con.userid)) {
					whiteList.remove(s);
					whiteList_seved=false;
					tag.chatDefaultHost(tag.getUserName(s)+"さんを許可リストから削除しました");
				}else tag.chatDefaultHost("ユーザIDを指定してください");
			}else if(isAdmin(tag.con.userid))try{
				Long.parseLong(s);
				if(whiteList.contains(s)) {
					whiteList.remove(s);
					whiteList_seved=false;
					tag.chatDefaultHost(tag.getUserName(s)+"さんを許可リストから削除しました");
				}else {
					tag.chatDefaultHost(tag.getUserName(s)+"さんは登録されていません");
				}
			}catch(NumberFormatException e) {
				tag.chatDefaultHost("ユーザIDを指定してください");
			}
		}
		s=tag.getTag("アップロード許可されてる？","アップロード許可されてる?");
		if(s!=null) {
			if(s.isEmpty()){
				tag.chatDefaultHost("あなたは許可されて"+(whiteList.contains(s)?"います":"いません"));
			}else try{
					Long.parseLong(s);
					String nick=tag.getUserName(s);
					tag.chatDefaultHost(nick+"さんは許可されて"+(whiteList.contains(s)?"います":"いません"));
			}catch(NumberFormatException e) {
				tag.chatDefaultHost("あなたは許可されて"+(whiteList.contains(s)?"います":"いません"));
			}
		}
		if(check(host,user,password,dir))return;
		if(tag.con.text.indexOf("SFTPアップロード")!=0)return;
		if(tag.con instanceof BouyomiBOTConection);
		else return;
		BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
		if(isAdmin(tag.con.userid)||whiteList.contains(tag.con.userid)){
			tag.con.text="";
			for(int i=0;i<bc.list.length;i++){
				if(log)System.out.println("DiscordにSFTP転送するファイルが投稿されました"+bc.list.length);
				FileData fd=getData(tag,bc,bc.list[i]);
				if(fd!=null)synchronized(queue) {
					System.out.println(fd.name+"をアップロード");
					DiscordAPI.chatDefaultHost(tag,"/"+fd.name+"をアップロード");
					queue.add(fd);
				}
			}
			synchronized(this) {
				if(!conection) {
					conection=true;
					new Thread(this).start();
				}
			}
		}else {
			tag.chatDefaultHost("あなたは許可されていません");
		}
	}
	@Override
	public void run(){
		try{
			main();
		}catch(JSchException|SftpException|IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void shutdownHook() {
		try{
			autoSave();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	@Override
	public void autoSave() throws IOException{
		if(whiteList_seved)return;
		BouyomiProxy.save(whiteList,"SFTP転送許可ユーザリスト.txt");
		whiteList_seved=true;
	}
}