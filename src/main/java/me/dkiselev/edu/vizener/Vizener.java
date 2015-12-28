package me.dkiselev.edu.vizener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import me.dkiselev.edu.cesar.Cesar;
import me.dkiselev.edu.util.FormattedWriter;
import me.dkiselev.edu.util.SanitizeBufferedReader;
import me.dkiselev.edu.util.StringSpinner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Vizener {
	
	private char[] alpahbeth;
	private StringSpinner password;
	private boolean preserveCase;
	public static final char[] RU_ALPHABETH = Cesar.RU_ALPHABETH.toCharArray();

	public Vizener(char[] alpahbeth, String password, boolean preserveCase) {
		this.alpahbeth = alpahbeth;
		this.password = new StringSpinner(password);
		
		this.preserveCase = preserveCase;
	}

	private void encrypt(BufferedReader reader, Writer writer) throws IOException {
		String line = reader.readLine();
		while(line != null) {
			writer.write(shiftString(line.toCharArray(), false));
			writer.write("\n");
			line = reader.readLine();
		}
	}
	
	private char[] shiftString(char[] string, boolean decrypt) {
		char[] result = new char[string.length];
		int i = 0;
		for(char c : string) {
			boolean upercase = Character.isUpperCase(c);
			char lower = Character.toLowerCase(c);
			
			// Symbol from alphabeth
			if(ArrayUtils.indexOf(this.alpahbeth, lower) >= 0) {
				char passChar = this.password.next();
				int vizenerShift = ArrayUtils.indexOf(this.alpahbeth, passChar);
				int shift = decrypt? -vizenerShift : vizenerShift;
				result[i++] = Cesar.transformChar(shift, lower, preserveCase, alpahbeth);
			}
			// Out 
			else {
				result[i++] = Cesar.transformChar(0, lower, preserveCase, alpahbeth);
			}
			
		}
		return result;
	}

	private void decrypt(BufferedReader reader, Writer writer) throws IOException {
		String line = reader.readLine();
		while(line != null) {
			writer.write(shiftString(line.toCharArray(), true));
			writer.write("\n");
			line = reader.readLine();
		}
	}


	public static void main(String[] args) {
		Options options = getOptions();
		
		CommandLineParser parser = new DefaultParser();
		
		try {
			
			CommandLine cmd = parser.parse( options, args);
			
			if(cmd.hasOption("h") || args.length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp( "encription.jar vizener", options );
				return;
			}
			
			String alphabetString = Cesar.parseAlphabeth(cmd);

			BufferedReader reader = Cesar.parseReader(cmd);
			boolean sanitize = !cmd.hasOption("S");
			if(sanitize) {
				reader = new SanitizeBufferedReader(reader, alphabetString.toCharArray());
			}
			
			Writer writer = Cesar.parseWriter(cmd);
			if(sanitize) {
				writer = new FormattedWriter(writer);
			}

			if(cmd.hasOption("b")) {
				VizenerAnalyzer analyzer = new VizenerAnalyzer();
				
				analyzer.setMaxAnalyzeTryes(Integer.parseInt(cmd.getOptionValue("m", "6")));
				
				String password = analyzer.tryBreak(reader);
				
				if(password != null) {
					System.err.println("Пароль: " + password);
					writer.write(analyzer.getOriginalMessage());
				}
				else {
					System.err.println("Failed to break encoded text.");
				}
				
				if(writer instanceof FormattedWriter) {
					((FormattedWriter) writer).newLine();
				}
				
				writer.flush();
				writer.close();
				
				return;
			}
			
			String password = parsePassword(cmd);
			if(password == null) {
				return;
			}
			
			Vizener instance = new Vizener(alphabetString.toCharArray(), password, cmd.hasOption("c"));
			
			if(cmd.hasOption("d")) {
				instance.decrypt(reader, writer);
			}
			else {
				instance.encrypt(reader, writer);
			}
			
			reader.close();
			
			if(writer instanceof FormattedWriter) {
				((FormattedWriter) writer).newLine();
			}
			
			writer.close();
			
		} catch (ParseException exp) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "encription.jar vizener", options  );
		}
		catch (FileNotFoundException fnf) {
			System.err.println( fnf.getMessage() );
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private static String parsePassword(CommandLine cmd) {
		
		String password = cmd.getOptionValue("p");
		if(password == null) {
			System.err.println("Пароль не указан.");
			return null;
		}
		
		password = StringUtils.replaceChars(password, 'ё', 'е').toLowerCase();
		String sanitized = sanitizePassword(password);
		
		if(!sanitized.equals(password)) {
			System.err.println("Пароль содержит недопустимые символы. "
					+ "Допустимы только символы русского алфавита от а до я. "
					+ "Символ ё автоматически заменяется на е.");
			return null;
		}
		
		return sanitized;
	}
	
	private static String sanitizePassword(String password) {
		String sanitized = password.toLowerCase();
		sanitized = StringUtils.replaceChars(sanitized, "ё", "е");
		
		ArrayList<Character> onlyAlphabeth = new ArrayList<Character>(); 
		for(char c : sanitized.toCharArray()) {
			if(ArrayUtils.indexOf(RU_ALPHABETH, c) >= 0) {
				onlyAlphabeth.add(c);
			}
		}
		
		Character[] filteredChars = onlyAlphabeth.toArray(new Character[]{});
		sanitized = new String(ArrayUtils.toPrimitive(filteredChars));
		
 		return sanitized;
	}

	private static Options getOptions() {
		
		Options options = new Options();
		
		options.addOption("h", "help", false, "Справка");

		options.addOption("a", "alphabet", true, "Алфавит");
		options.addOption("d", "decrypt", false, "Режим расшифровки.");
		options.addOption("b", "break", false, "Режим взлома.");
		options.addOption("c", "preserve-case", false, "Preserve casing");
		options.addOption("p", "pass", true, "Пароль");
		options.addOption("i", "in", true, "Источник (используйте - для стандартного ввода)");
		options.addOption("o", "out", true, "Назначение (используйте - для стандартного ввода)");
		options.addOption("m", "max-analyze-tryes", true, "Ограничение числа попыток для подстановки длин ключа.");
		
		return options;
	}
}
