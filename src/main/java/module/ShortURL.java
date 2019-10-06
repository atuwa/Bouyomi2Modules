package module;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Tag.TagCommand;
import bouyomi.Util;
import bouyomi.Util.JsonUtil;

public class ShortURL implements IModule{

	@Override
	public void call(Tag tag){
		Matcher m=Pattern.compile("https?://\\S++").matcher(tag.con.text);
		StringBuffer sb=new StringBuffer();
		while(m.find()){
			//System.out.println(m.group());
			if(m.group().length()>200&&!tag.con.text.contains("短縮")) {
				int i=m.group().length()-1;
				if(m.group().charAt(i)!=')'&&m.group().charAt(i)!='）')m.appendReplacement(sb, "URL短縮("+m.group()+")");
			}
		}
		m.appendTail(sb);
		tag.con.text=sb.toString();
		TagCommand url0=tag.getTagCommand("URL短縮","ＵＲＬ短縮","url短縮");
		int mode=0;
		if(url0==null) {
			url0=tag.getTagCommand("ナズ短縮","nazrin短縮","ナズーリン短縮","ﾅｽﾞ短縮","ﾅｽﾞｰﾘﾝ短縮");
			mode=1;
			if(url0==null)return;
		}
		String url=url0.value;
		if(url.isEmpty()) {
			if(mode==1)tag.chatDefaultHost("https://nazr.in/");
			else tag.chatDefaultHost("https://kisu.me/");
			return;
		}
		try{
			String ret=shorten(mode,url);
			System.out.println("短縮="+ret+"　元URL="+url);
			if(tag.con instanceof BouyomiBOTConection) {
				url0.replaceTag(ret);
				BouyomiBOTConection bc=(BouyomiBOTConection) tag.con;
				try{
					DiscordBOT.DefaultHost.getTextChannel(bc.channel.getId()).deleteMessageById(bc.event.getMessageId()).queue();
					tag.chatDefaultHost(Util.IDtoMention(bc.userid)+"が送信 "+tag.con.text);
				}catch(Exception t) {
					t.printStackTrace();
					tag.chatDefaultHost(ret);
				}
			}else tag.chatDefaultHost(ret);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	private String shorten(int mode,String url) throws IOException{
		//String url="https://nicovideo.jp/watch/sm20285108";
		InputStream is=null;
		if(mode==0)is=kisume(url);
		else if(mode==1)is=nazrin(url);
		if(is==null)return null;
		byte[] b=new byte[512];
		int len;
		ByteArrayOutputStream res=new ByteArrayOutputStream();
		while(true) {
			len=is.read(b);
			if(len<1)break;
			res.write(b,0,len);
		}
		String json=res.toString("utf-8");
		if(json.isEmpty()) {
			System.out.println("短縮エラー");
			return null;
		}
		String ret=null;
		if(mode==0)ret="https://kisu.me/"+JsonUtil.get(json,"shorten").toString();
		else if(mode==1)ret=JsonUtil.get(json,"shortURL").toString();
		return ret;
	}
	private InputStream kisume(String url) throws IOException {
		url=URLEncoder.encode(url,"utf-8");
		URL url0=new URL("https://kisu.me/api/shorten.php?url="+url);
		URLConnection uc=url0.openConnection();
		uc.setRequestProperty("User-Agent","Atuwa Bouyomi Proxy");
		return uc.getInputStream();//POSTした結果を取得
	}
	private InputStream nazrin(String url) throws IOException {
		//url=URLEncoder.encode(url,"utf-8");
		URL url0=new URL("https://nazr.in/api/short_links");
		URLConnection uc=url0.openConnection();
		uc.setDoOutput(true);//POST可能にする
		uc.setRequestProperty("User-Agent","Atuwa Bouyomi Proxy");
		uc.setRequestProperty("Accept","application/json");
		byte[] b=("{\"url\":\""+url+"\"}").getBytes(StandardCharsets.UTF_8);
		// データがJSONであること、エンコードを指定する
		uc.setRequestProperty("Content-Type", "application/json");
		// POSTデータの長さを設定
		//uc.setRequestProperty("Content-Length", String.valueOf(b.length));
		OutputStream os=uc.getOutputStream();//POST用のOutputStreamを取得
		os.write(b);
		return uc.getInputStream();//POSTした結果を取得
	}
}