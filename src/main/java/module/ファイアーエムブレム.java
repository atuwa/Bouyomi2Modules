package module;

import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class ファイアーエムブレム implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.text.contains("ファイヤーエンブレム")||
				tag.con.text.contains("ファイアーエンブレム")||
				tag.con.text.contains("ファーイヤンエンムレム")||
				tag.con.text.contains("ファイヤーエムブレム")) {
			if(!tag.con.text.contains("ファイアーエムブレム")) {
				tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"ファイアーエムブレムだ二度と間違えるな");
			}
		}
		if(tag.con.text.contains("ファイアーカイコガエムブレム")) {
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"ファイアーエムブレムだ二度と...なんだそれ？");
		}
		if(tag.con.text.contains("エーデルガルド")) {
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"エーデルガルトだ光柱撃つぞ");
		}
		if(tag.con.text.contains("ファイファン")) {
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"ファイナルファンタジーだ訂正しろ");
		}
	}
}