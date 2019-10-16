package module;

import java.security.SecureRandom;

import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;

public class 燃えろカイコガ implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.text.equals("燃えろカイコガ")) {
			int num=new SecureRandom().nextInt(1000);
			if(num<1)tag.chatDefaultHost("🔥🔥🔥🔥🔥🔥🔥🔥🔥"+名前()+"🔥🔥🔥🔥🔥🔥🔥🔥🔥"+"/*0.1%です。抽選者："+tag.con.user);
			else if(num<21)tag.chatDefaultHost("🔥🔥🔥🔥"+名前()+"🔥🔥🔥🔥"+"/*2%です。抽選者："+tag.con.user);
			else if(num<71)tag.chatDefaultHost("🔥🔥"+名前()+"🔥🔥"+"/*5%です。抽選者："+tag.con.user);
			else if(num<221)tag.chatDefaultHost("🔥"+名前()+"🔥"+"/*15%です。抽選者："+tag.con.user);
			else tag.chatDefaultHost(名前()+"<"+セリフ()+"/*"+tag.con.user);
		}
	}
	private String 名前() {
		String[] kaikoga= {"解雇蛾","改蟲我","カイコガ",
				DiscordBOT.DefaultHost.getNick("566942640986390528","534060196767465485")};
		int num=new SecureRandom().nextInt(kaikoga.length);
		return kaikoga[num];
	}
	private String セリフ() {
		String[] kaikoga= {"助けてモチクゥン","やだぁ","ぬーん","やめてよぉ","なんでそんなことするの",
				"やれるもんならやってみろよ","プスクリ"};
		int num=new SecureRandom().nextInt(kaikoga.length);
		return kaikoga[num];
	}
}