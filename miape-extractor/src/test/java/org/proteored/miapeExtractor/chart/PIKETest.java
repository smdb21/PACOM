package org.proteored.miapeExtractor.chart;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.proteored.miapeExtractor.utils.ClientHttpRequest;

public class PIKETest {

	@Test
	public void testPIKE() {
		try {
			ClientHttpRequest clientRequest = new ClientHttpRequest(new URL(
					"http://proteo.cnb.csic.es:8080/pike/pike"));
			clientRequest.setParameter("InputFile", new File("c:\\input.txt"));
			Map<String, String> parameters = new HashMap<String, String>();
			clientRequest.setParameter("username", "salva");
			clientRequest.setParameter("usermail", "smartinez@cnb.csic.es");
			clientRequest.setParameter("database", "0_SWISS-PROT-AC");
			clientRequest.setParameter("fieldselect", "0_Protein name");
			clientRequest.setParameter("fieldselect", "0_taxonomy");
			clientRequest.setParameter("fieldselect", "2_dbxref?PRIDE");
			clientRequest.setParameter("inputfile", "txt");

			final InputStream post = clientRequest.post(parameters);
			InputStreamReader reader = new InputStreamReader(post);

			final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
					new File("c:\\output.txt"))));
			int charsRead;
			char[] cbuf = new char[1024];
			while ((charsRead = reader.read(cbuf)) != -1) {
				writer.write(cbuf, 0, charsRead);
			}
			writer.flush();
			writer.close();
			reader.close();

		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}
}
