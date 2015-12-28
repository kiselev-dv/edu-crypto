package me.dkiselev.edu.util;

import java.io.IOException;
import java.io.Writer;

public class FormattedWriter extends Writer {

	private Writer w;
	private long writed = 0;

	public FormattedWriter(Writer w) {
		this.w = w;
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		for(int i = 0; i < len; i++) {
			char c = cbuf[i + off];
			if(c == '\n' || c == '\r') {
				continue;
			}
			if(writed > 0) {
				if(writed % 5 == 0) {
					w.append(' ');
				}
				if(writed % 60 == 0) {
					w.append('\n');
				}
			}
			writed++;
			w.append(c);
		}
	}
	
	public void newLine() throws IOException {
		w.append('\n');
	}

	@Override
	public void flush() throws IOException {
		w.flush();
	}

	@Override
	public void close() throws IOException {
		w.close();
	}

}
