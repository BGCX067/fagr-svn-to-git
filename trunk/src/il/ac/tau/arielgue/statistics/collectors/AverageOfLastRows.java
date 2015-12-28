package il.ac.tau.arielgue.statistics.collectors;

import il.ac.tau.yoavram.pes.io.CsvReader;
import il.ac.tau.yoavram.pes.statistics.collectors.Collector;

import java.math.BigDecimal;
import java.math.MathContext;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;

public class AverageOfLastRows implements Collector {

	private static final Logger logger = Logger
			.getLogger(AverageOfLastRows.class);
	private int column = 0;
	private int rows = 0;
	private boolean onlyPositives = false;

	private int statisticInterval;
	int numOfRowsToAverageOn;

	@Override
	public String collect(CsvReader reader) {
		String[] row = null;
		String ret = null;
		BigDecimal sum = BigDecimal.ZERO;
		int count = 0;

		int totalRowNumber = 0;
		CsvReader readerHelper = new CsvReader(reader.getFilename(),
				reader.isHeader());
		while ((row = readerHelper.nextRow()) != null) {
			if (row.length > column && !Strings.isNullOrEmpty(row[column])) {
				try {
					BigDecimal data = new BigDecimal(row[column]);
					if (!isOnlyPositives()
							|| data.compareTo(BigDecimal.ZERO) != 0) {
						totalRowNumber++;
					}
				} catch (NumberFormatException e) {
					logger.error("couldn't parse '" + row[column] + "' in "
							+ readerHelper.getFilename() + ", row "
							+ totalRowNumber);
				}
			}
		}
		readerHelper.close();
		int indexOfRowToStartCollecting = totalRowNumber
				- (getNumOfRowsToAverageOn() / getStatisticInterval());
		while ((row = reader.nextRow()) != null) {
			if (row.length > column && !Strings.isNullOrEmpty(row[column])) {
				try {
					BigDecimal data = new BigDecimal(row[column]);
					if (!isOnlyPositives()
							|| data.compareTo(BigDecimal.ZERO) != 0) {
						if (count >= indexOfRowToStartCollecting) {
							sum = sum.add(data);
						}
						count++;
					}
				} catch (NumberFormatException e) {
					logger.error("couldn't parse '" + row[column] + "' in "
							+ reader.getFilename() + ", row " + count);
					return ret;
				}
			}
		}
		if ((rows == 0 && count > 0) || (rows == count)) {
			BigDecimal avg = sum.divide(new BigDecimal(
					getNumOfRowsToAverageOn()/ getStatisticInterval()), MathContext.DECIMAL128);
			ret = avg.toString();
		}
		return ret;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public void setOnlyPositives(boolean onlyPositives) {
		this.onlyPositives = onlyPositives;
	}

	public boolean isOnlyPositives() {
		return onlyPositives;
	}

	public int getStatisticInterval() {
		return statisticInterval;
	}

	public void setStatisticInterval(int statisticInterval) {
		this.statisticInterval = statisticInterval;
	}

	public int getNumOfRowsToAverageOn() {
		return numOfRowsToAverageOn;
	}

	public void setNumOfRowsToAverageOn(int numOfRowsToAverageOn) {
		this.numOfRowsToAverageOn = numOfRowsToAverageOn;
	}

}
