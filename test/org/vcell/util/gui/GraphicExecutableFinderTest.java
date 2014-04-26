package org.vcell.util.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JFrame;

import org.junit.Test;
import org.vcell.util.ExecutableException;

import cbit.vcell.resource.ResourceUtil;
import cbit.vcell.resource.VisitSupport;

public class GraphicExecutableFinderTest {
	public static String TEST_EXE  = "MovingBoundary";
	//@Test
	public void testDialog( ) throws FileNotFoundException {
		ExecutableFinderDialog gef = new ExecutableFinderDialog(new JFrame( ), "find the moving boundary executable, okay?");
		 File f = ResourceUtil.getExecutable(TEST_EXE, false,gef);
		 System.out.println(f.getAbsolutePath());
	}
	
	//@Test
	public void qt( ) {
		Component parentComponent = new JFrame( );
		String rcode = DialogUtils.showOKCancelWarningDialog(parentComponent, "title" ," Eureka!");
		System.out.println(rcode);
		
		
	}
	
	@Test
	public void vtest( ) throws HeadlessException, IOException, ExecutableException, InterruptedException, URISyntaxException {
		VisitSupport.launchVisTool(new JFrame( ));
		//kill Junit around for a bit
		Thread.sleep(1000000);
	}

}
