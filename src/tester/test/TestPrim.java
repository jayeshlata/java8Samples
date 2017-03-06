package tester.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;

public class TestPrim {
	public static void main(String[] args) {
		// testLambdaAndStream();
		// testLambdaAndStreamBasedRuleRunning();
		// testDefaultConstructor();
		testRxCold();
		// testRxColdCache();
		// testRxHot();
		// testZipExceptions();
		// testHttpObservable();
	}

	private static void testLambdaAndStream() {
		List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");

		long count = strings.stream().count();
		System.out.println(count);

		List<String> stringsNew = strings.stream().map(i -> {
			return i + 1;
		}).collect(Collectors.groupingBy(i -> i.length())).entrySet().stream().map(i -> {
			return i.getKey() + "" + 1;
		}).collect(Collectors.toList());
		System.out.println(stringsNew);

		List<List<String>> exList = Arrays.asList(strings, stringsNew);

		List<String> tObj = exList.parallelStream().map(c -> c).flatMap(List::stream).collect(Collectors.toList());
		System.out.println(tObj);
	}

	private static void testLambdaAndStreamBasedRuleRunning() {
		List<String> strings = Arrays.asList("abc", "", "bc", "efg", "abcd", "", "jkl");

		Predicate<String> isNullRule = j -> j != null && !j.equals("");
		Predicate<String> isNotNullRule = j -> j.length() > 2;

		List<Predicate<String>> ruleList = new LinkedList<>();
		ruleList.add(isNullRule);
		ruleList.add(isNotNullRule);

		List<String> newList = strings;

		ruleList.stream().forEach(r -> {
			newList.removeAll(strings.stream().filter(e -> !applyRule(e, r)).collect(Collectors.toList()));
		});

		System.out.println("w -> a " + strings);
		System.out.println("x -> a " + newList);
	}

	private static void testDefaultConstructor() {

		YoyHon yh1 = new YoyHonImpl();
		yh1.printMyDad();
		yh1.printMyName("chao1");

		YoyHon yh2 = new Yoy2HonImpl();
		yh2.printMyDad();
		yh2.printMyName("chao2");
	}

	private static boolean applyRule(String str, Predicate<String> isEmpStr) {
		boolean fu = isEmpStr.test(str);
		System.out.println("fu: " + fu);
		return fu;
	}

