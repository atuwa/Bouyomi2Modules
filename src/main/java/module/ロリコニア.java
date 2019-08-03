package module;

import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util;

public class ロリコニア implements IModule{

	@Override
	public void call(Tag tag){
		String text=tag.con.text;
		if(text.indexOf("巨乳好き")>=0||text.indexOf("巨乳すき")>=0
				||text.indexOf("巨乳良い")>=0||text.indexOf("巨乳いい")>=0
				||text.indexOf("貧乳嫌い")>=0||text.indexOf("貧乳きらい")>=0) {
			tag.chatDefaultHost(Util.IDtoMention(tag.con.userid)+"異端者めが。国外追放だ");
		}
	}
}