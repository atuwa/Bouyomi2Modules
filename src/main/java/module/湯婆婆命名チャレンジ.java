package module;

import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.exceptions.HierarchyException;

public class 湯婆婆命名チャレンジ implements IModule{

	@Override
	public void call(Tag tag){
		int index=tag.con.text.indexOf("湯婆婆「フン。");
		if(index==0) {
			ゆばーば(tag,index,"というのかい？贅沢な名だねぇ。今からおまえの名前は","今からおまえの名前は","だ。いいかい、");
			ゆばーば(tag,index,"というのかい？貧相な名だねぇ。今からあんたの名前は","今からあんたの名前は","だ。");
			ゆばーば(tag,index,"というのかい？さてはアンチだなオメー","さては","だなオメー");
			ゆばーば(tag,index,"というのかい？贅沢な名だねぇ。今からおまえの名前は","今からおまえの名前は","だ。嫌かい？");
		}
	}
	private void ゆばーば(Tag tag,int index,String orgEndKey,String startKey,String endKey) {
		int end=tag.con.text.indexOf(orgEndKey);
		if(end<0)return;
		String name=tag.con.text.substring(index+7,end);
		//tag.chatDefaultHost("デバッグログ："+name);
		index=tag.con.text.indexOf(startKey);
		if(index>7+name.length()) {
			end=tag.con.text.indexOf(endKey);
			if(end<0)return;
			String newname=tag.con.text.substring(index+startKey.length(),end);
			if(DiscordBOT.bots==null||DiscordBOT.bots.isEmpty())return;
			if(newname.equals("アンチ"))newname="湯婆婆アンチ";
			try {
				String oldnick=tag.getUserNick(tag.con.userid);
				Guild g=DiscordBOT.DefaultHost.jda.getGuildById("566942640986390528");
				Member member=g.getMemberById(tag.con.userid);
				g.getController().setNickname(member,newname+"("+name+")").queue();
				tag.chatDefaultHost(oldnick+"→"+newname+"("+name+")");
			}catch(HierarchyException he) {
				tag.chatDefaultHost(he.getLocalizedMessage());
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}