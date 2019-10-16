package module;

import java.util.ArrayList;

import bouyomi.IModule;
import bouyomi.Tag;

public class Calculator implements IModule{

	@Override
	public void call(Tag tag){
		if(tag.con.mute||tag.con.text.indexOf('=')!=0)return;
		char[] ca=tag.con.text.toCharArray();
		StringBuilder rpn=new StringBuilder();
		boolean b=false;
		ArrayList<CalcType> al=new ArrayList<CalcType>();
		for(char c:ca) {
			if(c=='=')continue;
			if(c>=0x30&&c<0x40) {
				//c-=0x30;
				rpn.append(c);
			}
			if(c>='０'&&c<='９') {
				c-='０'+0x30;
				rpn.append(c);
			}
			if(c=='π')rpn.append(Math.PI);
			if(c=='e')rpn.append(Math.E);
			if(!b)if(c=='.'||c=='。') {
				rpn.append('.');
				b=true;
			}
			CalcType lasttype=null;
			if(c=='+'||c=='＋')lasttype=CalcType.add;
			else if(c=='-'||c=='―')lasttype=CalcType.subtraction;
			else if(c=='*'||c=='×'||c=='＊')lasttype=CalcType.multiplication;
			else if(c=='/'||c=='÷')lasttype=CalcType.division;
			if(lasttype!=null) {
				b=false;
				if(al.isEmpty()) {
					al.add(0,lasttype);
					rpn.append(" ");
				}
				else{
					CalcType i0=al.get(0);
					if(lasttype.priority()>i0.priority()) {
						al.add(0,lasttype);
						rpn.append(" ");
					}
					else {
						while(true) {
							rpn.append(" "+i0.toString());
							al.remove(0);
							if(al.isEmpty()) {
								al.add(lasttype);
								rpn.append(" ");
								break;
							}
							i0=al.get(0);
							if(lasttype.priority()>i0.priority()) {
								al.add(0,lasttype);
								rpn.append(" ");
								break;
							}
						}
					}
				}
			}
		}
		for(CalcType a:al)rpn.append(" ").append(a.toString());
		ArrayList<String> stack=new ArrayList<String>();
		for(String s:rpn.toString().split(" ")) {
			if(s.length()==1) {
				char c=s.charAt(0);
				if(c=='+')calc(stack,0);
				else if(c=='-')calc(stack,1);
				else if(c=='*')calc(stack,2);
				else if(c=='/')calc(stack,3);
				else stack.add(0,s);
			}else stack.add(0,s);
		}
		//tag.chatDefaultHost(rpn.toString()+"\n"+stack.get(0));
		if(!stack.isEmpty()&&stack.get(0)!=null&&!stack.get(0).isEmpty()) {
			tag.chatDefaultHost("/"+stack.get(0)+"です");
			tag.con.text="";
		}
	}
	private void calc(ArrayList<String> stack,int i){
		double n1=Double.parseDouble(stack.get(0));
		stack.remove(0);
		double n0=Double.parseDouble(stack.get(0));
		stack.remove(0);
		switch(i) {
			case 0:
				stack.add(0,Double.toString(n0+n1));
				break;
			case 1:
				stack.add(0,Double.toString(n0-n1));
				break;
			case 2:
				stack.add(0,Double.toString(n0*n1));
				break;
			case 3:
				stack.add(0,Double.toString(n0/n1));
				break;
		}
		//String s="中間結果"+n0+"["+i+"]"+n1+"="+stack.get(0);
		//System.out.println(s);
		//DiscordBOT.DefaultHost.send("566943792033169418",s);
	}
	private enum CalcType{
		add,//足し算
		subtraction,//引き算
		multiplication,//掛け算
		division;//割り算
		private int priority() {
			switch(this) {
				case multiplication:
					return 1;
				case division:
					return 2;
				default:
					return 0;
			}
		}
		public String toString() {
			switch(this) {
				case add:
					return "+";
				case subtraction:
					return "-";
				case multiplication:
					return "*";
				case division:
					return "/";
			}
			return null;
		}
	}
}