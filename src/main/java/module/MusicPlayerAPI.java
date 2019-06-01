package module;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import bouyomi.BouyomiProxy;
import bouyomi.IModule;
import bouyomi.Tag;

public class MusicPlayerAPI implements IModule{

	public static float Volume;
	public static String host;
	public MusicPlayerAPI(){
		host=BouyomiProxy.Config.get("mp3とwavファイルを再生するサーバ");
	}
	public void call(Tag t) {
		if(host==null)return;
		String tag=t.getTag("音楽再生");
		if(tag!=null) {
			String em;
			if(tag.isEmpty()) {
				MusicPlayerAPI.play();
				em="続きを再生します。";
			}else {
				MusicPlayerAPI.play(tag);
				em="音楽ファイルを再生します。";
			}
			float vol=MusicPlayerAPI.nowVolume();
			if(vol>=0)em+="音量は"+vol+"です";
			t.con.addTask.add(em);
		}
		tag=t.getTag("音楽音量");
		if(tag!=null) {
			if(tag.isEmpty()) {
				float vol=MusicPlayerAPI.nowVolume();
				t.con.addTask.add("音量は"+vol+"です");
				System.out.println("音楽音量"+vol);//ログに残す
			}else try{
				float vol=Float.parseFloat(tag);
				float Nvol=-10;
				switch(tag.charAt(0)){
					case '+':
					case '-':
					case 'ー':
						Nvol=MusicPlayerAPI.nowVolume();//+記号で始まる時今の音量を取得
				}
				if(Nvol==-1) {
					t.con.addTask.add("音量を変更できませんでした");//失敗した時これを読む
				}else {
					if(Nvol>=0)vol=Nvol+vol;//音量が取得させていたらそれに指定された音量を足す
					if(vol>100)vol=100;//音量が100以上の時100にする
					else if(vol<0)vol=0;//音量が0以下の時0にする
					if(MusicPlayerAPI.setVolume(vol)>=0)t.con.addTask.add("音量を"+vol+"にします");//動画再生プログラムにコマンド送信
					else t.con.addTask.add("音量を変更できませんでした");//失敗した時これを読む
					System.out.println(t.con.addTask.get(t.con.addTask.size()-1));//ログに残す
				}
			}catch(NumberFormatException e) {

			}
		}
		tag=t.getTag("音楽停止");
		if(tag!=null) {
			MusicPlayerAPI.stop();
			t.con.addTask.add("音楽を停止します。");
		}
	}
	//ID=5
	public static boolean play(){
		return send(null,(byte)5);
	}
	//ID=4
	public static boolean play(String tag){
		if(tag.indexOf("http")!=0)return false;
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		DataOutputStream dos=new DataOutputStream(baos);
		try{
			dos.write(4);
			dos.writeUTF(tag);
		}catch(IOException e){
			e.printStackTrace();
		}
		return send(null,baos.toByteArray());
	}
	//ID=3
	public static float nowVolume(){
		byte[] b=new byte[4];
		if(send(b,(byte)3));
		else return -1;
		int ch1=b[0]<0?b[0]+256:b[0];
		int ch2=b[1]<0?b[1]+256:b[1];
		int ch3=b[2]<0?b[2]+256:b[2];
		int ch4=b[3]<0?b[3]+256:b[3];
		int i=((ch1<<24)+(ch2<<16)+(ch3<<8)+(ch4<<0));
		return Float.intBitsToFloat(i);
	}
	//ID=2
	public static float setVolume(float vol){
		int i=Float.floatToIntBits(vol);
		byte[] b=new byte[5];
		b[0]=2;
		b[1]=(byte) ((i>>>24)&0xFF);
		b[2]=(byte) ((i>>>16)&0xFF);
		b[3]=(byte) ((i>>>8)&0xFF);
		b[4]=(byte) ((i>>>0)&0xFF);
		send(null,b);
		return vol;
	}
	//ID=1
	public static boolean stop(){
		return send(null,(byte)1);
	}
	/**棒読みちゃんに送信する*/
	public synchronized static boolean send(byte[] read,byte... data){
		if(data.length<1||host==null||host.isEmpty())return false;
		Socket soc=null;
		try{
			int port=6000;
			int beginIndex=host.indexOf(':');
			if(beginIndex>0) {
				port=Integer.parseInt(host.substring(beginIndex+1));
				host=host.substring(0,beginIndex);
			}
			soc=new Socket(host,port);
			OutputStream os=soc.getOutputStream();
			os.write(data);
			if(read!=null&&read.length>0) {
				InputStream is=soc.getInputStream();
				for(int i=0;i<read.length;i++) {
					int r=is.read();
					if(r<0)throw new IOException("データ不足");
					read[i]=(byte) r;
				}
			}
			return true;
		}catch(ConnectException e) {
			System.out.println("再生サーバに接続できません");
		}catch(UnknownHostException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}finally {
			try{
				if(soc!=null)soc.close();
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		return false;
	}
}
