package tester.test;

public interface YoyHon {
	default void printMyDad() {
		System.out.println("YoyHon's default constructor");
	}
	
	void printMyName(String myName);
}