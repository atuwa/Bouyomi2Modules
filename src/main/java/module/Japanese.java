package module;

import static bouyomi.BouyomiProxy.*;

import java.util.HashMap;
import java.util.regex.Pattern;

import bouyomi.BouyomiConection;
import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;

//こけえもんのローマ字を翻訳する
public class Japanese implements IModule{
	public DiscordBOT chat_server;
	private static HashMap<String,String> map=new HashMap<String,String>();
	public long lastMatch;
	public long block;
	private int blockCunt;
	public boolean active=true;
	public Japanese() {
		if("無効".equals(BouyomiProxy.Config.get("平仮名変換"))){
			active=false;
		}
		String command=BouyomiProxy.Config.get("ひらがな変換の投稿先BOTのID");
		//0文字だったら無し、それ以外だったらそれ
		if(command!=null&&!command.isEmpty()) {
			for(DiscordBOT b:DiscordBOT.bots) {
				if(b.jda.getSelfUser().getId().equals(command)) {
					chat_server=b;
					break;
				}
			}
		}
		System.out.println("ひらがな変換の投稿先BOTのID"+(chat_server==null?"無し":chat_server.jda.getSelfUser().getName()));
	}
	static {
		map.put("a","あ");map.put("i","い");map.put("u","う");map.put("e","え");map.put("o","お");
		map.put("ka","か");map.put("ki","き");map.put("ku","く");map.put("ke","け");map.put("ko","こ");
		map.put("sa","さ");map.put("si","し");map.put("su","す");map.put("se","せ");map.put("so","そ");
		map.put("ta","た");map.put("ti","ち");map.put("tu","つ");map.put("te","て");map.put("to","と");
		map.put("na","な");map.put("ni","に");map.put("nu","ぬ");map.put("ne","ね");map.put("no","の");
		map.put("ha","は");map.put("hi","ひ");map.put("hu","ふ");map.put("he","へ");map.put("ho","ほ");
		map.put("ma","ま");map.put("mi","み");map.put("mu","む");map.put("me","め");map.put("mo","も");
		map.put("ya","や");map.put("yi","い");map.put("yu","ゆ");map.put("ye","いぇ");map.put("yo","よ");
		map.put("ra","ら");map.put("ri","り");map.put("ru","る");map.put("re","れ");map.put("ro","ろ");
		map.put("wa","わ");map.put("wi","うぃ");map.put("wu","う");map.put("we","うぇ");map.put("wo","を");

		map.put("ca","か");map.put("ci","し");map.put("cu","く");map.put("ce","せ");map.put("co","こ");
		map.put("va","ヴぁ");map.put("vi","ヴぃ");map.put("vu","ヴ");map.put("ve","ヴぇ");map.put("vo","ヴぉ");

		map.put("cha","ちゃ");map.put("chi","ち");map.put("chu","ちゅ");map.put("che","ちぇ");map.put("cho","ちょ");
		map.put("xtu","っ");map.put("tsu","つ");
		map.put("lya","ゃ");map.put("li","ぃ");map.put("lu","ぅ");map.put("le","ぇ");map.put("lo","ぉ");
		map.put("xa","ぁ");map.put("xi","ぃ");map.put("xu","ぅ");map.put("xe","ぇ");map.put("xo","ぉ");

		map.put("kya","きゃ");map.put("kyi","きぃ");map.put("kyu","きゅ");map.put("kye","きぇ");map.put("kyo","きょ");
		map.put("sya","しゃ");map.put("syi","しぃ");map.put("syu","しゅ");map.put("sye","しぇ");map.put("syo","しょ");
		map.put("tya","ちゃ");map.put("tyi","ちぃ");map.put("tyu","ちゅ");map.put("tye","ちぇ");map.put("tyo","ちょ");
		map.put("nya","にゃ");map.put("nyi","にぃ");map.put("nyu","にゅ");map.put("nye","にぇ");map.put("nyo","にょ");
		map.put("hya","ひゃ");map.put("hyi","ひぃ");map.put("hyu","ひゅ");map.put("hye","ひぇ");map.put("hyo","ひょ");
		map.put("mya","みゃ");map.put("myi","みぃ");map.put("myu","みゅ");map.put("mye","みぇ");map.put("myo","みょ");
		map.put("rya","りゃ");map.put("ryi","りぃ");map.put("ryu","りゅ");map.put("rye","りぇ");map.put("ryo","りょ");
		map.put("gya","ぎゃ");map.put("gyi","ぎぃ");map.put("gyu","ぎゅ");map.put("gye","ぎぇ");map.put("gyo","ぎょ");
		map.put("za","ざ");map.put("zi","じ");map.put("zu","ず");map.put("ze","ぜ");map.put("zo","ぞ");
		map.put("ja","じゃ");map.put("ji","じ");map.put("ju","じゅ");map.put("je","じぇ");map.put("jo","じょ");
		map.put("jya","じゃ");map.put("jyi","じぃ");map.put("jyu","じゅ");map.put("jye","じぇ");map.put("jyo","じょ");
		map.put("zya","じゃ");map.put("zyi","じぃ");map.put("zyu","じゅ");map.put("zye","じぇ");map.put("zyo","じょ");
		map.put("da","だ");map.put("di","ぢ");map.put("du","づ");map.put("de","で");map.put("do","ど");
		map.put("ga","が");map.put("gi","ぎ");map.put("gu","ぐ");map.put("ge","げ");map.put("go","ご");
		map.put("ba","ば");map.put("bi","び");map.put("bu","ぶ");map.put("be","べ");map.put("bo","ぼ");
		map.put("pa","ぱ");map.put("pi","ぴ");map.put("pu","ぷ");map.put("pe","ぺ");map.put("po","ぽ");
		map.put("fa","ふぁ");map.put("fi","ふぃ");map.put("fu","ふ");map.put("fe","ふぇ");map.put("fo","ふぉ");
		map.put("byi","びゃ");map.put("byi","びぃ");map.put("byu","びゅ");map.put("bye","びぇ");map.put("byo","びょ");
		map.put("sha","しゃ");map.put("shi","し");map.put("shu","しゅ");map.put("she","しぇ");map.put("sho","しょ");
		map.put("vyu","ヴゅ");

		map.put("pha","ぴゃ");map.put("phi","ぴぃ");map.put("phu","ぴゅ");map.put("phe","ぴぇ");map.put("pho","ぴょ");
		map.put("tha","てゃ");map.put("thi","てぃ");map.put("thu","てゅ");map.put("the","てぇ");map.put("tho","てょ");

		map.put("nn","ん");map.put("n","ん");map.put(".","。");map.put(",","、");map.put("~","～");

		map.put("-","ー");
	}
	public boolean isTrans(String text) {
		for(int i=0;i<text.length();i++) {
			char c=text.charAt(i);
			if(c=='-'||c=='?'||c==','||c=='.'||c=='!'||c==' '||c=='/');
			else if(c>=0x30&&c<0x40);
			else if(c<0x60||c>0x7A)return false;
		}
		return true;
	}
	public boolean trans(BouyomiConection bc,String text) {
		//有効、5文字以上、投稿サーバあり、変換可能。を全て満たす時だけ変換
		if(!active||text.length()<5||chat_server==null||!isTrans(text))return false;
		if(block>System.currentTimeMillis())return false;
		text=text.trim();
		//chat_server.chat("変換前="+text+"文字数="+text.length());
		StringBuilder result=new StringBuilder();
		for(int i=0;i<text.length();i++) {
			char c=text.charAt(i);
			if(i+1<text.length()) {
				if(c==text.charAt(i+1)) {
					if(c=='a'||c=='i'||c=='u'||c=='e'||c=='o'||c=='/'||c=='^');
					else if(c>=0x30&&c<0x40);
					else if(c!='n'&&c!=' ') {
						result.append('っ');
						continue;
					}
				}
				if(i+2<text.length()) {
					String ms=new String(new char[] {c,text.charAt(i+1),text.charAt(i+2)});
					String r=map.get(ms);
					if(r!=null) {
						i+=2;
						result.append(r);
						continue;
					}
				}
				String ms=new String(new char[] {c,text.charAt(i+1)});
				String r=map.get(ms);
				if(r!=null) {
					i+=1;
					result.append(r);
					continue;
				}
			}
			String ms=String.valueOf(c);
			String r=map.get(ms);
			if(r!=null) {
				result.append(r);
			}else result.append(ms);
		}
		String r=result.toString();
		if(isTrans(r))return false;
		if(Pattern.compile("[a-z]").matcher(r).find())return false;
		chat_server.chat(bc,"/"+r);
		return true;
	}
	public void block(BouyomiConection bc) {
		System.out.println("NG掛かった");
		if(System.currentTimeMillis()-lastMatch<2000)blockCunt++;//2秒以内にNGに掛かった時
		else blockCunt=0;//それ以上空いていた時カウントをリセット
		lastMatch=System.currentTimeMillis();//最後に掛かった時間
		if(blockCunt>3) {//3回以上掛かった時
			block=lastMatch+30*1000;//ブロック時間10秒
			chat_server.chat(bc,"/NGワードを連投されたので30秒間変換を停止します。");
		}
	}
	@Override
	public void call(Tag t){
		String tag=t.getTag("平仮名変換");
		if(tag!=null) {
			Config.put("平仮名変換",tag);
			if(tag.equals("有効")) {
				active=true;
				t.con.addTask.add("平仮名変換機能を有効にしました");
			}else if(tag.equals("無効")) {
				active=false;
				t.con.addTask.add("平仮名変換機能を無効にしました");
			}
		}
	}
	@Override
	public void postcall(Tag tag){
		if(trans(tag.con, tag.con.text)){
			tag.con.text=tag.con.text.replaceAll("nn","n");
		}
	}
}
