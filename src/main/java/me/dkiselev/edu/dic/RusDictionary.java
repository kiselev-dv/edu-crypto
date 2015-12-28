package me.dkiselev.edu.dic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

public class RusDictionary {
	
	private static final List<String> dictionary = new ArrayList<String>();
	
	static {
		InputStream resourceAsStream = RusDictionary.class.getResourceAsStream("/words_utf8.num.csv");
		try {
			LineIterator lineIterator = IOUtils.lineIterator(resourceAsStream, Charset.forName("UTF-8"));
			while(lineIterator.hasNext()) {
				dictionary.add(StringUtils.split(lineIterator.next())[2]);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static int countMatches(String text) {
		final AtomicInteger counter = new AtomicInteger();
		
		List<Runnable> tasks = new ArrayList<Runnable>(4);
		
		List<String> d1 = dictionary.subList(0, dictionary.size() / 4);
		List<String> d2 = dictionary.subList(dictionary.size() / 4 * 1, dictionary.size() / 4 * 2);
		List<String> d3 = dictionary.subList(dictionary.size() / 4 * 2, dictionary.size() / 4 * 3);
		List<String> d4 = dictionary.subList(dictionary.size() / 4 * 3, dictionary.size() - 1);

		tasks.add(new CountTask(d1, text, counter));
		tasks.add(new CountTask(d2, text, counter));
		tasks.add(new CountTask(d3, text, counter));
		tasks.add(new CountTask(d4, text, counter));
		
		ExecutorService es = Executors.newFixedThreadPool(4);
		for(Runnable task : tasks) {
			es.execute(task);
		}
		
		es.shutdown();
		try {
			es.awaitTermination(60, TimeUnit.SECONDS);
		}
		catch (InterruptedException e) {
			es.shutdownNow();
		}
		
		return counter.get();
	}
	
	private static class CountTask implements Runnable {
		
		private List<String> d;
		private String text;
		private AtomicInteger counter;
		private int intCounter = 0;
		
		public CountTask(List<String> d, String text, AtomicInteger counter) {
			this.d = d;
			this.text = text;
			this.counter = counter;
		}
		
		public void run() {
			for(String term : d) {
				intCounter += StringUtils.countMatches(text, term);
			}
			
			counter.addAndGet(intCounter);
		}
	}

}
