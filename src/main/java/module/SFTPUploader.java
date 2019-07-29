package module;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.Message.Attachment;

/**
 * @author tool-taro.com
 * @see https://qiita.com/tool-taro/items/a3f016a58e3e32675858
 */
public class SFTPUploader implements IModule,Runnable{
	public static final long memorySize=1*1024L*1024L*1024L;
	public static final long keepConection=1*60*1000L;
	public ArrayList<FileData> queue=new ArrayList<FileData>();
	private boolean conection;
	//サーバ設定
	private String host,user,password,dir;
	public SFTPUploader(){
		host=BouyomiProxy.Config.get("SFTPサーバ");
		user=BouyomiProxy.Config.get("SFTPユーザ");
		password=BouyomiProxy.Config.get("SFTPパスワード");
		dir=BouyomiProxy.Config.get("SFTPディレクトリ");
	}
	public void main()
			throws JSchException,SftpException,FileNotFoundException,IOException{
		System.out.println("SFTPサーバに接続します...");
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
			System.out.println("SFTPサーバに接続成功");
			//ディレクトリ移動
			System.out.println("ターゲットディレクトリに移動します...");
			channel.cd(dir);
			System.out.println("移動成功\n転送ループに突入しました");
			long t=0;
			while(true) {
				if(!queue.isEmpty())synchronized(queue){
					System.out.println("転送キューは"+queue.size()+"件です\n0件目を取り出します...");
					FileData fd=queue.get(0);
					System.out.println("0件目をキューから除去します...");
					queue.remove(0);
					try {
						System.out.println("ファイル名重複を検証します...");
						final String nb=fd.name;
						if(isExist(channel,fd.name)) {
							for(int i=1;isExist(channel,fd.name);i++){
								fd.name=nb+i;
							}
						}
						System.out.println("ファイル名"+fd.name+"で転送を開始します...");
						//アップロード
						channel.put(fd.is,fd.name);
					}finally {
						fd.Final();
					}
					System.out.println("転送を完了しました");
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
			System.out.println("接続を切断します...");
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
				System.out.println("正常に切断しました");
			}finally {
				conection=false;
			}
		}
	}
	private boolean isExist(ChannelSftp channel,String targetFilePath) {
		System.out.println("ファイル名"+targetFilePath+"を検証します...");
		try {
			channel.lstat(targetFilePath);
		} catch (SftpException e) {
			System.out.println("ファイル名"+targetFilePath+"は存在しません");
			return false;
		}
		System.out.println("ファイル名"+targetFilePath+"は存在します");
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
	private FileData getData(BouyomiBOTConection bc,Attachment a){
		if(a.getSize()<memorySize){
			ByteArrayOutputStream os=new ByteArrayOutputStream(a.getSize());
			bc.bot.downloadOutputStream(a.getUrl(),os);
			InputStream is =new ByteArrayInputStream(os.toByteArray());
			return new FileData(a.getSize(),is,a.getFileName());
		}else {
			try {
				int index=a.getFileName().lastIndexOf('.');
				String suffix=null;
				if(index>0&&index<a.getFileName().length())suffix=a.getFileName().substring(index);
				final File temp=Files.createTempFile("sftp_temp",suffix).toFile();
				FileOutputStream os=new FileOutputStream(temp);
				bc.bot.downloadOutputStream(a.getUrl(),os);
				final FileInputStream is=new FileInputStream(temp);
				return new FileData(a.getSize(),is,a.getFileName()) {
					@Override
					public void Final(){
						if(is!=null)try{
							is.close();
							temp.delete();
						}catch(IOException e){}
					}
				};
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	private boolean check(String... list) {
		for(String s:list) {
			if(s==null||s.isEmpty())return true;
		}
		return false;
	}
	@Override
	public void call(Tag tag){
		if(check(host,user,password,dir))return;
		if(tag.con.text.indexOf("SFTPアップロード")!=0)return;
		if(tag.con instanceof BouyomiBOTConection);
		else return;
		BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
		if(tag.con.userid.equals("543670181734645781")||//妖花ちゃん
				tag.con.userid.equals("544529530866368522")||//黄泉
				tag.con.userid.equals("581044447890898964")||//つるぎ
				tag.con.userid.equals("440102258143920128")){//天狗
			tag.con.text="";
			for(int i=0;i<bc.list.length;i++){
				System.out.println("DiscordにSFTP転送するべきファイルが投稿されました"+bc.list.length);
				FileData fd=getData(bc,bc.list[i]);
				if(fd!=null)synchronized(queue) {
					queue.add(fd);
				}
			}
			synchronized(this) {
				if(!conection) {
					conection=true;
					new Thread(this).start();
				}
			}
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
}