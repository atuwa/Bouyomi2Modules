package module;

import java.util.Map.Entry;

import bouyomi.DiscordBOT;
import bouyomi.DiscordBOT.BouyomiBOTConection;
import bouyomi.IModule;
import bouyomi.Tag;
import net.dv8tion.jda.api.entities.TextChannel;

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
			for(DiscordBOT b:DiscordBOT.bots){
				for(Entry<String, String> e:b.speakListC.entrySet()){
					if(e.getValue().equals(bid)) {
						b.speakListC.remove(e.getKey());
						b.speakListC.put(tag.getTextChannel().getId(),bid);
						StringBuilder sb=new StringBuilder();
						TextChannel c1=b.jda.getTextChannelById(e.getKey());
						sb.append("「").append(c1.getGuild().getName()).append("の").append(c1.getName()).append("」から「");
						TextChannel c2=tag.getTextChannel();
						sb.append(c2.getGuild().getName()).append("の").append(c2.getName()).append("」へ移動");
						tag.chatDefaultHost(sb.toString());
						break;
					}
				}
			}
		}
	}
}
