package com.mingri.service;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /**
     * 导出数据报表
     * @param response
     */
    void exportClockData(HttpServletResponse response);
}
