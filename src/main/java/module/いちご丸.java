package module;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bouyomi.BouyomiProxy;
import bouyomi.DailyUpdate;
import bouyomi.DailyUpdate.IDailyUpdate;
import bouyomi.IAutoSave;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class いちご丸 implements IModule,IAutoSave,IDailyUpdate{

	private SecureRandom ランダム生成源=new SecureRandom();
	private long 合計距離;
	private boolean 保存済;
	private HashMap<String,String> 今日引いた人達=new HashMap<String,String>();
	private static double 初期確率=20;
	private static int ノルマ=4000;
	private double 確率;
	private ArrayList<String> 今日歩いた距離=new ArrayList<String>();
	private boolean 今日歩いた距離保存済;
	private int 一日に引ける回数=1;//回数制限
	private static String 本人ID="534060228099178537";
	private UserList 気のせい引いた人=new UserList();
	private int 最後に書き込んだ気のせい引いた人リストのハッシュ値;
	public いちご丸(){
		try{
			BouyomiProxy.load(今日引いた人達,"いちご丸.txt");
			合計距離=Long.parseLong(今日引いた人達.get("合計距離"),16);
			一日に引ける回数=Integer.parseInt(今日引いた人達.get("一日に引ける回数"));
			今日引いた人達.remove("合計距離");
			今日歩いた距離保存済=true;
		}catch(IOException|NumberFormatException e){
			e.printStackTrace();
		}
		try{
			気のせい引いた人.readFromFile("いちご気のせい.long.array");
			最後に書き込んだ気のせい引いた人リストのハッシュ値=気のせい引いた人.hashCode();
		}catch(IOException e1){
			e1.printStackTrace();
		}
		try{
			BouyomiProxy.load(今日歩いた距離,"いちご丸確率.txt");
			確率=Double.parseDouble(今日歩いた距離.get(0));
			今日歩いた距離.remove(0);
			保存済=true;
		}catch(IOException|NumberFormatException e){
			e.printStackTrace();
		}
		if(確率<=0)確率=初期確率;
		DailyUpdate.Ragister("いちご丸",this);
	}
	private boolean 何回でも引けるか(Tag tag) {
		return 本人ID.equals(tag.con.userid)||tag.isAdmin();
	}
	@Override
	public void call(Tag tag){
		if(tag.con.text.equals("気のせいでしょ")&&tag.con.mentions.contains("581268794794573870")) {
			if(気のせい引いた人.contains(Long.parseLong(tag.con.userid))) {
				tag.chatDefaultHost("いや間違いないで/*"+tag.con.user+"さん");
			}else {
				気のせい抽選 t=new 気のせい抽選(tag);
				t.呼び出し();
			}
		}
		if(tag.con.text.equals("痩せろデブ")||tag.con.text.equals("痩せデブ")) {
			int 引いた回数=Integer.parseInt(今日引いた人達.getOrDefault(tag.con.userid,"0"));
			抽選 t=null;
			if(何回でも引けるか(tag)) {
				t=new 抽選(this,tag);
				if(本人ID.equals(tag.con.userid))t.いちご丸が呼び出し();
				else t.呼び出し();
			}else if(引いた回数>=一日に引ける回数) {//TODO 回数判定
				tag.chatDefaultHost("今日はもう制限まで引いたでしょ/*"+tag.con.user+"さん");
			}else{
				t=new 抽選(this,tag);
				t.呼び出し();
			}
			if(t!=null)BouyomiProxy.module.event(t);
		}
		if(tag.con.text.equals("今何メートル")||tag.con.text.toLowerCase().equals("今何m")||
				tag.con.text.equals("今何キロメートル")||tag.con.text.toLowerCase().equals("今何km")||
				tag.con.text.equals("今何メートル?")||tag.con.text.toLowerCase().equals("今何m?")||
				tag.con.text.equals("今何メートル？")||tag.con.text.toLowerCase().equals("今何ｍ？")) {
			StringBuilder 投稿メッセージ=new StringBuilder();
			if(tag.con.mute)投稿メッセージ.append("/");
			投稿メッセージ.append(合計距離).append("m(");
	        DecimalFormat df = new DecimalFormat("#,##0.0");
			投稿メッセージ.append(df.format(合計距離/1000D)).append("km)");
			tag.chatDefaultHost(投稿メッセージ.toString());
		}
		String パラメータ=tag.getTag("ちゃんと歩いたよ");
		if(パラメータ!=null) {
			boolean キロ指定=パラメータ.toLowerCase().contains("k");
			if(!キロ指定)キロ指定=パラメータ.contains("キロ");
			if(!キロ指定)キロ指定=パラメータ.contains("㌔");
			if(!キロ指定)キロ指定=パラメータ.contains("ｷﾛ");
			Matcher m=Pattern.compile("[0-9０-９]++.?[0-9０-９]*+").matcher(パラメータ);
			if(m.find()){
				String 数値抽出文字列=m.group();
				double 指定値=Double.parseDouble(数値抽出文字列);
				if(キロ指定)指定値*=1000;
				if(指定値>20000) {
					tag.chatDefaultHost("指定ミスってない？("+指定値+"m)");
				}else {
					今日歩いた距離.add(String.valueOf((int)指定値));
					long 元の距離=合計距離;
					合計距離=(long) (合計距離-指定値);
					StringBuilder システムメッセージ=new StringBuilder("いちご丸が");
					システムメッセージ.append(元の距離-合計距離).append("m歩いて残り");
					システムメッセージ.append(合計距離).append("m");
					運動ログ(システムメッセージ.toString());
					System.out.println(システムメッセージ);
					StringBuilder 投稿メッセージ=new StringBuilder();
					if(tag.con.mute)投稿メッセージ.append("/");
					投稿メッセージ.append("残り");
					boolean limit=false;
					if(リミッター())limit=true;
					投稿メッセージ.append(合計距離).append("m(");
			        DecimalFormat df = new DecimalFormat("#,##0.0");
					投稿メッセージ.append(df.format(合計距離/1000D)).append("km)");
					if(limit)投稿メッセージ.append("下限-2km");
					int 距離=0;
					for(String s:今日歩いた距離) {
						try{
							距離+=Integer.parseInt(s);
						}catch(NumberFormatException nfe) {

						}
					}
					投稿メッセージ.append("今日歩いた距離は").append(距離).append("mです。");
					if(距離>ノルマ) {
						投稿メッセージ.append("ノルマ達成です。確率は").append(初期確率).append("%になります。");
					}else {
						投稿メッセージ.append("ノルマ達成出来てません。確率は").append(確率).append("%になります。");
					}
					今日歩いた距離保存済=false;
					保存済=false;
					tag.chatDefaultHost(投稿メッセージ.toString());
				}
			}else tag.con.addTask.add("数値を指定してください");
		}
		パラメータ=tag.getTag("いちご値");
		if(パラメータ!=null) {
			if(パラメータ.isEmpty()) {
				tag.chatDefaultHost(tag.con.mute?"/":""+Long.toString(合計距離));
			}else if(tag.isAdmin())	try{
				合計距離=Long.parseLong(パラメータ);
				保存済=false;
		        DecimalFormat df = new DecimalFormat("#,##0.0");
				tag.chatDefaultHost(tag.con.mute?"/残り":"残り"+合計距離+"m("+df.format(合計距離/1000D)+"km)");
			}catch(NumberFormatException nfe) {}
		}
		パラメータ=tag.getTag("いちご引ける回数");
		if(パラメータ!=null) {
			if(パラメータ.isEmpty()) {
				int 引いた回数=Integer.parseInt(今日引いた人達.getOrDefault(tag.con.userid,"0"));
				if(何回でも引けるか(tag)) {
					tag.chatDefaultHost("/1日"+一日に引ける回数+"回です("+tag.con.user+"さんは何回でも引けます。今日引いた回数は"+引いた回数+"回です)");
				}else {
					int 引ける回数=一日に引ける回数-引いた回数;
					tag.chatDefaultHost("/1日"+一日に引ける回数+"回です("+tag.con.user+"さんはあと"+引ける回数+"回です)");
				}
			}else if(tag.isAdmin())	try{
				一日に引ける回数=Integer.parseInt(パラメータ);
				tag.chatDefaultHost(tag.con.mute?"/":""+"1日に引ける回数を"+一日に引ける回数+"回に変更しました");
				保存済=false;
			}catch(NumberFormatException nfe) {}
		}
		パラメータ=tag.getTag("今日歩いた距離");
		if(パラメータ!=null) {
			int 距離=0;
			for(String s:今日歩いた距離) {
				try{
					距離+=Integer.parseInt(s);
				}catch(NumberFormatException nfe) {

				}
			}
			StringBuilder sb=new StringBuilder("いちご丸が今日歩いた距離は");
			sb.append(距離).append("mです。");
			if(距離>ノルマ) {
				sb.append("ノルマ達成です。確率は").append(初期確率).append("%になります。");
			}else {
				sb.append("ノルマ達成出来てません。確率は").append(確率).append("%になります。");
			}
			tag.chatDefaultHost(sb.toString());
		}
		パラメータ=tag.getTag("いちご率");
		if(パラメータ!=null) {
			StringBuilder sb=new StringBuilder();
			if(!パラメータ.isEmpty()&&(本人ID.equals(tag.con.userid)||tag.isAdmin())) {
				try {
					確率=Double.parseDouble(パラメータ);
					if(確率>100)確率=100;
					else if(確率<0)確率=0;
					sb.append("確率を").append(確率).append("%に変更しました。");
					保存済=false;
				}catch(NumberFormatException nfe) {

				}
			}else {
				sb.append("確率は").append(確率).append("%です。");
			}
			tag.chatDefaultHost(sb.toString());
		}
		パラメータ=tag.getTag("今日引いた人達");
		if(パラメータ!=null) {
			StringBuilder sb=new StringBuilder("/");
			for(Entry<String, String> s:今日引いた人達.entrySet()){
				try{
					Long.parseLong(s.getKey());
				}catch(NumberFormatException e) {
					continue;
				}
				sb.append(tag.getUserName(s.getKey())).append(" さんが ").append(s.getValue()).append("回");
				try{
					if(気のせい引いた人.contains(Long.parseLong(s.getKey())))sb.append("(気のせい済)");
				}catch(NumberFormatException e) {}
				sb.append("\n");
			}
			tag.chatDefaultHost(sb.toString());
		}
		パラメータ=tag.getTag("痩せろデブをもう一回引けるようにする");
		if(パラメータ!=null&&(本人ID.equals(tag.con.userid)||tag.isAdmin())) {
			if(パラメータ.isEmpty()) {
				tag.chatDefaultHost(tag.con.mute?"/":""+今日引いた人達.size()+"件の引いた人リストを消去しました");
				今日引いた人達.clear();
				気のせい引いた人.clear();
			}else if(今日引いた人達.containsKey(パラメータ)) {
				if(今日引いた人達.remove(パラメータ)!=null) {
					try{
						気のせい引いた人.remove(Long.parseLong(パラメータ));
					}catch(NumberFormatException e) {}
					tag.chatDefaultHost(tag.con.mute?"/":""+tag.getUserName(パラメータ)+"を引いた人リストから消去しました");
				}
			}
		}
	}
	private void 運動ログ(String ログテキスト) {
		SimpleDateFormat 日付フォーマットするやつ = new SimpleDateFormat("yyyy年MM月dd日HH時mm分");
		String 日付時刻=日付フォーマットするやつ.format(new Date());
		try{
			FileOutputStream fos=new FileOutputStream("いちご丸.log",true);//追加モードで開く
			BufferedOutputStream ログ出力先=new BufferedOutputStream(fos);
			try{
				ログ出力先.write((日付時刻+"\t"+ログテキスト+"\t"+System.currentTimeMillis()+"\n").getBytes(StandardCharsets.UTF_8));
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try{
					ログ出力先.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}
	}
	private class 気のせい抽選{
		private Tag tag;
		public 気のせい抽選(Tag tag){
			this.tag=tag;
		}
		public void 呼び出し(){
			String 今までに引いた回数=今日引いた人達.get(tag.con.userid);
			if(今までに引いた回数==null||今までに引いた回数.isEmpty()) {
				tag.chatDefaultHost("まだ引いてないよ/*"+tag.con.user+"さん");
				return;
			}else if(Integer.parseInt(今までに引いた回数)<一日に引ける回数){
				int 残り=一日に引ける回数-Integer.parseInt(今までに引いた回数);
				tag.chatDefaultHost("あと"+残り+"回引けるよ/*"+tag.con.user+"さん");
				return;
			}
			気のせい引いた人.add(Long.parseLong(tag.con.userid));
			int ランダム値=ランダム生成源.nextInt(100);
			if(ランダム値>=10) {
				tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"お前もう引いただろ(確率10%)");
			}else if(今日引いた人達.remove(tag.con.userid)!=null) {
				tag.chatDefaultHost("そうか気のせいか。/*"+tag.con.user+"は引けるようにしてやるよ。(確率10%)");
			}
		}
	}
	public static class 抽選 implements BouyomiEvent{
		public いちご丸 親;
		public Tag tag;
		public double ランダム値;
		public long 古い合計距離;
		public 抽選(いちご丸 i,Tag tag){
			親=i;
			this.tag=tag;
			古い合計距離=親.合計距離;
			String 今までに引いた回数=親.今日引いた人達.get(tag.con.userid);
			if(今までに引いた回数==null||今までに引いた回数.isEmpty())今までに引いた回数="1";
			else 今までに引いた回数=Integer.toString(Integer.parseInt(今までに引いた回数)+1);
			親.今日引いた人達.put(tag.con.userid,今までに引いた回数);
			親.保存済=false;
		}
		public void いちご丸が呼び出し() {
			ランダム値=親.ランダム生成源.nextDouble()*100;//0～1000のランダムを生成
			if(ランダム値<0.5)まさかこれを引くとは("0.5%");
			else if(ランダム値<5.5)むしゃむしゃ("5%");
			else if(ランダム値<親.確率+5.5)行く(親.確率+"%");
			else やだ((100-(親.確率+5.5))+"%");
		}
		public void 呼び出し() {
			ランダム値=親.ランダム生成源.nextDouble()*100;//0～1000のランダムを生成
			if(ランダム値<0.1)まさかこれを引くとは("0.1%");
			else if(ランダム値<1.1)むしゃむしゃ("1%");
			else if(ランダム値<親.確率+1.1)行く(親.確率+"%");
			else やだ((100-(親.確率+1.1))+"%");
		}
		private void まさかこれを引くとは(String 確率メッセージ){
			親.今日引いた人達.put(tag.con.userid,String.valueOf(親.一日に引ける回数));
			int 増える量=15000;//15km増える
			親.合計距離+=増える量;
			StringBuilder sb=new StringBuilder();
			if(tag.con.mute)sb.append("/");
			sb.append(Util.IDtoMention(tag.con.userid));
			sb.append(確率メッセージ).append("のこれを引くとは凄い運だな\n15km行く");
			sb.append("(").append(ランダム値).append(")");
			if(親.リミッター())sb.append("42.195km上限");
			定型文(sb,確率メッセージ);
			String s=sb.toString();
			System.out.println(s);
			tag.chatDefaultHost(s);
		}
		private void むしゃむしゃ(String 確率メッセージ){
			//単位はメートルで
			int 距離=親.ランダム生成源.nextInt(450)+50;
			親.合計距離=親.合計距離-距離;
			StringBuilder sb=new StringBuilder();
			if(tag.con.mute)sb.append("/");
			sb.append(距離).append("m減った");
			//sb.append("(").append(ランダム値).append(")");
			if(親.リミッター())sb.append("-2km下限");
			定型文(sb,確率メッセージ);
			String s=sb.toString();
			System.out.println(s);
			tag.chatDefaultHost(s);
		}
		private void 行く(String 確率メッセージ){
			//単位はメートルで
			int 距離=親.ランダム生成源.nextInt(550)+250;
			親.合計距離=親.合計距離+距離;
			StringBuilder sb=new StringBuilder();
			if(tag.con.mute)sb.append("/");
			sb.append(距離).append("m行く");
			//sb.append("(").append(ランダム値).append(")");
			if(親.リミッター())sb.append("42.195km上限");
			定型文(sb,確率メッセージ);
			String s=sb.toString();
			System.out.println(s);
			tag.chatDefaultHost(s);
		}
		private void やだ(String 確率メッセージ){
			StringBuilder sb=new StringBuilder();
			if(tag.con.mute)sb.append("/");
			sb.append("行かない");
			//sb.append("(").append(ランダム値).append(")");
			定型文(sb,確率メッセージ);
			String s=sb.toString();
			System.out.println(s);
			tag.chatDefaultHost(s);
		}
		private void 定型文(StringBuilder sb,String 確率メッセージ) {
			sb.append("/*");
			変化メッセージ(sb);
			sb.append(" 抽選者：").append(tag.con.user);
			int 引いた回数=Integer.parseInt(親.今日引いた人達.get(tag.con.userid));
			if(親.何回でも引けるか(tag)) {
				sb.append("(").append(引いた回数).append("回目)");
			}else {
				int 引ける回数=親.一日に引ける回数-引いた回数;
				sb.append("(あと").append(引ける回数).append("回)");
			}
			sb.append(" 確率").append(確率メッセージ);
		}
		private void 変化メッセージ(StringBuilder 書き込み先) {
			long 増えた分=親.合計距離-古い合計距離;
			if(増えた分==0)return;
	        DecimalFormat df = new DecimalFormat("#,##0");
			書き込み先.append("残り").append(df.format(古い合計距離));
			if(増えた分<0)書き込み先.append(増えた分);
			else 書き込み先.append("+").append(増えた分);
			書き込み先.append("=").append(df.format(親.合計距離)).append("m(");
	        DecimalFormat df2 = new DecimalFormat("#,##0.0");
	        書き込み先.append(df2.format(親.合計距離/1000D)).append("km)");
		}
	}
	protected boolean リミッター() {
		int 最大値=84390;//TODO 上限84.39km
		int 最小値=-42195;//TODO 下限-42.195km
		if(合計距離>最大値) {
			合計距離=最大値;
			return true;
		}
		if(合計距離<最小値) {
			合計距離=最小値;
			return true;
		}
		return false;
	}
	@Override
	public void autoSave() throws IOException{
		shutdownHook();
	}
	public void shutdownHook() {
		if(!保存済) {
			String 距離文字列=Long.toHexString(合計距離);
			今日引いた人達.put("合計距離",距離文字列);
			今日引いた人達.put("一日に引ける回数",String.valueOf(一日に引ける回数));
			try{
				BouyomiProxy.save(今日引いた人達,"いちご丸.txt",false);
				保存済=true;
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		if(!今日歩いた距離保存済) {
			@SuppressWarnings("unchecked")
			ArrayList<String> コピー=(ArrayList<String>) 今日歩いた距離.clone();
			String 確率文字列=Double.toString(確率);
			コピー.add(0,確率文字列);
			try{
				BouyomiProxy.save(コピー,"いちご丸確率.txt");
				今日歩いた距離保存済=true;
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		int 気のせいハッシュ値=気のせい引いた人.hashCode();
		if(最後に書き込んだ気のせい引いた人リストのハッシュ値!=気のせいハッシュ値)try{
			気のせい引いた人.writeToFile("いちご気のせい.long.array");
			最後に書き込んだ気のせい引いた人リストのハッシュ値=気のせいハッシュ値;
		}catch(IOException e1){
			e1.printStackTrace();
		}
	}
	@Override
	public void update(){
		DailyUpdate.chat("痩せろデブが引けるようになりました");
		今日引いた人達.clear();
		気のせい引いた人.clear();
		int 距離=0;
		for(String s:今日歩いた距離) {
			try{
				距離+=Integer.parseInt(s);
			}catch(NumberFormatException nfe) {

			}
		}
		今日歩いた距離.clear();
		StringBuilder sb=new StringBuilder("いちご丸が今日歩いた距離は");
		sb.append(距離).append("です。");
		if(距離>ノルマ) {
			確率=初期確率;
			sb.append("ノルマ達成したので確率は").append(初期確率).append("%になります。");
		}else {
			確率+=2.5;
			sb.append("ノルマ達成出来なかったので確率は").append(確率).append("%になります。");
		}
		DailyUpdate.chat(sb.toString());
		保存済=false;
		今日歩いた距離保存済=false;
	}
}