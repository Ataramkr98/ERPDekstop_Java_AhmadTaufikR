package com.erp.bi;

import com.erp.shared.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * One tile on a dashboard (M-BI-004). Clicking a tile drills through to the underlying records.
 */
@Entity(name = "BiTile")
@Table(name = "bi_tile")
public class BiTile extends BaseEntity {

    /**
     * dashboardId
     */
    @Column(name = "dashboard_id", nullable = false)
    public Long dashboardId;

    /**
     * kpiId
     */
    @Column(name = "kpi_id")
    public Long kpiId;

    /**
     * title
     */
    @Column(name = "title", length = 200, nullable = false)
    public String title;

    /**
     * tileType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tile_type", length = 40)
    public TileType tileType;

    /**
     * chartType
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", length = 40)
    public ChartType chartType;

    /**
     * Navigation target opened when the tile is clicked.
     */
    @Column(name = "drilldown_path", length = 300)
    public String drilldownPath;

    /**
     * filterExpression
     */
    @Column(name = "filter_expression", length = 500)
    public String filterExpression;

    /**
     * colIndex
     */
    @Column(name = "col_index")
    public Integer colIndex;

    /**
     * rowIndex
     */
    @Column(name = "row_index")
    public Integer rowIndex;

    /**
     * colSpan
     */
    @Column(name = "col_span")
    public Integer colSpan;

    /**
     * rowSpan
     */
    @Column(name = "row_span")
    public Integer rowSpan;

    /**
     * colour
     */
    @Column(name = "colour", length = 20)
    public String colour;

    @Override
    public String displayLabel() {
        return title;
    }

}
