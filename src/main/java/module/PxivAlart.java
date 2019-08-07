package module;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import bouyomi.BouyomiProxy;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;

public class PxivAlart implements IModule,IAutoSave{
	private static final String pxivURL="https://sketch.pixiv.net/";
	private static String UserAgent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";
	private ArrayList<String> map=new ArrayList<String>();
	private boolean saved=false;
	public PxivAlart() {
		try{
			BouyomiProxy.load(map,"PxivAlart.txt");
			saved=true;
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	public class PxivLiveEvent implements BouyomiEvent{
		public final String url;
		private PxivLiveEvent(String u) {
			url=u;
		}
	}
	@Override
	public void call(Tag tag){
		String s=tag.getTag("配信");
		//if(tag.con.text.contains(pxivURL)&&tag.con.text.contains("配信開始")) {
		if(s!=null&&s.contains(pxivURL)) {
			//Matcher m=Pattern.compile("https?://\\S++").matcher(tag.con.text);
			//if(m.find())
			{
				//String urlS=m.group();
				String urlS=s;
				if(!map.contains(urlS))try{
					URL url=new URL(urlS);
					HttpURLConnection uc=(HttpURLConnection)url.openConnection();
					uc.setInstanceFollowRedirects(false);
					uc.setRequestProperty("User-Agent",UserAgent);
					InputStream is=uc.getInputStream();//POSTした結果を取得
					if(uc.getResponseCode()==200) {
						map.add(urlS);
						BouyomiProxy.module.event(new PxivLiveEvent(urlS));
						saved=false;
						tag.chatDefaultHost("配信中リストに追加しました");
					}else if(uc.getResponseCode()==302) {
						if(map.remove(urlS))saved=false;
						tag.chatDefaultHost("既に終了しています");
					}
					is.close();
				}catch(FileNotFoundException e) {

				}catch(IOException e){
					StringWriter sw=new StringWriter();
					PrintWriter pw=new PrintWriter(sw);
					e.printStackTrace(pw);
					pw.flush();
					tag.chatDefaultHost("あれー？\n"+sw.toString());
				}
			}
		}
		String u=tag.getTag("ピクシブ配信してる?","ピクシブ配信してる？","pxiv配信してる?","pxiv配信してる？",
				"支部助配信してる?","支部助配信してる？","支部助してる?","支部助してる？",
				"シブスケ配信してる？","シブスケ配信してる?","シブスケしてる？","シブスケしてる?",
				"誰か配信してる？","誰か配信してる?","だれか配信してる？","だれか配信してる?");
		if(u!=null) {
			checkALL(tag);
		}
		u=tag.getTag("このピクシブURL配信してる?","このピクシブURL配信してる？");
		if(u!=null)try{
			tag.chatDefaultHost(new Check(tag, u).toString());
		}catch(MalformedURLException e){
			e.printStackTrace();
			tag.chatDefaultHost("これURL間違えてない？");
		}
	}
	private void checkALL(Tag t) {
		ArrayList<String> l2=new ArrayList<String>();
		StringBuilder sb=new StringBuilder("");
		for(int i=0;i<map.size();i++) {
			try{
				Check check=new Check(t,map.get(i));
				if(check.lived) {
					sb.append(check.toString());
				}else l2.add(map.get(i));
			}catch(MalformedURLException e1){
				e1.printStackTrace();
			}
		}
		if(!l2.isEmpty())saved=false;
		map.removeAll(l2);
		if(sb.length()<1)sb.append("たぶん誰も配信してません");
		t.chatDefaultHost("/"+sb.toString());
	}
	private class Check{
		private boolean lived;
		private String title;
		private Check(Tag tag,String u) throws MalformedURLException{
			URL url=new URL(u);
			try{
				check(url);
			}catch(IOException e){
				StringWriter sw=new StringWriter();
				PrintWriter pw=new PrintWriter(sw);
				e.printStackTrace(pw);
				pw.flush();
				tag.chatDefaultHost("あれー？\n"+sw.toString());
			}
		}
		private void check(URL url) throws IOException {
			//System.out.println(url+" の配信チェック");
			HttpURLConnection uc=(HttpURLConnection)url.openConnection();
			uc.setInstanceFollowRedirects(false);
			uc.setRequestProperty("User-Agent",UserAgent);
			InputStream is=uc.getInputStream();//POSTした結果を取得
			if(uc.getResponseCode()==302) {
				//System.out.println(url+" は配信してないみたい（レスポンス302）");
				lived=false;
				return;
			}
			title=getTitle(is);
			title=title.replace(" is live on ","さんが");
			title=title.replace(" - pixiv Sketch","を配信中です");
			title=url+"\nで"+title;
			//lived=u.equals(uc.getURL().toString());
			//System.out.println("元URL="+u);
			//System.out.println("最終URL="+uc.getURL().toString());
			lived=true;
			try{
				Thread.sleep(500);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		public String toString() {
			if(lived)return title;
			return "配信してない";
		}
		private String getTitle(InputStream is) throws IOException {
			byte[] b=new byte[512];
			int len;
			ByteArrayOutputStream title=new ByteArrayOutputStream();
			int index=0;
			boolean start=false,end=false;
			char[] tt="<title>".toCharArray();
			char[] et="</title>".toCharArray();
			char[] TT="<TITLE>".toCharArray();
			char[] ET="</TITLE>".toCharArray();
			while(true) {
				len=is.read(b);
				if(len<1)break;
				if(!end)for(int i=0;i<len;i++) {
					if(start)title.write(b[i]);
					if(!start&&index+1==tt.length) {
						start=true;
						index=0;
					}
					if(!start&&(tt[index]==b[i]||TT[index]==b[i]))index++;
					else if(start&&(et[index]==b[i]||ET[index]==b[i]))index++;
					else index=0;
					if(index+1==et.length) {
						end=true;
						break;
					}
				}
			}
			String s=title.toString("utf-8");
			return s.substring(0,s.length()-7);
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
		if(saved)return;
		BouyomiProxy.save(map,"PxivAlart.txt");
		saved=true;
	}
}
