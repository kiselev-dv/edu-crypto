package me.dkiselev.edu.cesar;

import org.apache.commons.lang3.StringUtils;

public class StringSpinner {
	
	private String string;
	private int pointer;
	
	public StringSpinner(String string) {
		this.string = string.toLowerCase();
	}
	
	public char next() {
		return string.charAt((pointer++ ) % this.string.length());
	}

}
