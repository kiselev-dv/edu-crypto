package me.dkiselev.edu.cesar;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;

public class FrequinceAnalyzer {
	
	private String alphabeth;

	private double[] refFerq;

	private String inputString;
	
	private static final String RU_ALPHABETH = "абвгдежзийклмнопрстуфхцчшщъыьэюя";
	private static final String EN_ALPHABETH = "etaoinshrdlcumwfgypbvkjxqz";
	
	private static final double[] ru_freq = new double[]{
		0.07998, 0.01592, 0.04533, 0.01687, 0.02977, 0.08483, 0.0094, 0.01641, 0.07367, 
		0.01208, 0.03486, 0.04343, 0.03203, 0.067, 0.10983, 0.02804, 0.04746, 0.05473, 
		0.06318, 0.02615, 0.00267, 0.00966, 0.00486, 0.0145, 0.00718, 0.00361, 0.00037, 
		0.01898, 0.01735, 0.00331, 0.00639, 0.02001};
	
	private static final double[] en_freq = new double[]{ 0.12702, 0.09056, 0.08167, 0.07507, 0.06966, 
								0.06749, 0.06327, 0.06094, 
								0.05987, 0.04253, 0.04025, 0.02782, 0.02758, 0.02406, 0.02361, 0.02228, 0.02015, 0.01974, 
								0.01929, 0.01492, 0.00978, 0.00772, 0.00153, 0.00150, 0.00095, 0.00074 };

	private int[] ru_counts;

	private int[] en_counts;
	public FrequinceAnalyzer() {
		ru_counts = new int[RU_ALPHABETH.length()];
		en_counts = new int[EN_ALPHABETH.length()];
	}

	public String getInputString() {
		return inputString;
	}

	public int decrypt(BufferedReader reader) throws IOException {
		
		char[] buffer = new char[1000];
		int red = reader.read(buffer);
		if(red == -1) {
			throw new EOFException();
		}
		
		this.inputString = new String(buffer, 0, red).toLowerCase(); 
		
		for(char c : inputString.toCharArray()) {
			int ruInd = RU_ALPHABETH.indexOf(c);
			if(ruInd >= 0) {
				ru_counts[ruInd] = ru_counts[ruInd] + 1; 
			}
			int enInd = EN_ALPHABETH.indexOf(c);
			if(enInd >= 0) {
				en_counts[enInd] = en_counts[enInd] + 1; 
			}
		}
		
		int summRu = summOverArray(ru_counts);
		int summEn = summOverArray(en_counts);
		
		this.alphabeth = (summRu > summEn) ? "ru" : "en";
		this.refFerq = (summRu > summEn) ? ru_freq : en_freq;

		int norm = (summRu > summEn) ? summRu : summEn;
		int[] counts = (summRu > summEn) ? ru_counts : en_counts;
		
		double[] textFreq = normalize(counts, norm);
//		for(double d : textFreq) {
//			System.err.println(
//					String.format("%.5f ", d) + drawBar((int)(d * 1000)));
//		}
		
		int mostProbable = 0;
		double minDistance = Double.MAX_VALUE; 
		double[] correlations = countCorrelations(textFreq, refFerq);
		for(int i = 0; i < correlations.length; i++) {
			if(correlations[i] < minDistance) {
				mostProbable = i;
				minDistance = correlations[i];
			}
		}
		
		return mostProbable;
	}

	private String drawBar(int i) {
		StringBuilder sb = new StringBuilder();
		while (i-- > 0) {
			sb.append("*");
		}
		return sb.toString();
	}

	private double[] countCorrelations(double[] textFreq, double[] refFerq) {
		
		double[] result = new double[textFreq.length];
		
		for(int i = 0; i < textFreq.length; i++) {
			result[i] = countCorrelation(rotate(textFreq, i), refFerq);
		}
		
		return result;
	}

	private double[] rotate(double[] textFreq, int offset) {
		double[] result = new double[textFreq.length];
		for(int i = 0; i < textFreq.length; i++) {
			result[i] = textFreq[(i + offset) % textFreq.length];
		}
		return result;
	}

	private double countCorrelation(double[] textFreq, double[] refFerq) {
		
		double result = 0;
		
		for(int i = 0; i < textFreq.length; i++) {
			result += Math.abs(textFreq[i] - refFerq[i]); 
		}
		
		return result;
	}

	private double[] normalize(int[] counts, int norm) {
		double[] result = new double[counts.length];
		
		for(int i = 0; i < counts.length; i++) {
			result[i] = (double)counts[i] / norm;
		}
		
		return result;
	}

	private int summOverArray(int[] array) {
		int summ = 0;
		for(int i : array) {
			summ += i;
		}
		return summ;
	}

	public String getAlphabeth() {
		return this.alphabeth;
	}

}
