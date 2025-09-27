import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class MarketTickFileOptimizer {
	
	private static void work(File file) {
		try (FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				FileOutputStream fos = new FileOutputStream(new File("./res/" + file.getName().substring(0, file.getName().indexOf('.')) + "_Optimized.csv"), true)) {
			String in = br.readLine() + "\n";
			int size = Integer.parseInt(in.substring(0, in.indexOf(' ')));
			String outSignature = in.substring(in.indexOf(' ') + 1);
			ArrayList<String> ticks = new ArrayList<String>();
			in = br.readLine();
			int progress = 0;
			boolean changed = true;
			int last = 0;
			int newSize = 0;
			for (int i = 0; i < size; i++) {
				if (in == null) {
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
				int percent = (int)((double)progress/size*100);
				if (last < percent) {
					changed = true;
				}
				last = percent;
				if (changed) {
					System.out.println(percent + "%");
					changed = false;
				}				
			}
			fos.write((newSize + " " + outSignature).getBytes());
			for (String s : ticks) {
				fos.write((s + '\n').getBytes());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void optimize(File file) {
		work(file);
	}
	
	public static void optimize(String file) {
		work(new File(file));
	}
}
