package module;

import bouyomi.DailyUpdate;
import bouyomi.DailyUpdate.IDailyUpdate;
import bouyomi.DiscordBOT;
import bouyomi.IModule;
import bouyomi.Tag;

public class 時報 implements IDailyUpdate,IModule{
	{
		DailyUpdate.Ragister("atuwa.nonnon",this);
	}
	@Override
	public void call(Tag tag){
		if(tag.con.text.equals("のんのん再生"))update();
	}
	@Override
	public void update(){
		DiscordBOT.DefaultHost.send("566943792033169418","動画再生(v=grrX9elpi_A)");
	}

}
