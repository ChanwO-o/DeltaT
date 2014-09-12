package com.stopwatch.delta.t;

import java.io.Serializable;

public class TimeRecord implements Serializable {

	private static final long serialVersionUID = 1L;
	long milliseconds;
	
	public TimeRecord(long mills) {
		milliseconds = mills;
	}
	
	public void setMilliseconds(long mills) {
		milliseconds = mills;
	}

	public String toString() {
		
		int sec = (int) (milliseconds / 1000);
		int min = sec / 60;
		sec = sec % 60;
		int mil = (int) milliseconds % 1000;
		
		return (min + ":" + String.format("%02d", sec) + ":" + String.format("%03d", mil));
	}
	
}
