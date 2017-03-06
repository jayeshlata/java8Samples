package tester.test;

public class Yoy2HonImpl implements YoyHon {

	@Override
	public void printMyName(String myName) {
		System.out.println(myName);
	}
	
	@Override
	public void printMyDad() {
		System.out.println("YoyHon's non default constructor");
	}
}