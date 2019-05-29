package module;

import bouyomi.BouyomiProxy;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util.SaveProxyData;

public class CommentLogger implements IModule{

	public SaveProxyData logger;
	public CommentLogger(){
		String command=BouyomiProxy.Config.get("ログファイル");
		//0文字だったら無し、それ以外だったらそれ
		if(command!=null&&!command.isEmpty())logger=new SaveProxyData(command);
		System.out.println("ログ"+(logger==null?"無し":logger.file));
	}
	@Override
	public void call(Tag tag){
		if(logger!=null) logger.log(tag.con.user+"\t"+tag.con.text+"\t"+tag.con.userid);
	}
}
