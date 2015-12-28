package me.dkiselev.edu.cesar;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;

import me.dkiselev.edu.util.FormattedWriter;
import me.dkiselev.edu.util.SanitizeBufferedReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Cesar {

	public static final String RU_ALPHABETH = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
	public static final String EN_ALPHABETH = "abcdefghijklmnopqrstuvwxyz";
	
	
	public static void main(String[] args) {
		
		Options options = getOptions();
		
		CommandLineParser parser = new DefaultParser();
		try {
			
			
			if(args.length == 0) {
				System.err.println("Encoding from stdin to stdout. Use Ctrl+C to brake.");
			}
			
			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "encription.jar cesar", options );
				return;
			}
			
			String alphabetString = parseAlphabeth(cmd);

			BufferedReader reader = parseReader(cmd);
			boolean sanitize = !cmd.hasOption("S");
			if(sanitize) {
				reader = new SanitizeBufferedReader(reader, alphabetString.toCharArray());
			}

			if(cmd.hasOption("b")) {
				doBrake(cmd, reader, sanitize);
				return;
			}
			
			int shift = Integer.parseInt(cmd.getOptionValue("n", "0"));
			
			boolean preserveCasing = cmd.hasOption("c");
			Cesar instance = new Cesar(alphabetString.toCharArray(), shift, preserveCasing);
			
			Writer writer = parseWriter(cmd);
			if(sanitize) {
				writer = new FormattedWriter(writer);
			}
			
			if(cmd.hasOption("d")) {
				instance.decrypt(reader, writer);
			}
			else {
				instance.encrypt(reader, writer);
			}
			
			if(writer instanceof FormattedWriter) {
				((FormattedWriter) writer).newLine();
			}
			
			writer.flush();
			
			reader.close();
			writer.close();
			
		} catch (ParseException exp) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "encription.jar cesar", options );
		}
		catch (FileNotFoundException fnf) {
			System.err.println( fnf.getMessage() );
		}
		catch (NumberFormatException nfe) {
			System.err.println( "Пароль должен быть целым числом." );
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	/**
	 * Потоково взламываем и декодируем кусками по 1000 символов
	 * */
	private static void doBrake(CommandLine cmd, BufferedReader reader,
			boolean sanitize) throws FileNotFoundException, IOException {
		
		Writer writer = parseWriter(cmd);
		if(sanitize) {
			writer = new FormattedWriter(writer);
		}
		
		try {
			FrequinceAnalyzer analyzer = new FrequinceAnalyzer();
			
			int lastShift = -1;
			while(true) {
				
				int decryptedShift = analyzer.decrypt(reader); 
				
				if(lastShift != decryptedShift) {
					System.err.println("Alphabeth is: " + analyzer.getAlphabeth());
					System.err.println("Most possible shift is: " + decryptedShift);
					lastShift = decryptedShift;
				}
				
				String matchedAlphabeth = null;
				if("ru".equals(analyzer.getAlphabeth())) {
					matchedAlphabeth = RU_ALPHABETH;
				}
				else if("en".equals(analyzer.getAlphabeth())){
					matchedAlphabeth = EN_ALPHABETH;
				}
				
				String analyzedFragment = analyzer.getInputString();
				BufferedReader buffer = new BufferedReader(new StringReader(analyzedFragment));
				if(sanitize) {
					buffer = new SanitizeBufferedReader(buffer, matchedAlphabeth.toCharArray());
				}
				
				boolean preserveCasing = cmd.hasOption("c");
				Cesar instance = new Cesar(matchedAlphabeth.toCharArray(), decryptedShift, preserveCasing);
				
				instance.decrypt(buffer, writer);
				buffer.close();
			}
		}
		catch (EOFException e) {
			
		}
		
		if(writer instanceof FormattedWriter) {
			((FormattedWriter) writer).newLine();
		}
		writer.close();
		reader.close();
	}

	private void encrypt(BufferedReader reader, Writer writer) throws IOException {
		String line = reader.readLine();
		
		while(line != null) {
			
			if(StringUtils.isNotBlank(line)) {
				char[] shiftString = shiftString(line.toCharArray(), shift, preserveCase, alphabeth);
				
				writer.write(shiftString);
				
				// Don't print new lines for sanitized texts
				writer.write("\n");
			}
			
			line = reader.readLine();
		}
	}

	private void decrypt(BufferedReader reader, Writer writer) throws IOException {
		String line = reader.readLine();
		while(line != null) {
			
			writer.write(shiftString(line.toCharArray(), -shift, preserveCase, alphabeth));
			writer.write("\n");
			line = reader.readLine();
		}
	}

	public static char[] shiftString(char[] string, int shift, 
			boolean preserveCase, char[] alphabeth) {
		
		char[] result = new char[string.length];
		int i = 0;
		for(char c : string) {
			result[i++] = transformChar(shift, c, preserveCase, alphabeth);
		}
		
		return result;
	}

	public static char transformChar(int shift, char c, boolean preserveCase, char[] alphabeth) {
		
		boolean upercase = Character.isUpperCase(c); 
		c = Character.toLowerCase(c);
		
		int indexIn = ArrayUtils.indexOf(alphabeth, c);
		
		// Outside alphabeth
		if(indexIn < 0) {
			// Restore case before skip nonalphabetical char
			if(preserveCase && upercase) {
				c = Character.toUpperCase(c);
			}
			// Don't shift characters which are out of alphabeth.
			return c;
		}
		//Inside alphabeth
		else {
			int indexOut = getOutIndexNorm(shift, indexIn, alphabeth);
			
			char outChar = alphabeth[indexOut];
			
			// Restore casing
			if(preserveCase && upercase) {
				outChar = Character.toUpperCase(outChar);
			}
			return outChar;
		}
	}

	/* 
	 * Normalized out index inside alphabeth array
	 */
	private static int getOutIndexNorm(int shift, int indexIn, char[] alphabeth) {
		int indexOut = (indexIn + shift) % alphabeth.length;
		if(indexOut < 0) {
			indexOut = alphabeth.length - Math.abs(indexOut);
		}
		return indexOut;
	}

	public static BufferedReader parseReader(CommandLine cmd)
			throws FileNotFoundException {
		BufferedReader reader;
		String inpPath = cmd.getOptionValue("i", "-");
		if("-".equals(inpPath)) {
			reader = new BufferedReader(new InputStreamReader(System.in));
		}
		else {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(inpPath))));
		}
		return reader;
	}

	public static Writer parseWriter(CommandLine cmd)
			throws FileNotFoundException {
		Writer writer;
		String outPath = cmd.getOptionValue("o", "-");
		if("-".equals(outPath)) {
			writer = new OutputStreamWriter(System.out);
		}
		else {
			writer = new OutputStreamWriter(
					new FileOutputStream(new File(outPath)));
		}
		return writer;
	}

	public static String parseAlphabeth(CommandLine cmd) {
		String alphabetString = null;
		String alphabetOption = cmd.getOptionValue("a", "ru");
		if("ru".equals(alphabetOption)) {
			alphabetString = RU_ALPHABETH;
		}
		else if("en".equals(alphabetOption)) {
			alphabetString = EN_ALPHABETH;
		}
		else {
			alphabetString = alphabetOption;
		}
		return alphabetString;
	}

	private char[] alphabeth;
	private int shift;
	private boolean preserveCase;
	
	public Cesar(char[] alphabeth, int shift, boolean preserveCase) {
		this.alphabeth = alphabeth;
		this.shift = shift;
		this.preserveCase = preserveCase;
	}

	private static Options getOptions() {
		
		Options options = new Options();
		
		options.addOption("h", "help", false, "Display help.");
		
		options.addOption("a", "alphabet", true, "String with alphabet to use.");
		options.addOption("d", "decrypt", false, "Decrypt, default action is encrypt.");
		options.addOption("b", "break", false, "Break encrypted text.");
		options.addOption("n", "shift", true, "Cesar key, alphabetic shift.");
		
		options.addOption("i", "in", true, "Source");
		options.addOption("o", "out", true, "Output");

		options.addOption("c", "preserve-case", false, "Preserve casing for not sanitized input");
		options.addOption("S", "no-sanitize", false, "Do not sanitize input");
		
		return options;
	}
}
