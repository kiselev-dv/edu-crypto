package me.dkiselev.edu.cesar;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;

public class Vizener {
	
	private char[] alpahbeth;
	private StringSpinner password;
	private boolean preserveCase;

	public Vizener(char[] alpahbeth, String password, boolean preserveCase) {
		this.alpahbeth = alpahbeth;
		this.password = new StringSpinner(password);
		
		this.preserveCase = preserveCase;
	}

	private void encrypt(BufferedReader reader, Writer writer) throws IOException {
		String line = reader.readLine();
		while(line != null) {
			writer.write(encriptString(line.toCharArray()));
			writer.write("\n");
			line = reader.readLine();
		}
	}
	
	private char[] encriptString(char[] string) {
		char[] result = new char[string.length];
		int i = 0;
		for(char c : string) {
			boolean upercase = Character.isUpperCase(c);
			char lower = c;
			
			if(preserveCase && upercase) {
				lower = Character.toLowerCase(c);
			}
			
			// Symbol from alphabeth
			if(ArrayUtils.indexOf(this.alpahbeth, lower) >= 0) {
				
			}
			// Out 
			else {
				result[i++] = Cesar.transformChar(0, lower, preserveCase, alpahbeth);
			}
			
		}
		return result;
	}

	private void decrypt(BufferedReader reader, Writer writer) {
		// TODO Auto-generated method stub
		
	}

	public static void main(String[] args) {
		Options options = getOptions();
		CommandLineParser parser = new DefaultParser();
		
		try {
			
			CommandLine cmd = parser.parse( options, args);
			
			String alphabetString = Cesar.parseAlphabeth(cmd);
			BufferedReader reader = Cesar.parseReader(cmd);
			Writer writer = Cesar.parseWriter(cmd);
			
			String password = cmd.getOptionValue("p");
			if(password == null) {
				System.err.println("Password not provided");
				System.exit(1);
			}
			
			Vizener instance = new Vizener(alphabetString.toCharArray(), password, cmd.hasOption("c"));
			
			if(cmd.hasOption("d") || cmd.hasOption("b")) {
				instance.decrypt(reader, writer);
			}
			else {
				instance.encrypt(reader, writer);
			}
			
			reader.close();
			writer.close();
			
		} catch (ParseException exp) {
			System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
			
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "cesar", options );
		}
		catch (FileNotFoundException fnf) {
			System.err.println( fnf.getMessage() );
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	private static Options getOptions() {
		
		Options options = new Options();
		
		options.addOption("a", "alphabet", true, "String with alphabet to use");
		options.addOption("d", "decrypt", false, "Decrypt");
		options.addOption("b", "break", false, "Break encrypted text");
		options.addOption("c", "preserve-case", false, "Preserve casing");
		options.addOption("p", "pass", true, "Password");
		options.addOption("i", "in", true, "Source");
		options.addOption("o", "out", true, "Output");
		
		return options;
	}
}
