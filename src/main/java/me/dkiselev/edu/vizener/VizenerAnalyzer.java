package me.dkiselev.edu.vizener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.dkiselev.edu.cesar.Cesar;
import me.dkiselev.edu.cesar.FrequinceAnalyzer;
import me.dkiselev.edu.dic.RusDictionary;
import me.dkiselev.edu.util.IntCount;
import me.dkiselev.edu.util.SanitizeBufferedReader;
import me.dkiselev.edu.util.VizDecryptCortage;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class VizenerAnalyzer {
	
	private int maxAnalyzeTryes = 6;
	
	public static final char[] RU_ALPHABETH = Cesar.RU_ALPHABETH.toCharArray();
	private String inputString;
	private String originalMessage;

	public String tryBreak(BufferedReader reader) throws IOException {
		this.inputString = SanitizeBufferedReader.sanitizeLine(IOUtils.toString(reader), RU_ALPHABETH);
		
		List<IntCount> matchesByOffset = countMatchesByOffset();
		
		Collections.sort(matchesByOffset, Collections.reverseOrder(new Comparator<IntCount>() {

			public int compare(IntCount o1, IntCount o2) {
				return Integer.compare(o1.count, o2.count);
			}
			
		}));
		
		int[] lengths = new int[3];
		System.err.println("Top three passwords lengths (in order of probability): ");
		for(int j = 0; j < 3 && j < matchesByOffset.size(); j++) {
			IntCount c = matchesByOffset.get(j);
			lengths[j] = c.offset;
			System.err.println("Lenght: " + c.offset + " (Count: " + c.count + ")");
		}
		
		int gcd = gcdThing(matchesByOffset.get(0).offset, matchesByOffset.get(1).offset);
		if(gcd != 1) {
			for(int i = 0; i < 3; i++) {
				int gcdT = gcdThing(gcd, matchesByOffset.get(2 + i).offset);
				if(gcdT != 1) {
					gcd = gcdT; 
				}
			}
		}
		
		System.err.println("GCB: " + gcd);
		
		List<VizDecryptCortage> results = new ArrayList<VizDecryptCortage>();
		for(int i = 1; i < maxAnalyzeTryes && gcd * i < 32; i++) {
			StringBuilder password = decryptWithPasswordLength(gcd * i);
			System.err.println("Password: " + password.toString());
			
			System.err.println("Analyze restored mesasge");
			
			VizDecryptCortage res = new VizDecryptCortage();
			res.text = originalMessage;
			res.dictMatches= RusDictionary.countMatches(originalMessage);
			res.passwd = password.toString();
			results.add(res);
		}
		
		Collections.sort(results, Collections.reverseOrder(new Comparator<VizDecryptCortage>() {

			public int compare(VizDecryptCortage o1, VizDecryptCortage o2) {
				int dicMatches = Integer.compare(o1.dictMatches, o2.dictMatches);
				if(dicMatches == 0) {
					Integer.compare(o2.passwd.length(), o1.passwd.length());
				}
				return dicMatches;
			}
			
		}));
		
		originalMessage = results.get(0).text;
		
		return results.get(0).passwd;
	}

	private StringBuilder decryptWithPasswordLength(int gcd) throws IOException {
		List<StringBuilder> submesssages = getSubmessages(inputString, gcd); 
		
		StringBuilder password = new StringBuilder();
		for(StringBuilder submessage : submesssages) {
			FrequinceAnalyzer fa = new FrequinceAnalyzer();
			int offset = fa.decrypt(new BufferedReader(new StringReader(submessage.toString())));
			password.append(RU_ALPHABETH[offset]);
		}
		
		this.originalMessage = restoreMessage(submesssages, password.toString());
		return password;
	}

	private String restoreMessage(List<StringBuilder> submesssages,
			String password) {
		
		List<char[]> restoredSubmessages = new ArrayList<char[]>();
		
		int fullLength = 0;

		char[] passwdArray = password.toCharArray();
		for(int i = 0; i < passwdArray.length; i++) {
		    int offset = ArrayUtils.indexOf(RU_ALPHABETH, passwdArray[i]);
		    String submessage = submesssages.get(i).toString();
		    
		    char[] restoredSubmessage = 
		    		Cesar.shiftString(submessage.toCharArray(), -offset, true, RU_ALPHABETH);
		    
		    restoredSubmessages.add(restoredSubmessage);
		    fullLength += restoredSubmessage.length;
		} 
		
		StringBuilder result = new StringBuilder();
		
		for(int i = 0; i < fullLength; i++) {
			char[] subMSG = restoredSubmessages.get(i % passwdArray.length);

			if(i / passwdArray.length < subMSG.length) {
				result.append(subMSG[i / passwdArray.length]);
			}
		}
		
		return result.toString();
	}

	private List<StringBuilder> getSubmessages(String inputString, int offset) {
		List<StringBuilder> result = new ArrayList<StringBuilder>();
		for(int i = 0; i < offset; i++) {
			result.add(new StringBuilder());
		}
		
		for(int i = 0; i < inputString.length(); i++) {
			char charAt = inputString.charAt(i);
			result.get(i % offset).append(charAt);
		}
		
		return result;
	}

	private List<IntCount> countMatchesByOffset() {
		List<IntCount> matchesByOffset = new ArrayList<IntCount>();
		
		for(int i = 1; i <= 32; i++) {
			String rotated = rotate(inputString, i);
			int matches = countMatchedCharacters(inputString, rotated);
			
			IntCount c = new IntCount();
			c.offset = i;
			c.count = matches;
			matchesByOffset.add(c);
		}
		return matchesByOffset;
	}
	
	private int countMatchedCharacters(String in, String rotated) {
		
		int result = 0;
		
		char[] inA = in.toCharArray();
		char[] rotatedA = rotated.toCharArray();
		
		for(int i = 0; i < inA.length && i < rotatedA.length; i++) {
			if( inA[i] == rotatedA[i] ) {
				result ++;
			}
		}
		
		return result;
	}

	private String rotate(String in, int offset) {
		
		char[] result = new char[in.length()];
		char[] input  = in.toCharArray();

		for(int i = 0; i < result.length; i++) {
			result[i] = input[(i + offset) % input.length];
		}
		return new String(result);
	}

	public String getOriginalMessage() {
		return originalMessage;
	}
	
	private static int gcdThing(int a, int b) {
	    BigInteger b1 = BigInteger.valueOf(a);
	    BigInteger b2 = BigInteger.valueOf(b);
	    BigInteger gcd = b1.gcd(b2);
	    return gcd.intValue();
	}

	public void setMaxAnalyzeTryes(int maxAnalyzeTryes) {
		this.maxAnalyzeTryes = maxAnalyzeTryes;
	}
	
}
