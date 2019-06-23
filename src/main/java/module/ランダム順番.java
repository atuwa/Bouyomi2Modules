package module;

import java.security.SecureRandom;
import java.text.DecimalFormat;

import bouyomi.IModule;
import bouyomi.Tag;

public class ランダム順番 implements IModule{

	private static final DecimalFormat df = new DecimalFormat("#,##0.00");
	private SecureRandom rundom=new SecureRandom();
	public int kakuritu=1000;
	@Override
	public void call(Tag tag){
		if(tag.con.text.equals("Atuwa")||tag.con.text.equals("atuwa"))call(tag,"atuwa");
		if(tag.con.text.equals("KOBARU"))call(tag,"KOBARU");
		if(tag.con.text.equals("塔街"))call(tag,"塔街");
		if(tag.con.text.equals("TAKESHI"))call(tag,"TAKESHI");
		if(tag.con.text.equals("TAKESI"))call(tag,"TAKESI");
		if(tag.con.text.equals("ryosios"))call(tag,"ryosios");
	}
	public void call(Tag tag,String strings){
		tag.con.text="";
		int r=rundom.nextInt(10000);
		StringBuilder sb=new StringBuilder();
		if(r<kakuritu) {
			sb.append("あたり ").append(strings);
		}else {
			char[] chars=strings.toCharArray();
			String s=strings;
			while(true) {
				if(!s.equals(strings))break;
				for (int i=chars.length; i>1; i--) {
					int j=rundom.nextInt(i);
					char tmp = chars[i-1];
					chars[i-1] = chars[j];
					chars[j] = tmp;
				}
				s=new String(chars);
			}
			sb.append("はずれ ").append(s);
		}
		sb.append("/* ").append(r).append("抽選者：").append(tag.con.user).append(" 確率").append(df.format(kakuritu/100d)).append("%");
		tag.chatDefaultHost(sb.toString());
	}
}