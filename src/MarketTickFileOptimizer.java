import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javafx.beans.property.IntegerProperty;

public class MarketTickFileOptimizer {
	
	private static void work(File file, String outSignature, boolean autoSignature) {
		File outFile = new File(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf('.')) + "_Optimized.csv");
		if (outFile.exists()) {
			System.out.println("file already exists. fuhgeddaboudit");
			return;
		}
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				FileOutputStream fos = new FileOutputStream(outFile, true)) {			
			String in; 
			String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
			int size = -1;			
			if (autoSignature) {					
				outSignature = name;
				outSignature += " 0.25 2\n";
			} else if (outSignature == null) {
				in = br.readLine() + "\n";
				size = Integer.parseInt(in.substring(0, in.indexOf(' ')));			
				outSignature = in.substring(in.indexOf(' ') + 1);
			} else {
				outSignature += '\n';
			}
			ArrayList<String> ticks = new ArrayList<String>();
			in = br.readLine();
			if (Signature.validFull(in) || Signature.validPartial(in)) {
				in = br.readLine();
			}
			int progress = 0;
			boolean changed = false;
			int last = -1;
			int last2 = 0;
			int newSize = 0;
			if (size == -1) {
				System.out.println("working on " + name + ".csv...");
			}
			while (true) {
				if (in == null || in.equals("")) {
					break;
				}
				StringTokenizer tokens = new StringTokenizer(in, ";");
				String date = tokens.nextToken();
				tokens.nextToken();
				if (tokens.nextToken().equals("2")) {
					ticks.add(date + ";" + tokens.nextToken());
					newSize++;
				}	
				in = br.readLine();
				progress++;
				if (size != -1) {					
					int percent = (int)((double)progress/size*100);
					if (last < percent) {
						changed = true;
						last = percent;
					}					
					if (changed) {
						System.out.println(percent + "%");
						changed = false;
					}			
				} else {
					if (last2 + 10000000 < progress) {
						changed = true;
						last2 = progress;
					}					
					if (changed) {
						System.out.println("still working on " + name + ".csv...");
						changed = false;
					}	
				}
			}			
			System.out.println("writing " + name + "_Optimized.csv...");
			fos.write((newSize + " " + outSignature).getBytes());
			for (String s : ticks) {
				fos.write((s + '\n').getBytes());
			}
			System.out.println("done optimizing " + name + ".csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean optimize(File file, String outSignature) {
		if (Signature.validPartial(outSignature)) {
			work(file, outSignature, false);
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean optimize(String file, String outSignature) {
		if (Signature.validPartial(outSignature)) {
			work(new File(file), outSignature, false);
			return true;
		} else {
			return false;
		}
	}
	
	public static void optimize(File file) {
		work(file, null, false);
	}
	
	public static void optimize(String file) {
		work(new File(file), null, false);
	}
	
	public static void optimize(File file, boolean autoSignature) {
		work(file, null, autoSignature);
	}
	
	public static void optimize(String file, boolean autoSignature) {
		work(new File(file), null, autoSignature);
	}
	
	public static void optimize(File file, boolean autoSignature, IntegerProperty numJobs) {
		numJobs.set(numJobs.get() + 1);
		Menu m = Menu.menu();
		if (m != null) {
			m.draw();
		}
		work(file, null, autoSignature);
		numJobs.set(numJobs.get() - 1);
		if (m != null) {
			m.draw();
		}
	}
}
