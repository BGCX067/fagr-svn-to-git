package il.ac.tau.arielgue.fagr.utils;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.io.filefilter.FileFilterUtils;

public class DirNamer {
	public static void main(String[] args) {
		File mainFolder = new File("D:\\workspace\\fagr\\cluster_results");
		String logMainFolder = "D:\\workspace\\fagr\\log\\";
		FileFilter filter = FileFilterUtils.directoryFileFilter();

//		for (File folder : mainFolder.listFiles(filter)) {
//			File logFolder = new File(logMainFolder + folder.getName());
//			for (File job : folder.get)
//			
		}
	}


