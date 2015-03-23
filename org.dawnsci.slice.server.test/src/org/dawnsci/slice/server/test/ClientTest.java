package org.dawnsci.slice.server.test;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;

import org.dawnsci.slice.client.DataClient;
import org.dawnsci.slice.server.Format;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.junit.Test;

/**
 * Runs as standard junit test.
 * Start the Data Server before running this test!
 * 
 * Test tests require that the DataServer is going and that the
 * data is at the pre-supposed locations.
 * 
 * TODO make this into a replicable unit test.
 * 
 * @author fcp94556
 *
 */
public class ClientTest {

	@Test
	public void testFullData() throws Exception {
		
		final DataClient<IDataset> client = new DataClient<IDataset>("http://localhost:8080/");
		client.setPath("c:/Work/results/TomographyDataSet.hdf5");
		client.setDataset("/entry/exchange/data");
		client.setSlice("[0,:1024,:1024]");
		
		final IDataset data = client.get();
		if (!Arrays.equals(data.getShape(), new int[]{1024, 1024})) {
			throw new Exception("Unexpected shape "+Arrays.toString(data.getShape()));
		}

	}
	
	@Test
	public void testDownsampledData() throws Exception {
		
		final DataClient<IDataset> client = new DataClient<IDataset>("http://localhost:8080/");
		client.setPath("c:/Work/results/TomographyDataSet.hdf5");
		client.setDataset("/entry/exchange/data");
		client.setSlice("[0,:1024,:1024]");
		client.setBin("MEAN:2x2");
		
		final IDataset data = client.get();
		if (!Arrays.equals(data.getShape(), new int[]{512, 512})) {
			throw new Exception("Unexpected shape "+Arrays.toString(data.getShape()));
		}

	}
	
	@Test
	public void testDownsampledJPG() throws Exception {
		
		final DataClient<BufferedImage> client = new DataClient<BufferedImage>("http://localhost:8080/");
		client.setPath("c:/Work/results/TomographyDataSet.hdf5");
		client.setDataset("/entry/exchange/data");
		client.setSlice("[0,:1024,:1024]");
		client.setBin("MEAN:2x2");
		client.setFormat(Format.JPG);
		client.setHisto("MEAN");
		
		final BufferedImage image = client.get();
		if (image.getHeight()!=512) throw new Exception("Unexpected image height '"+image.getHeight()+"'");
		if (image.getWidth()!=512)  throw new Exception("Unexpected image height '"+image.getWidth()+"'");
	}

	
	@Test
	public void testDownsampledMJPG() throws Exception {
		
		final DataClient<BufferedImage> client = new DataClient<BufferedImage>("http://localhost:8080/");
		client.setPath("c:/Work/results/TomographyDataSet.hdf5");
		client.setDataset("/entry/exchange/data");
		client.setSlice("[700,:1024,:1024]");
		client.setBin("MEAN:2x2");
		client.setFormat(Format.MJPG);
		client.setHisto("MEAN");
		client.setSleep(100); // Default anyway is 100ms
		
		
		int i = 0;
		while(!client.isFinished()) {
			
			final BufferedImage image = client.take();
			if (image ==null) break; // Last image in stream is null.
			if (image.getHeight()!=512) throw new Exception("Unexpected image height '"+image.getHeight()+"'");
			if (image.getWidth()!=512)  throw new Exception("Unexpected image height '"+image.getWidth()+"'");
			++i;
			System.out.println("Image "+i+" found");
		}
	
		if (i != 20) throw new Exception("20 images were not found! "+i+" were!");
	}
	
	@Test
	public void testFastMJPG() throws Exception {
		
		final DataClient<BufferedImage> client = new DataClient<BufferedImage>("http://localhost:8080/");
		client.setPath("RANDOM:512x512");
		client.setFormat(Format.MJPG);
		client.setHisto("MEAN");
		client.setSleep(10); // 100Hz - she's a fast one!
		
		int i = 0;
		while(!client.isFinished()) {
			
			final BufferedImage image = client.take();
			if (image ==null) break; // Last image in stream is null.
			if (image.getHeight()!=512) throw new Exception("Unexpected image height '"+image.getHeight()+"'");
			if (image.getWidth()!=512)  throw new Exception("Unexpected image height '"+image.getWidth()+"'");
			++i;
			if (i>1000) {
				client.setFinished(true);
				break; // That's enough of that
			}
			
			Thread.sleep(80);// Evil sleep means that take() is not as fast as send and there will be drops.
		}
	
		// We say
		System.out.println("Received images = "+i);
		System.out.println("Dropped images = "+client.getDroppedImageCount());
	}

//	@Test
//	public void testFastMDATA() throws Exception {
//		
//		final DataClient client = new DataClient("http://localhost:8080/");
//		client.setPath("RANDOM:512x512");
//		client.setFormat(Format.MDATA);
//		client.setHisto("MEAN");
//		client.setSleep(10); // 100Hz - she's a fast one!
//		
//		int i = 0;
//		while(!client.isFinished()) {
//			
//			final IDataset image = client.takeData();
//			if (image ==null) break; // Last image in stream is null.
//			if (image.getShape()[0]!=512) throw new Exception("Unexpected image height '"+image.getShape()[0]+"'");
//			if (image.getShape()[1]!=512)  throw new Exception("Unexpected image height '"+image.getShape()[1]+"'");
//			++i;
//			if (i>1000) {
//				client.setFinished(true);
//				break; // That's enough of that
//			}
//			
//			Thread.sleep(80);// Evil sleep means that take() is not as fast as send and there will be drops.
//		}
//	
//		// We say
//		System.out.println("Received images = "+i);
//		System.out.println("Dropped images = "+client.getDroppedImageCount());
//	}
}