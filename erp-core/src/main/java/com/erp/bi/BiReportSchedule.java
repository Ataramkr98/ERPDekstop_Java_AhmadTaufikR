package com.erp.bi;

import java.time.Instant;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Scheduled delivery of a report by email (M-BI-007).
 */
@Entity(name = "BiReportSchedule")
@Table(name = "bi_report_schedule")
public class BiReportSchedule extends BaseEntity {

    /**
     * reportId
     */
    @Column(name = "report_id", nullable = false)
    public Long reportId;

    /**
     * name
     */
    @Column(name = "name", length = 200)
    public String name;

    /**
     * frequency
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", length = 40)
    public ScheduleFrequency frequency;

    /**
     * dayOfMonth
     */
    @Column(name = "day_of_month")
    public Integer dayOfMonth;

    /**
     * dayOfWeek
     */
    @Column(name = "day_of_week")
    public Integer dayOfWeek;

    /**
     * timeOfDay
     */
    @Column(name = "time_of_day", length = 5)
    public String timeOfDay;

    /**
     * Comma-separated email addresses.
     */
    @Column(name = "recipient_list", length = 1000)
    public String recipientList;

    /**
     * format
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 40)
    public ReportFormat format;

    /**
     * lastRunAt
     */
    @Column(name = "last_run_at")
    public Instant lastRunAt;

    /**
     * nextRunAt
     */
    @Column(name = "next_run_at")
    public Instant nextRunAt;

    /**
     * lastRunStatus
     */
    @Column(name = "last_run_status", length = 20)
    public String lastRunStatus;

    @Override
    public String displayLabel() {
        return name;
    }

}
