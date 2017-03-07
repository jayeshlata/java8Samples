package com.mmt.gabbar.rx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

public class RxLearning {

	class InnerClass{
		
	}
	
	public static void testRx(){
		
		
		String[] s = new String[] {"a","b","c"};
		/*Observable<String> os = Observable.from(s);
		Subscription oss = os.subscribe(a -> {
			System.out.println(a);
		});
		System.out.println(oss.isUnsubscribed());*/
	}
	
	private static Observable<String> getColdObservable(){
		return Observable.<String>create(sub -> {
			sub.onNext(getNextCold());
			sub.onCompleted();
		});
	}
	
	private static String getNextCold() {
		return "Cold";
	}
	
	private static void testColdObservable(){
		Observable<String> t1 = getColdObservable();
		t1.subscribe(s -> System.out.println(s));
	}
	
	private static void testColdIntObservable() {
		try {
			/*Observable<Integer> obs = Observable.range(1, 10);
			obs.subscribe(s -> System.out.println("First: " + s));
			obs.subscribe(s -> System.out.println("Second: " + s));*/

			Observable<Long> obs1 = Observable.interval(200, TimeUnit.MILLISECONDS);
			System.out.println("First Subscription");
			obs1.subscribe(s -> System.out.println("First1: " + s));
			Thread.sleep(700);
			System.out.println("Second Subscription");
			obs1.subscribe(s -> System.out.println("Second1: " + s));
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testCacheObservable() {
		try {
			Observable<Long> obs1 = Observable.interval(200, TimeUnit.MILLISECONDS).cache();
			System.out.println("First Subscription");
			obs1.subscribe(s -> System.out.println("First1: " + s));
			Thread.sleep(2000);
			System.out.println("Second Subscription");
			obs1.subscribe(s -> System.out.println("Second1: " + s));
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testPublishObservable() {
		try {
			ConnectableObservable<Long> obs1 = Observable.interval(200, TimeUnit.MILLISECONDS).publish();
			obs1.connect();
			System.out.println("First Subscription");
			obs1.subscribe(s -> System.out.println("First1: " + s));
			Thread.sleep(2000);
			System.out.println("Second Subscription");
			obs1.subscribe(s -> System.out.println("Second1: " + s));
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testReplayObservable() {
		try {
			ConnectableObservable<Long> obs1 = Observable.interval(200, TimeUnit.MILLISECONDS).replay();
			obs1.connect();
			System.out.println("First Subscription");
			obs1.subscribe(s -> System.out.println("First1: " + s));
			Thread.sleep(2000);
			System.out.println("Second Subscription");
			obs1.subscribe(s -> System.out.println("Second1: " + s));
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testZipObservable() {
		System.out.println("Creating first Observable");
		Observable<Long> obs1 = Observable.interval(200, TimeUnit.MILLISECONDS).take(10);
		System.out.println("Creating second Observable");
		Observable<String> obs2 = Observable.create(sub -> {
			for (int i = 0; i < 10; i++) {
				if(i == 4){
					String s = null;
					sub.onNext(s.length() + "");
				}else
					sub.onNext(getNextCold() + i);
			}
			sub.onCompleted();
		});
		Observable<String> obs3 = Observable.create(sub -> {
			for (int i = 0; i < 10; i++) {
				sub.onNext(getNextCold() + " obs3 " + i);
			}
			sub.onCompleted();
		});
		//obs2 = obs2.onErrorResumeNext(obs3);
		obs2 = obs2.onErrorReturn(new Func1<Throwable, String>() {
			public String call(Throwable t) {
				System.out.println("On Error Return");
				return t.getMessage();
			};
		});
		System.out.println("Creating zipped Observable");
		Observable<Map<String, Long>> zipped = Observable.zip(obs1, obs2, (obsr1, obsr2) -> {
			Map<String, Long> map = new HashMap<>();
			map.put(obsr2, obsr1);
			return map;
		});
		
		zipped.subscribe(s -> System.out.println("zipped: " + s), exception -> System.out.println("Exception"));
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void testMergeObservable() {
		System.out.println("Creating first Observable");
		//Observable<Long> obs1 = Observable.interval(400, TimeUnit.MILLISECONDS).take(10);
		Observable<String> obs1 = Observable.<String>create(sub -> {
			for (int i = 0; i < 10; i++) {
				sub.onNext(getNextCold() + " First " + i);
				try {
					Thread.currentThread().sleep(400);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sub.onCompleted();
		}).subscribeOn(Schedulers.computation());
		System.out.println("Creating second Observable");
		Observable<String> obs2 = Observable.<String>create(sub -> {
			for (int i = 0; i < 10; i++) {
				sub.onNext(getNextCold() + i);
				try {
					Thread.currentThread().sleep(500);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			sub.onCompleted();
		}).subscribeOn(Schedulers.computation());
		System.out.println("Creating Merge Observable");
		Observable merged = Observable.merge(obs1, obs2);
		
		merged.subscribe(s -> System.out.println(s));
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void testFlatMap(){
		List<String> strings =new ArrayList<>();
		strings.add("Hello");
		strings.add("My");
		strings.add("Name");
		
		List<String> strings1 =new ArrayList<>();
		strings1.add("Is");
		strings1.add("Mayank");
		strings1.add("Jandiyal");
		
		
	}
	
	public static void main(String[] args) {
		testRx();
		//testColdObservable();
		//testColdIntObservable();
		//testCacheObservable();
		//testPublishObservable();
		//testReplayObservable();
//		testZipObservable();
		testMergeObservable();
		
	}
}