	private static void testRxCold() {
		try {
			Observable<String> fu1 = Observable.<String>create(sub -> {
				for (int i = 0; i < 2; i++) {
					sub.onNext(getIt(true));
				}
				sub.onCompleted();
			}); // cache and do on completed methods return observable on which
				// caching and the action works
			// no caching here, emits and replays all values for any new
			// subscription

			// fu1.single().subscribe(s -> System.out.println("received: " +
			// s));
			// works if only a single response
			// Thread.sleep(2000);

			fu1 = fu1.doOnCompleted(new Action0() {

				@Override
				public void call() {
					System.out.println("blewy 1");
				}
			});

			/*
			 * fu1.subscribe(s -> System.out.println("received on sub 1: " +
			 * s)); Thread.sleep(2000);
			 * 
			 * fu1 = fu1.cache();
			 * 
			 * fu1.subscribe(s -> System.out.println("received on sub 2: " +
			 * s)); Thread.sleep(2000);
			 * 
			 * fu1.subscribe(s -> System.out.println("received on sub 3: " +
			 * s)); Thread.sleep(2000);
			 */
			Observable<Integer> fu2 = Observable.<Integer>create(sub -> {
				Runnable run1 = new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < 5; i++) {
							sub.onNext(i);
							try {
								Thread.currentThread().sleep(2000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						sub.onCompleted();
					}
				};
				Thread th1 = new Thread(run1);
				th1.start();
			}).doOnCompleted(new Action0() {

				@Override
				public void call() {
					System.out.println("blewy 2");
				}
			});

			Observable<SomeRanClass> fu3 = Observable.<SomeRanClass>create(sub -> {
				Runnable run2 = new Runnable() {

					@Override
					public void run() {

						for (int i = 0; i < 5; i++) {
							try {
								Thread.currentThread().sleep(1500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							sub.onNext(new SomeRanClass());
						}
						sub.onCompleted();

					}
				};
				Thread th2 = new Thread(run2);
				th2.start();
			}).doOnCompleted(new Action0() {

				@Override
				public void call() {
					System.out.println("blewy 2");
				}
			});
			Observable.merge(fu2, fu3).subscribe(s -> System.out.println("received on merge: " + s));
			// only difference in merge and zip is that you can choose how to
			// merge in zip method, while merge just makes a List<Object> and
			// puts everything in it.

			Observable<Map<String, Integer>> ew = Observable.zip(fu1, fu2, (fuR1, fuR2) -> {
				Map<String, Integer> mma = new HashMap<>();
				mma.put(fuR1, fuR2);
				return mma;
			});
			ew.subscribe(s -> System.out.println("received on zip: " + s),
					exception -> System.out.println("received exception on zip: " + exception.getMessage()));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testZipExceptions() {
		Observable<String> fu4 = Observable.<String>create(sub -> {
			String ro = null;
			sub.onNext("gimme red");
			sub.onNext(ro.length() + "");
			sub.onCompleted();
		});
		Observable<String> fu5 = Observable.<String>create(sub -> {
			sub.onNext("gave red");
			sub.onCompleted();
		});
		Observable<String> fu6 = Observable.<String>create(sub -> {
			sub.onNext("gimme black");
			sub.onNext("gimme black again");
			sub.onCompleted();
		});
		// fu4 = fu4.onErrorResumeNext(fu5); // works on exceptions except when
		// caused by .single()
		fu4 = fu4.onErrorReturn(new Func1<Throwable, String>() {

			@Override
			public String call(Throwable t) {
				System.out.println("ffrfr");
				return "error came";
			}
		});
		Observable.zip(fu6, fu4, (fuR6, fuR4) -> {
			return fuR4 + " and " + fuR6;
		}).subscribe(s -> System.out.println("received on zip with resume: " + s),
				exception -> System.out.println("received exception on zip with resume: " + exception.getMessage()));
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void testRxColdCache() {
		try {
			Observable<String> fu1 = Observable.<String>create(sub -> {
				for (int i = 0; i < 10; i++) {
					sub.onNext(i + getIt(true));
				}
				sub.onCompleted();
			}).cache();
			// .cache makes it a cold observable, and it plays all values for
			// any new subscription from cache

			fu1.subscribe(s -> System.out.println("received on sub 1: " + s));
			Thread.sleep(20000);
			fu1.subscribe(s -> System.out.println("received on sub 2: " + s));
			Thread.sleep(50000);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("static-access")
	private static void testRxHot() {
		try {
			ConnectableObservable<Long> connectableObservable = Observable.interval(100, TimeUnit.MILLISECONDS)
					.replay();
			// .replay or .publish makes it a hot (connectable) observable,
			// replay will pick up the already emitted values from cache and
			// play them again,
			// while publish omits the already emitted values
			connectableObservable.connect();

			connectableObservable.subscribe(s -> {
				try {
					Thread.currentThread().sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.println("received: " + s);
			});
			long startTs = System.currentTimeMillis();

			connectableObservable.subscribe(s -> System.out.println("received on sub 1: " + s));
			System.out.println(System.currentTimeMillis() - startTs);

			Thread.sleep(20000);
			connectableObservable.subscribe(s -> System.out.println("received on sub 2: " + s));
			Thread.sleep(50000);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String getIt(boolean isCold) {
		String resp = "";
		TestIntController testIntController = new TestIntController();
		try {
			resp = isCold ? testIntController.getTestColdResponseStr() : testIntController.getTestHotResponseStr();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return resp;
	}

	private static void testHttpObservable() {
	}
}

/*
 * class DumOb<T> extends Observable<T> {
 * 
 * protected DumOb(rx.Observable.OnSubscribe<T> f) { super(f); }
 * 
 * @Override public void onCompleted() { System.out.println("completed thead");
 * super.onCom }
 * 
 * }
 */
class SomeRanClass {

}