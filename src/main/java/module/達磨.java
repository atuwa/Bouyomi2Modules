package module;

import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;
import module.NicoAlart.Live;
import module.NicoAlart.NicoLiveEvent;
import net.dv8tion.jda.api.JDA;

public class 達磨 implements IModule{

	@Override
	public void call(Tag tag){

	}
	@Override
	public void event(BouyomiEvent o) {
		if(o instanceof NicoLiveEvent) {
			Live live=((NicoLiveEvent)o).live[0];
			StringBuilder sb=new StringBuilder("生放送 ");
			sb.append(live.title);
			sb.append(" が開始されました/*画像省略の為URL非表示\n||");
			sb.append(live.getURL()).append("||");
			JDA jda=DiscordBOT.DefaultHost.jda;
			jda.getTextChannelById("569067304877555712").sendMessage(sb.toString()).queue();
			if(live.title.contains("絵")&&"1003067".equals(live.communityId)) {
				String url="https://cdn.discordapp.com/attachments/534291633110515714/596688931475554314/unknown.png";
				jda.getTextChannelById("566943792033169418").sendMessage(live.getURL()+"\n"+url).queue();
			}
		}
	}
}