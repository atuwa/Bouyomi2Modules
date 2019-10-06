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
			int end=tag.con.text.indexOf("というのかい？贅沢な名だねぇ。今からおまえの名前は");
			if(end<0)return;
			String name=tag.con.text.substring(index+7,end);
			//tag.chatDefaultHost("デバッグログ："+name);
			index=tag.con.text.indexOf("今からおまえの名前は");
			if(index>7+name.length()) {
				end=tag.con.text.indexOf("だ。いいかい、");
				String newname=tag.con.text.substring(index+10,end);
				if(DiscordBOT.bots==null||DiscordBOT.bots.isEmpty())return;
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
}