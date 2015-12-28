package me.dkiselev.edu.util;

import me.dkiselev.edu.cesar.Cesar;
import me.dkiselev.edu.vizener.Vizener;

import org.apache.commons.lang3.ArrayUtils;

public class Encription {
	
	public static void main(String[] args) {
		
		if(args.length < 1) {
			System.err.println("Too few arguments.");
			System.err.println("Specify algorithm as first argument");
			return;
		}
		
		String alg = args[0];
		if("cesar".equals(alg)) {
			Cesar.main(ArrayUtils.subarray(args, 1, args.length));
		}
		else if("vizener".equals(alg)) {
			Vizener.main(ArrayUtils.subarray(args, 1, args.length));
		}
		else {
			System.err.println("Unknown algorithm: " + alg);
		}
		
	}

}
