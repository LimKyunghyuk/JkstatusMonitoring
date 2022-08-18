package com.hyundai.monitoring.jkstatus;

public class JkstatusVo {

	private final Long MAX_VALUE = 999L;
	
	private String name;
	private String act;
	private String state;
	private Long busy;
	private Long maxBusy;
	private Long con;
	private Long maxCon;

	public JkstatusVo() {
		this.name = "";
		this.act = "TIME_OUT";
		this.state = "TIME_OUT";
		this.busy = MAX_VALUE;
		this.maxBusy = MAX_VALUE;
		this.con = MAX_VALUE;
		this.maxCon = MAX_VALUE;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAct() {
		return act;
	}

	public void setAct(String act) {
		this.act = act;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Long getBusy() {
		return busy;
	}

	public void setBusy(Long busy) {
		this.busy = busy;
	}

	public Long getMaxBusy() {
		return maxBusy;
	}

	public void setMaxBusy(Long maxBusy) {
		this.maxBusy = maxBusy;
	}

	public Long getCon() {
		return con;
	}

	public void setCon(Long con) {
		this.con = con;
	}

	public Long getMaxCon() {
		return maxCon;
	}

	public void setMaxCon(Long maxCon) {
		this.maxCon = maxCon;
	}

	@Override
	public String toString() {
		return "JkstatusVo [name=" + name + ", act=" + act + ", state=" + state + ", busy=" + busy + ", maxBusy="
				+ maxBusy + ", con=" + con + ", maxCon=" + maxCon + "]";
	}

}
