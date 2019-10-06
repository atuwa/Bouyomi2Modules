package module;

import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class ファイアーエムブレム implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.text.contains("ファイヤーエンブレム")||
				tag.con.text.contains("ファイアーエンブレム")||
				tag.con.text.contains("ファイヤーエムブレム")) {
			if(!tag.con.text.contains("ファイアーエムブレム")) {
				tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"ファイアーエムブレムだ二度と間違えるな");
			}
		}
		if(tag.con.text.contains("ファイファン")) {
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"ファイナルファンタジーだ訂正しろ");
		}
	}

}
