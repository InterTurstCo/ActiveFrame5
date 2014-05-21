package ru.intertrust.cm.core.business.api.dto;

import org.simpleframework.xml.Attribute;

public class RelativeDate implements Dto{
    private static final long serialVersionUID = 6882692312608270974L;
    @Attribute(required=false)
	private Integer offsetMin;
	@Attribute(required=false)
	private Integer offsetHour;
	@Attribute(required=false)
	private Integer offsetDay;
	@Attribute(required=false)
	private Integer offsetMonth;
	@Attribute(required=false)
	private Integer offsetYear;
	@Attribute(required=false)
	private RelativeDateBase baseDate;
	
	public Integer getOffsetMin() {
		return offsetMin;
	}
	public void setOffsetMin(Integer offsetMin) {
		this.offsetMin = offsetMin;
	}
	public Integer getOffsetDay() {
		return offsetDay;
	}
	public void setOffsetDay(Integer offsetDay) {
		this.offsetDay = offsetDay;
	}
	public Integer getOffsetMonth() {
		return offsetMonth;
	}
	public void setOffsetMonth(Integer offsetMonth) {
		this.offsetMonth = offsetMonth;
	}
	public Integer getOffsetYear() {
		return offsetYear;
	}
	public void setOffsetYear(Integer offsetYear) {
		this.offsetYear = offsetYear;
	}
	public Integer getOffsetHour() {
		return offsetHour;
	}
	public void setOffsetHour(Integer offsetHour) {
		this.offsetHour = offsetHour;
	}
	public RelativeDateBase getBaseDate() {
		return baseDate;
	}
	public void setBaseDate(RelativeDateBase baseDate) {
		this.baseDate = baseDate;
	}
    @Override
    public String toString() {
        return "RelativeDate [offsetMin=" + offsetMin + ", offsetHour=" + offsetHour + ", offsetDay=" + offsetDay + ", offsetMonth=" + offsetMonth
                + ", offsetYear=" + offsetYear + ", baseDate=" + baseDate + "]";
    }
}
