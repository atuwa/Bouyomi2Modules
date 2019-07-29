package module;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;

public class BouyomiKeeper implements IModule,IAutoSave{

	private int keep=50*60*1000;
	private final long sleep=5*60*1000;
	/**k=ホスト	v=時刻*/
	private HashMap<String,String> map=new HashMap<String,String>();
	/**k=チャンネル v=ホスト*/
	public HashMap<String, String> mix=new HashMap<String, String>();
	public int hash;
	private static final String FileName="BouyomiKeeper.txt";
	public BouyomiKeeper() {
		try{
			BouyomiProxy.load(map,FileName);
		}catch(IOException e){
			e.printStackTrace();
		}
		new BouyomiKeeperThread().start();
		for(DiscordBOT b:DiscordBOT.bots) {
			mix.putAll(b.speakListC);
			hash=mix.hashCode();
		}
		for(String c:mix.keySet()) {
			map.put(mix.get(c),Long.toString(System.currentTimeMillis()+keep));
			call();
		}
	}
	@Override
	public void call(Tag tag){
		int hc=0;
		for(DiscordBOT b:DiscordBOT.bots) {
			hc+=b.speakListC.hashCode();
		}
		if(hc!=0&&hc!=hash) {
			mix.clear();
			for(DiscordBOT b:DiscordBOT.bots) {
				mix.putAll(b.speakListC);
				hash=mix.hashCode();
			}
		}
		if(tag.con.mute)return;
		String k=tag.getTextChannel().getId();
		if(mix.containsKey(k)){
			map.put(mix.get(k),Long.toString(System.currentTimeMillis()+keep));
			call();
		}
	}
	private class BouyomiKeeperThread extends Thread{
		public BouyomiKeeperThread() {
			super("BouyomiKeeper");
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
			if(l-now<sleep) {
				String host=es.getKey();
				if(map.containsKey(host)) {
					BouyomiProxy.talk(host,"音量(10)接続維持");
				}
				map.put(host,Long.toString(System.currentTimeMillis()+keep));
				//System.out.println(host+"接続維持タイマ再設定");
				//System.out.println("スレッド起動");
			}
		}
	}
	@Override
	public void autoSave() throws IOException{

	}
	@Override
	public void shutdownHook() {
		try{
			BouyomiProxy.save(map,FileName);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}