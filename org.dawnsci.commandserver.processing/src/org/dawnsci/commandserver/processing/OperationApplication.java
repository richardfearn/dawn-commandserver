package org.dawnsci.commandserver.processing;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.commandserver.processing.beans.OperationBean;
import org.dawnsci.commandserver.processing.process.OperationExecution;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Used to execute an operation pipeline from an OperationBean
 * 
 * Example command line to run the application from a DAWN install
 * which should work on the cluster:
 *    module load dawn/nightly ; $DAWN_RELEASE_DIRECTORY/dawn -noSplash -application org.dawnsci.commandserver.processing.processing -data ~/Operation_Consumer -path /<beanpath>/operationBean.json >> someLogFile.txt
 * 
 * @author Matthew Gerring
 *
 */
public class OperationApplication implements IApplication {

	private OperationExecution runner;

	/**
	 * Must have the path to where the OperationBean is jsoned
	 * as an argument called 'path'
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// Parse out the configuration.
		final Map      args          = context.getArguments();
		final String[] configuration = (String[])args.get("application.args");
        
		Map<String, String> conf = new HashMap<String, String>(7);
		for (int i = 0; i < configuration.length; i++) {
			final String pkey = configuration[i];
			if (pkey.startsWith("-")) {
				conf.put(pkey.substring(1), configuration[i+1]);
			}
		}
		
		this.runner = new OperationExecution();
		runner.run(createOperationBean(conf));
		return runner;
	}

	private OperationBean createOperationBean(Map<String, String> conf) throws Exception {
		final String path   = conf.get("path");
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(new File(path), OperationBean.class); 
	}

	@Override
	public void stop() {
		if (runner!=null) runner.stop();
	}

}
