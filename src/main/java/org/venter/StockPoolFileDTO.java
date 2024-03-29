package org.venter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author venter.zhu
 * @date 2024/3/20 16:31
 */
@Data
public class StockPoolFileDTO {
    @ExcelProperty(value = "Ticker")
    String stockNo;
    @ExcelProperty(value = "Name")
    String stockName;
    @ExcelProperty(value = "Qty")
    Integer qty;

    String channel;
}
