package tester.test;

public class TestIntController {

	private static int i;

	public String getTestColdResponseStr() throws InterruptedException {
		Thread.sleep(1000);
		String resp = "cold " + i++;
		System.out.println("emitted: " + resp);
		return resp;
	}

	public String getTestHotResponseStr() throws InterruptedException {
		Thread.sleep(1000);
		String resp = "hot " + i++;
		System.out.println("emitted: " + resp);
		return resp;
	}
}