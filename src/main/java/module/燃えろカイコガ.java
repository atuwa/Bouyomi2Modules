package module;

import java.security.SecureRandom;

import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class 燃えろカイコガ implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.text.equals("燃えろカイコガ")||tag.con.text.equals("燃えコガ")) {
			tag.chatDefaultHost(燃えろカイコガ抽選(tag));
		}else if(tag.con.text.equals("燃えコガ5連")||tag.con.text.equals("燃えコガ５連")||tag.con.text.equals("燃えコガ五連")) {
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<5;i++) {
				sb.append(燃えろカイコガ抽選(tag)).append("\n");
			}
			tag.chatDefaultHost(sb.toString());
		}else if(tag.con.text.equals("燃えコガ10連")||tag.con.text.equals("燃えコガ１０連")) {
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<10;i++) {
				sb.append(燃えろカイコガ抽選(tag)).append("\n");
			}
			if(((BouyomiBOTConection)tag.con).channel.getId().equals("608756623036383238"))tag.chatDefaultHost(sb.toString());
		}
	}
	private String 燃えろカイコガ抽選(Tag tag){
		int num=new SecureRandom().nextInt(10000);
		if(num<1) {
			return "🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥"+名前()+"🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥🔥\n"+
				"おおおおおおおおおおおおおお燃えたあああああああああああああああああああ/*\n"+
				"https://cdn.discordapp.com/attachments/569063021918552074/574198092471992353/5088f35b064742a3a9b69a7a0806d595.jpg"+
				Util.IDtoMention("534060196767465485")+"遂に燃えたぞ!!!!!!!!!!0.01%です。抽選者："+tag.con.user;
		}else if(num<11)return "🔥🔥🔥🔥🔥🔥🔥🔥🔥"+名前()+"🔥🔥🔥🔥🔥🔥🔥🔥🔥"+"/*0.1%です。抽選者："+tag.con.user;
		else if(num<211)return "🔥🔥🔥🔥"+名前()+"🔥🔥🔥🔥"+"/*2%です。抽選者："+tag.con.user;
		else if(num<711)return "🔥🔥"+名前()+"🔥🔥"+"/*5%です。抽選者："+tag.con.user;
		else if(num<2211)return "🔥"+名前()+"🔥"+"/*15%です。抽選者："+tag.con.user;
		else return 名前()+"<"+セリフ()+"/*"+tag.con.user;
	}
	private String 名前() {
		String[] kaikoga= {"解雇蛾","改蟲我","カイコガ",
				DiscordBOT.DefaultHost.getNick("566942640986390528","534060196767465485")};
		int num=new SecureRandom().nextInt(kaikoga.length);
		return kaikoga[num];
	}
	private String セリフ() {
		String[] kaikoga= {"助けてモチクゥン","やだぁ","ぬーん","やめてよぉ","なんでそんなことするの",
				"やれるもんならやってみろよ","プスクリ","？？？「違うって。君の性癖は、ガチ寄りのゲイだよ」　　　滅亡迅雷.netに接続…"};
		int num=new SecureRandom().nextInt(kaikoga.length);
		return kaikoga[num];
	}
}