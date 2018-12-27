package com.benja;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Benja {
	
	long requests; //number of requests in total
	long networkFailed; //number of requests failed
	long responses;
	double responseTime;
	final int NLINK = 1000;	
	final int timeout = 10000; //timeout for the connection time
	final String websiteURL = "http://192.168.1.10/wikimirror/index.php/";
	double start;
	double interval;
	static double lambda=0.3; //this is the arrival rate
	String filename;
	ArrayList<Double> rt = new ArrayList<Double>(); //Arraylist for response time
	ArrayList<Double> st = new ArrayList<Double>(); //arraylist for service time
	
	public Benja(){
		requests = 0L;
		responses = 0L;
		networkFailed = 0L;
	}
	
	public void runTest(double lambda, ArrayList<String> urls){
		final int NTHREAD =15; //number of thread to create	
		final long minute = 60 * 1000L;
		long requestsMinute = 0L;
		final double minSec = 60.0; //length of one minute in seconds
		
		System.out.println("[+] Test started");
		System.out.println("[+] Lambda: " + lambda);
		System.out.println("[+] Preparing " + NTHREAD + " client to spawn...");
		System.out.println("[+] This test will take approximately " + NTHREAD/lambda + " sec");
		
		//The method Hook is executed always before the program exit
		Runtime.getRuntime().addShutdownHook(new Hook());
		
		Thread mt[] = new Request[NTHREAD];
		
		//TODO: determine service time
		
		start = System.currentTimeMillis();
		long nextCall = System.currentTimeMillis() + minute;
		
		for(int i=0; i<NTHREAD; i++){
			long now = System.currentTimeMillis();
			
			//this condition is satisfied every minute
			if(now>nextCall){
				System.out.println("[+] Number of requests per minute: " + requestsMinute);
				System.out.printf("[+] Arr. rate minute %.3f\n", requestsMinute/minSec);
				nextCall = now + minute;
				requestsMinute = 0L;
			}
			
			//generate a random number for the link in the array
			Random rn = new Random();
			int n = NLINK - 0 + 1;
			int index = rn.nextInt(NLINK) + 0;
			//get a random link from the list
			String url = urls.get(index);

			
			mt[i] = new Request(websiteURL+url, timeout);
			mt[i].start();
	
			try {
				//System.out.println((long)exponential(1)*1000);
				double exp = exponential(lambda)*1000;
				long sleep = (long)Math.floor(exp*100)/100;
				//System.out.println("Sleeping for: " + sleep + " ms"); 
				mt[i].sleep(sleep); //thinking time
				requestsMinute++;
				requests++;
			} catch(Exception e){}
		}
		
		interval = (System.currentTimeMillis() - start) / 1000.0 ;
		System.out.println("[+] Test finished");
		
		for(Thread t: mt){
			try {
				t.join();
				responseTime = ((Request) t).getTime();
				// the name of the file is the actual date
				Format f = new SimpleDateFormat("ddMMyyyyHHmmss");
				filename = f.format(new Date());
				CSVWriter csv = new CSVWriter(filename);
				//debug
				csv.write(responseTime);
				System.out.println("[+] Response time: " + responseTime + "ms for " + ((Request) t).getUrl() + " - " + ((Request) t).getStatusCode());
				//System.out.println("Status code: " + ((Request) t).getStatusCode() + " for " + ((Request) t).getUrl());
				if(responseTime<0 || ((Request) t).getStatusCode()!=200){
					networkFailed++;
				} else {
					responses++;
					rt.add(responseTime);
				}
				//System.out.println(responseTime);
			} catch(Exception ex){}
		}
		
		//System.out.println("Random " + exponential(1));
	}
	
	public static void main(String[] args) throws IOException {
		String file = "C:\\Users\\Marco\\Documents\\safe.csv";
		ArrayList<String> urls = new ArrayList<String>();
		Benja bj = new Benja();		
		urls = loadUrls(file);		
		bj.runTest(lambda, urls);
	}
	
	// this function generate a random number using the esponential distribution
	public static double exponential(double rate){
		double rand = Math.random();
		//return -Math.log(1.0-rand)/rate;
		return -Math.log(rand)/rate;
	}
	
	//Loads the urls from the specified file
	public static ArrayList<String> loadUrls(String filename) throws IOException {
		ArrayList<String> urls = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(filename));
		int nline = 0;
		String line;
		
		while ((line = br.readLine()) != null) {
			urls.add(line);
			nline++;
			//read the first 10000 link
			if(nline>1000){
				break;
			}
		}
		
		br.close();
		return urls;
	}
	
	//print the statistics and do other stuffs
	protected void terminate(){
		double mean = 0;
		double sum = 0;
		double variance = 0;
		//long success = requests-networkFailed;
		
		System.out.println("=============================================");
		System.out.println("[+] Elapsed Time: " + interval + " sec.");
		System.out.println("[+] Num. of requests in total: " + requests);
		System.out.println("[+] Num. of successful requests: " + responses);
		System.out.println("[+] Num. of requests failed: " + networkFailed);
		System.out.printf("[+] Throughput: %.3f jobs/sec\n", responses/interval);
		System.out.println("[+] Arrival rate: " + Math.round((requests/interval)) + " req/sec");
		System.out.printf("[+] Inter arr. time %.3f sec\n", 1/(requests/interval));
		for(double time : rt){
			sum += time;
		}
		
		//System.out.println();
		double m = (sum/rt.size())/1000; //mean response time
		System.out.printf("[+] Mean response time: %.3f sec\n", m);
		double t = responses/interval; //throughput
		double n = t*m; //number of users
		System.out.printf("[+] Num. of users in the system (N): %.3f\n", n);
	
		//compute the variance
		for(double time : rt){
			variance  += (time-mean)*(time-mean); 
		}
		
		System.out.printf("[+] Variance response time: %.2f\n", variance/(rt.size()-1));
		System.out.println("[+] Data saved to " + filename);
		 
	}
	
	protected class Hook extends Thread {
		public void run(){
			terminate();
		}
	}

}
