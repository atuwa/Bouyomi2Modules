package module;

import java.security.SecureRandom;
import java.util.Random;

import bouyomi.IModule;
import bouyomi.Tag;

public class MoviePlayHistory implements IModule{
	@Override
	public void call(Tag tag){
		String seed=tag.getTag("ランダム動画");
		if(seed!=null) {
			Random rnd;
			if(seed.isEmpty())rnd=new SecureRandom();
			else rnd=new Random(seed.hashCode());
			int index=rnd.nextInt(TubeAPI.playHistory.size());
			String s=TubeAPI.playHistory.get(index);
			tag.chatDefaultHost("/"+TubeAPI.playHistory.size()+"件の履歴からランダムに選択された動画："+TubeAPI.IDtoURL(s));
		}
	}
}