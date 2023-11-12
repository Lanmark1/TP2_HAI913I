package main;

import java.io.BufferedReader; 
import java.io.IOException;
import java.io.InputStreamReader;

import util.ProgramAnalyzerHelper;

public class App {

	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("    HAI913I - TP2");
		System.out.println("Please enter a project folder name to be analyzed "
				+ "(must be present in the projectsToParse folder) \nOR just press enter to analyze the dummyProject");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String s = br.readLine();
		ProgramAnalyzerHelper.run(s);
	}
}
