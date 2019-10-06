package module;

import java.security.SecureRandom;
import java.util.ArrayList;

import bouyomi.IModule;
import bouyomi.Tag;

public class 改蟲我 implements IModule{

	private SecureRandom rand=new SecureRandom();
	private ArrayList<String> list=new ArrayList<String>(20);
	{
		list.add("チンコまた生えた");
		list.add("死んだ");
		list.add("しこってる");
		list.add("生きてるわけないよね");
		list.add("生きてない");
		list.add("誰それ");
		list.add("生きてる");
		list.add("たっちゃんに燃やされた");
		list.add("スズメバチに刺されて死んだ");
		list.add("死んでる");
		list.add("ほじくってる");
		list.add("自分のケツいじめてる");
		list.add("いきってる");
		list.add("機械に巻き込まれてる");
		list.add("脱いでる");
		list.add("脱いだ");
		list.add("砂利食ってる");
		list.add("ケツ毛抜いて快楽得てる");
		list.add("豚丼のどに詰まらせてる");
		list.add("65km走る");
		list.add("ﾎﾞﾛﾝしながら65km走る");
		list.add("999999ｍ走る");
		list.add("AV見てる");
		list.add("しごいてる");
		list.add("永い眠りについた");
		list.add("ポストの下に埋まってる");
		list.add("脱ごうとする");
		list.add("出口に大根入れてる");
	}
	@Override
	public void call(Tag tag){
		if(tag.getGuild()!=null)return;
		String s=tag.getTag("カイコガ生きてる？");
		if(s!=null) {
			int index=rand.nextInt(list.size());
			String get=list.get(index);
			tag.chatDefaultHost(get);
		}
		s=tag.getTag("カイコガ状態追加");
		if(s!=null) {
			list.add(s);
			tag.chatDefaultHost(s+" を追加しました");
		}
		s=tag.getTag("カイコガ状態削除");
		if(s!=null) {
			if(list.remove(s))tag.chatDefaultHost(s+" を削除しました");
		}
	}
}