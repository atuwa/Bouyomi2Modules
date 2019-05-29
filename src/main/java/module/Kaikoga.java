package module;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.BouyomiConection;
import bouyomi.BouyomiProxy;
import bouyomi.Counter;
import bouyomi.DailyUpdate;
import bouyomi.DailyUpdate.IDailyUpdate;
import bouyomi.DiscordAPI;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.ListMap;
import bouyomi.ListMap.Value;
import bouyomi.Tag;
import bouyomi.Util;

/** おまけ機能 */
public class Kaikoga implements IModule,IAutoSave{

	//k=id v=count
	private ListMap<String, String> kaikogaDB=new ListMap<String, String>();
	private int lastWriteHashCode;
	/**当選率はこの値/10*/
	private int kakuritu;
	private int up,upKaikoga;
	private Random rundom=new SecureRandom();
	public Kaikoga(){
		try{
			BouyomiProxy.load(kaikogaDB,"kaikoga.txt");
			lastWriteHashCode=kaikogaDB.hashCode();
		}catch(IOException e){
			e.printStackTrace();
		}
		String s=BouyomiProxy.Config.get("カイコガ本人ボーナス");
		try{
			upKaikoga=Integer.parseInt(s);
		}catch(NumberFormatException nfe) {
			nfe.printStackTrace();
		}
		IDailyUpdate update=new IDailyUpdate(){
			@Override
			public void update() {
				upKaikoga=0;
				kakuritu=rundom.nextInt(45)+1+5;
				DailyUpdate.chat("カイコガボロン率が"+(kakuritu/10F)+"%に変更されました\n本人ボーナスを0に初期化しました");
			}
			@Override
			public void init() {
				kakuritu=rundom.nextInt(45)+1+5;
			}
			@Override
			public void read(DataInputStream dis) throws IOException {
				kakuritu=(int) dis.readLong();
				System.out.println("起動時のボロン率"+(kakuritu/10F));
			}
			@Override
			public void write(DataOutputStream dos) throws IOException {
				dos.writeLong(kakuritu);
			}
		};
		DailyUpdate.Ragister("KaikogaKakuritu",update);
	}
	private int count(String key,String text) {
		Matcher m=Pattern.compile(key).matcher(text);
		int co=0;//素振りの数
		boolean result=m.find();
		if(result) {
			do{
				co++;
				result=m.find();
			}while(result);
		}
		return co;
	}
	@Override
	public void call(Tag tag){
		BouyomiConection con=tag.con;
		if(con.text.contains("ボロン")||con.text.contains("ﾎﾞﾛﾝ")||con.text.contains("ぼろん")) {
			int co=count("ボロン", con.text);
			co+=count("ﾎﾞﾛﾝ", con.text);
			co+=count("ぼろん", con.text);
			//DiscordAPI.chatDefaultHost(co+"回の素振り");
			up+=co;
			if(up>50)up=Integer.MIN_VALUE;
		}
		String str=tag.getTag("ボロンさせろ");
		if(str!=null){
			if(tag.isAdmin()){
				if(str.isEmpty()) str=con.userid;
				DiscordAPI.chatDefaultHost(tag,"了解。"+Counter.getUserName(str)+"の要求としてボロンさせます");
				hit(con,str);
			}else con.addTask.add("権限がありません");
		}
		str=tag.getTag("ボロン抹消");
		if(str!=null){
			if(tag.isAdmin()){
				if(str.isEmpty()) str=con.userid;
				String old=kaikogaDB.remove(str);
				if(old!=null) DiscordAPI.chatDefaultHost(tag,"了解。"+Counter.getUserName(str)+"のボロンを抹消します");
				else DiscordAPI.chatDefaultHost(tag,Counter.getUserName(str)+"のボロンを抹消出来ませんでした");
			}else con.addTask.add("権限がありません");
		}
		str=tag.getTag("ボロン減算");
		if(str!=null){
			if(tag.isAdmin()){
				String c=kaikogaDB.get(str);
				String n;
				if(c==null){
					n="1";
				}else try{
					int count=Integer.parseInt(c);
					n=Integer.toString(count<1 ? 0 : count-1);
				}catch(NumberFormatException nfe){
					n="1";
				}
				if("0".equals(n)){
					kaikogaDB.remove(str);
					DiscordAPI.chatDefaultHost(tag,"了解。"+Counter.getUserName(str)+"のボロンを抹消します");
				}else{
					kaikogaDB.put(str,n);
					DiscordAPI.chatDefaultHost(tag,"了解。"+Counter.getUserName(str)+"のボロンを"+n+"にさせます");
				}
			}else con.addTask.add("権限がありません");
		}
		str=tag.getTag("ボロン率再設定");
		if(str!=null) {
			if(tag.isAdmin()) {
				IDailyUpdate u=DailyUpdate.updater.target.get("KaikogaKakuritu");
				if(u!=null)u.update();
			}else DiscordAPI.chatDefaultHost(tag,"権限がありません");
		}
		str=tag.getTag("ボロン率");
		if(str!=null) {
			if(str.isEmpty()) {
				String s="現在のボロン率は"+(kakuritu/10F)+"%です";
				DiscordAPI.chatDefaultHost(tag,s);
			}else if(tag.isAdmin()) {
				try{
					kakuritu=(int)(Float.parseFloat(str)*10);
					String s="カイコガボロン率を"+(kakuritu/10F)+"%に変更しました";
					DiscordAPI.chatDefaultHost(tag,s);
				}catch(NumberFormatException nfe) {

				}
			}else DiscordAPI.chatDefaultHost(tag,"権限がありません");
		}
		if(con.text.contains("今素振り何回")||con.text.contains("今素振り何回?")) {
			StringBuilder sb=new StringBuilder(up+"回/*確率");
			kakuritu(sb);
			DiscordAPI.chatDefaultHost(tag,sb.toString());
		}
		if(con.text.equals("グレートカイコガ２")||con.text.equals("グレートカイコガ2")
				||con.text.equals("グレートカイコガ")||con.text.equals("greatKaikoga")||con.text.equals("GreatKaikoga")){
			int r=rundom.nextInt(1000)+1;//当選率可変
			int k=kakuritu+upKaikoga;
			if(up>20)k+=20;
			else k+=up;
			StringBuilder sb=new StringBuilder();
			if(r==1)sb.append("燃えた");
			else if(r<=k+1)sb.append("ボロン");
			else if(r<=k*1.5)sb.append("おしい");
			else sb.append("はずれ");
			sb.append(" (").append(r).append(")/*");
			sb.append("抽選者：").append(con.user);
			sb.append(" 確率");
			kakuritu(sb);
			sb.append(" ").append(up).append("回の素振り");
			up=0;
			if(!con.mute) {
				DiscordAPI.chatDefaultHost(tag,sb.toString());
				if(r==1) {
					DiscordAPI.chatDefaultHost(tag,"おおおおおおおおおおおおおお燃えたあああああああああああああああああああ/*\n"
			+ "https://cdn.discordapp.com/attachments/569063021918552074/574198092471992353/5088f35b064742a3a9b69a7a0806d595.jpg");
					DiscordAPI.chatDefaultHost(tag,Util.IDtoMention("544529530866368522")+"遂に燃えたぞ");
				}
				//DiscordAPI.chatDefaultHost(Util.IDtoMention(con.userid)+s);
			}
			if(r<=k+1) hit(con,con.userid);
			//if(r<5)con.addTask.add("おしい");
		}
		String t=tag.getTag("カイコガランキング");
		if(t!=null||"カイコガランキング".equals(con.text)){
			String s=rank(t);
			if(con.mute)System.out.println(s);
			else DiscordAPI.chatDefaultHost(tag,s);
		}
		if("534060196767465485".equals(con.userid)){
			str=tag.getTag("本人ボーナス");
			if(str!=null) {
				if(str.isEmpty())DiscordAPI.chatDefaultHost(tag,upKaikoga/10F+"%");
				else try{
					upKaikoga=(int) (Double.parseDouble(str)*10D);
					if(upKaikoga>100)upKaikoga=100;
					BouyomiProxy.Config.put("カイコガ本人ボーナス",Integer.toString(upKaikoga));
					DiscordAPI.chatDefaultHost(tag,"本人ボーナスを"+upKaikoga/10F+"%に設定");
				}catch(NumberFormatException nfe) {

				}
			}
		}
	}
	private void kakuritu(StringBuilder sb){
		int k=kakuritu;
		if(up>20)k+=20;
		else k+=up;
		sb.append(kakuritu/10F);
		sb.append("+").append((k-kakuritu)/10F);
		if(upKaikoga>0)sb.append("+").append(upKaikoga/10F);
		sb.append("=").append((k+upKaikoga)/10F);
		sb.append("%");
	}
	private void hit(BouyomiConection con,String id){
		con.addTask.add("おめでとう当たったよ");
		String c=kaikogaDB.get(id);
		String n;
		if(c==null){
			n="1";
		}else try{
			int count=Integer.parseInt(c);
			n=Integer.toString(count+1);
		}catch(NumberFormatException nfe){
			n="1";
		}
		kaikogaDB.put(id,n);
	}
	public String rank(String s){
		Comparator<Value<String,String>> c0=new Comparator<Value<String,String>>(){
			@Override
			public int compare(Value<String, String> o1,Value<String, String> o2){
				int n1=Integer.parseInt(o2.getValue());
				int n2=Integer.parseInt(o1.getValue());
				return Integer.compare(n1,n2);
			}
		};
		kaikogaDB.rawList().sort(c0);
		long all=0;
		for(Value<String, String> v:kaikogaDB.rawList()){
			try{
				all+=Integer.parseInt(v.getValue());
			}catch(NumberFormatException nfe){

			}
		}
		if(all<=0) return "ボロンした回数合計0回";
		DecimalFormat fo=new DecimalFormat("##0.00%");
		StringBuilder sb=new StringBuilder("ボロンした合計");
		sb.append(all).append("回/*\n");
		if(s!=null&&!s.isEmpty()) {
			String v=null;
			int i=-1;
			int rank=0;
			int count=0;
			Value<String, String> prev=null;
			for(int in=0;in<kaikogaDB.rawList().size();in++) {
				Value<String, String> va=kaikogaDB.rawList().get(in);
				if(prev!=null&&!va.getValue().equals(prev.getValue()))rank++;
				if(kaikogaDB.get(s)!=null&&kaikogaDB.get(s).equals(va.getValue()))count++;
				if(va.equalsKey(s)){
					v=va.getValue();
					i=rank;
				}
				//if(i>=0&&i!=rank)break;
				prev=va;
			}
			if(v!=null)appendUser(sb,fo,all,s,v);
			if(i<0)sb.append("ランキング外です");
			else sb.append(i+1).append("位です(").append(count).append("人)");
			return sb.toString();
		}
		for(int index=0;index<Math.min(5,kaikogaDB.size());index++){
			Value<String, String> v=kaikogaDB.rawList().get(index);
			appendUser(sb,fo,all,v.getKey(),v.getValue());
		}
		return sb.toString();
	}
	private void appendUser(StringBuilder sb,DecimalFormat fo,long all,String id,String value) {
		String name=Counter.getUserName(id);
		sb.append(name).append(" が").append(value).append("回");
		try{
			double i=Integer.parseInt(value);
			sb.append("(").append(fo.format(i/all)).append(")\n");
		}catch(NumberFormatException nfe){
			sb.append("\n");
		}
	}
	@Override
	public void autoSave(){
		int hc=kaikogaDB.hashCode();
		if(hc==lastWriteHashCode) return;
		lastWriteHashCode=hc;
		shutdownHook();
	}
	@Override
	public void shutdownHook(){
		if(kaikogaDB.isEmpty()) return;
		try{
			BouyomiProxy.save(kaikogaDB,"kaikoga.txt");
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}