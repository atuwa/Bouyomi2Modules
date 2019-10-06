package module;

import bouyomi.BouyomiProxy;
import bouyomi.IModule;
import bouyomi.Tag;
import bouyomi.Util.SaveProxyData;

public class いちごロガー implements IModule{

	public SaveProxyData logger;
	public いちごロガー(){
		String command=BouyomiProxy.Config.get("いちご丸ログ");
		//0文字だったら無し、それ以外だったらそれ
		if(command!=null&&!command.isEmpty())logger=new SaveProxyData(command);
		System.out.println("いちごログ"+(logger==null?"無し":logger.getFile()));
	}
	@Override
	public void call(Tag tag){

	}
	@Override
	public void event(BouyomiEvent o) {
		if(o instanceof いちご丸.抽選) {

		}
		if(o instanceof いちご丸.距離変更イベント) {
			いちご丸.距離変更イベント ev=(いちご丸.距離変更イベント)o;
			StringBuilder sb=new StringBuilder();
			sb.append(ev.old);
			long sa=ev.now-ev.old;
			if(sa>=0)sb.append("+");
			sb.append(sa);
			sb.append("=");
			sb.append(ev.now);
			if(ev.t!=null) {
				sb.append("\t").append(ev.t.printMessage);
			}
			if(logger!=null) {
				logger.log(sb.toString());
			}
		}
	}
}
