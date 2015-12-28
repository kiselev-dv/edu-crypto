package me.dkiselev.edu.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SanitizeBufferedReader extends BufferedReader {

	private char[] alphabeth;
	
	public SanitizeBufferedReader(Reader in, char[] sanitizeAlpahbeth) {
		super(in);
		this.alphabeth = sanitizeAlpahbeth;
	}
	
	@Override
	public String readLine() throws IOException {
		String line = super.readLine();
		if(line == null) {
			return null;
		}
		return sanitizeLine(line, alphabeth);
	}
	
	public static String sanitizeLine(String line, char[] alphabeth) {
		String sanitized = StringUtils.lowerCase(line);
		sanitized = StringUtils.replaceChars(sanitized, "ё", "е");
		
		ArrayList<Character> onlyAlphabeth = new ArrayList<Character>(); 
		for(char c : sanitized.toCharArray()) {
			if(ArrayUtils.indexOf(alphabeth, c) >= 0) {
				onlyAlphabeth.add(c);
			}
		}
		
		Character[] filteredChars = onlyAlphabeth.toArray(new Character[]{});
		sanitized = new String(ArrayUtils.toPrimitive(filteredChars));
		
 		return sanitized;
	}

}
