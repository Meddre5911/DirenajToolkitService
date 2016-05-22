package direnaj.functionalities.organizedBehaviour;

import org.apache.log4j.Logger;

public enum ResumeBreakPoint {

	INIT(0), //
	TWEET_COLLECTION_COMPLETED(1), //
	USER_ANALYZE_COMPLETED(2), //
	COS_SIMILARITY_INIT(3), //
	TF_CALCULATION_COMPLETED(4), //
	IDF_CALCULATION_COMPLETED(5), //
	TF_IDF_CALCULATION_COMPLETED(6), //
	SIMILARTY_CALCULATED(7), FINISHED(8);

	int sequence;

	private ResumeBreakPoint(int sequence) {
		this.sequence = sequence;
	}

	public int getValue() {
		return sequence;
	}

	public static boolean shouldProcessCurrentBreakPoint(ResumeBreakPoint currentBreakPoint,
			ResumeBreakPoint requestBreakPoint) {
		boolean shouldProcessCurrentBreakPoint = false;
		// get str
		String requestBreakPointStr = "";
		if (requestBreakPoint != null) {
			requestBreakPointStr = requestBreakPoint.name();
		}
		// check should process
		if (requestBreakPoint == null) {
			shouldProcessCurrentBreakPoint = true;
		} else if (currentBreakPoint.getValue() > requestBreakPoint.getValue()) {
			shouldProcessCurrentBreakPoint = true;
		}
		
		Logger.getLogger(ResumeBreakPoint.class)
				.debug("shouldProcessCurrentBreakPoint : " + shouldProcessCurrentBreakPoint
						+ " for CurrentBreakPoint : " + currentBreakPoint.name() + " - RequestBreakPoint : "
						+ requestBreakPointStr);
		return shouldProcessCurrentBreakPoint;
	}

}