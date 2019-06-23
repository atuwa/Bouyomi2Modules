package module;

import bouyomi.IModule;
import bouyomi.Tag;

public class Pause implements IModule{
	private boolean pause;
	private long time;
	public void precall(Tag t) {
		if(t.getTag("一時停止")!=null) {
			if(pause) {//解除
				if(System.currentTimeMillis()-time<5000) {
					t.con.addTask.add("ちょっと待って");
				}else{
					time=System.currentTimeMillis();
					pause=false;
					t.chatDefaultHost("一時停止を解除");
				}
			}else {//一時停止
				if(System.currentTimeMillis()-time<5000) {
					t.con.addTask.add("ちょっと待って");
				}else{
					time=System.currentTimeMillis();
					pause=true;
					t.chatDefaultHost("一時停止しました");
				}
			}
		}
		if(pause)t.con.text="";
	}
	@Override
	public void call(Tag tag){

	}

}
