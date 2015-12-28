package il.ac.tau.arielgue.fagr.statistics;

import java.io.IOException;

import il.ac.tau.yoavram.pes.statistics.listeners.CsvWriterMultiListener;

public class CsvWriterMultiListenerBugFix extends CsvWriterMultiListener {

	@Override
	public void close() throws IOException {
		// do nothing
	}

	public void destroy() throws IOException {
		super.close();
	}
}
