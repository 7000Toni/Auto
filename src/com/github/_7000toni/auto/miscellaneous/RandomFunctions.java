package com.github._7000toni.auto.miscellaneous;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.github._7000toni.auto.dataset.Signature;

import javafx.stage.FileChooser;

public class RandomFunctions {
	
	public static void mergeFiles(List<File> files) {
		if (files == null) {
			return;
		}
		new Thread(() -> {
			ArrayList<String> nf = new ArrayList<String>();
			int size = files.size();
			int j = 0;
			for (File f : files) {										
				try (FileInputStream fis = new FileInputStream(f);
						BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
					String s = br.readLine();
					while (s != null) {
						nf.add(s);
						s = br.readLine();
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				j++;
				System.out.println("reading files: " + (int)((j*100)/(double)size) + "%");				
			}
			try (FileOutputStream fos = new FileOutputStream(new File("./merged"))) {
				fos.write(("size name tickSize numDecimalPts\n").getBytes());
				int last = -1;
				for (int i = 0; i < nf.size(); i++) {
					if (!Signature.validFull(nf.get(i))) {
						fos.write((nf.get(i) + '\n').getBytes());
					}
					int p = (int)((i*100)/(double)(nf.size()-1));
					if (p != last) {
						last = p;
						System.out.println("writing file: " + p + "%");
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}).start();
	}
	
	public static void databendoOptimizer() {
		File init = new File("C:\\Users\\Toni C\\Desktop\\TC'S\\The Projects\\Java\\Auto\\res");
		FileChooser fc = new FileChooser();
		if (init.exists()) {
			fc.setInitialDirectory(init);
		} else {
			fc.setInitialDirectory(new File("./"));
		}	
		fc.setTitle("Select Databendo Files");
		List<File> files = fc.showOpenMultipleDialog(null);		
		if (files == null) {
			return;
		}	
		double d = 0;
		for (File f : files) {	
			PrintWriter pw = null;
			try(FileInputStream fis = new FileInputStream(f);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
				br.readLine();	
				String contract = br.readLine();
				pw = new PrintWriter(new File("./res/mostly es trades/" + contract.substring(0, 2) + f.getName().substring(10, 18) + ".txt"));
				String in = br.readLine();
				String signature = contract.substring(0, 2) + f.getName().substring(10, 18) + " 0.25 2";
				int n = 0;
				ArrayList<String> data = new ArrayList<String>();
				while(in != null) {
					StringTokenizer st = new StringTokenizer(in, ",");
					String date = st.nextToken();
					for (int i = 0; i < 7; i++) {
						st.nextToken();
					}
					String price;
					if (contract.substring(0, 2).equals("ES")) {
						price = st.nextToken().substring(0, 7);
					} else {
						price = st.nextToken().substring(0, 8);
					}
					for (int i = 0; i < 4; i++) {
						st.nextToken();
					}
					if (!st.nextToken().equals(contract)) {
						in = br.readLine();
						continue;
					}
					String time = date.substring(date.indexOf('T') + 1, date.indexOf('T') + 1 + 8);
					date = date.substring(8, 10) + '/' + date.substring(5, 7) + '/' + date.substring(0, 4);
					String out = price + ' ' + date + ' ' + time;
					data.add(out);
					//System.out.println(out);
					n++;
					in = br.readLine();
				}
				signature = n + " " + signature;
				pw.println(signature);
				for (String s : data) {
					pw.println(s);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				pw.close();
			}
			d++;
			System.out.println(d*100/files.size());
		}		
	}
}
