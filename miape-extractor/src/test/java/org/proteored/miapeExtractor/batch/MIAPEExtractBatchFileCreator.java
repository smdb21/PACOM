package org.proteored.miapeExtractor.batch;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MIAPEExtractBatchFileCreator {

	@Test
	public void createFile() throws IOException {
		BufferedWriter fw = new BufferedWriter(new FileWriter(new File(
				"Y:\\backups Salva\\PME6\\miapeBatch.bat")));

		DataInputStream dis = new DataInputStream(new FileInputStream(new File(
				"Y:\\backups Salva\\PME6\\mascot_dat.csv")));
		String line = "";
		while ((line = dis.readLine()) != null) {
			final String[] split = line.split(",");
			String lab = split[0];
			List<String> list = new ArrayList<String>();
			list.add(split[1]);

			if (split.length > 2)
				list.add(split[2]);
			if (split.length > 3)
				list.add(split[3]);
			if (split.length > 4)
				list.add(split[4]);

			int numRep = 1;
			for (String string : list) {
				System.out.println(string);
				final String[] split2 = string.split("/");
				String date = split2[0];
				String dat = split2[1];
				String toPrint = "SET var1=" + date;
				fw.write(toPrint + "\n");
				toPrint = "SET var2=" + dat;
				fw.write(toPrint + "\n");
				toPrint = "SET command=perl C:\\inetpub\\mascot\\cgi\\export_dat_2_MZID11_optimised.pl file=E:/MASCOT/data/%var1%/%var2% do_export=1 prot_hit_num=1 prot_acc=1 pep_query=1 pep_rank=1 pep_isbold=1 pep_isunique=1 pep_exp_mz=1 sessionid=all_secdisabledsession export_format=mzIdentML _sigthreshold=0.05 use_homology=1 _minpeplen=5 _server_mudpit_switch=0.000000001 show_same_sets=1 _showsubsets=10 report=AUTO prot_desc=1";
				fw.write(toPrint + "\n");
				toPrint = "%command% > PME6_" + lab + "_rep" + numRep++
						+ ".mzid";
				fw.write(toPrint + "\n\n");
			}
		}

		fw.close();
		dis.close();
	}

}
