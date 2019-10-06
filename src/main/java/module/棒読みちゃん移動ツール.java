package module;

import java.util.Map.Entry;

import bouyomi.BouyomiProxy;
import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class 棒読みちゃん移動ツール implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con instanceof BouyomiBOTConection);
		else return;
		String bid=tag.getTag("棒読みちゃん移動");
		if(bid!=null&&!bid.isEmpty()) {
			if(!tag.isAdmin()) {
				tag.chatDefaultHost("権限がありません");
				return;
			}
			MessageReceivedEvent ev=((BouyomiBOTConection)tag.con).event;
			if(!ev.isFromType(ChannelType.TEXT))return;
			for(DiscordBOT b:DiscordBOT.bots){
				for(Entry<String, String> e:b.speakListC.entrySet()){
					if(e.getValue().equals(bid)) {
						b.speakListC.remove(e.getKey());
						b.speakListC.put(ev.getTextChannel().getId(),bid);
						StringBuilder sb=new StringBuilder();
						TextChannel c1=b.jda.getTextChannelById(e.getKey());
						sb.append("「").append(c1.getGuild().getName()).append("の").append(c1.getName()).append("」から「");
						sb.append(ev.getGuild().getName()).append("の").append(ev.getTextChannel().getName()).append("」へ移動");
						tag.chatDefaultHost(sb.toString());
						break;
					}
				}
			}
		}
		bid=tag.getTag("臨時棒読みちゃん登録");
		if(bid!=null) {
			if(!tag.isAdmin()) {
				tag.chatDefaultHost("権限がありません");
				return;
			}
			MessageReceivedEvent ev=((BouyomiBOTConection)tag.con).event;
			if(!ev.isFromType(ChannelType.TEXT))return;
			if(bid.isEmpty()) {
				if(DiscordBOT.DefaultHost.speakListC.remove(ev.getTextChannel().getId())!=null) {
					tag.chatDefaultHost("正常に除去しました");
				}
			}else if(BouyomiProxy.ConectionTest(bid)==0) {
				DiscordBOT.DefaultHost.speakListC.put(ev.getTextChannel().getId(),bid);
				tag.chatDefaultHost("正常に登録しました");
			}else tag.chatDefaultHost("失敗しました");
		}
	}
}
