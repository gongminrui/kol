package kol.common.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
public class TimeRange {
    private Date startTime;
    private Date endTime;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public TimeRange() {
    }

    public TimeRange(Date startTime, Date endTime) {
        this.startTime = startTime;
        this.endTime = endTime;

        this.startDate = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        this.endDate = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
