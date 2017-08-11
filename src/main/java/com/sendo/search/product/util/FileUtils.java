package com.sendo.search.product.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileUtils {
	
	public static List<String> readFile(String filePath) {
		List<String> result = new ArrayList<String>();
		try {

            File f = new File(filePath);
            BufferedReader b = new BufferedReader(new FileReader(f));
            String line = "";
            while ((line = b.readLine()) != null) {
            	if(line != null && line.trim().length() > 0)
            		result.add(line);
            }
            b.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;
	}

	
	public static void writeFile(String filePath, Collection<String> data){

		FileWriter fop = null;
		File file;

		try {

			file = new File(filePath);
			fop = new FileWriter(file);

			if (!file.exists()) {
				file.createNewFile();
			}

			for(String line: data) {
				fop.write(line + "\n");
			}
			fop.flush();
			fop.close();


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
