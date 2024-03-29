package org.venter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author venter.zhu
 * @date 2024/3/20 16:16
 */
@Data
public class ApplyFileDTO {
    @ExcelProperty(value = "交易账号")
    String operatorCode;
    @ExcelProperty(value = "通道账号")
    String channel;
    @ExcelProperty(value = "股票代码")
    String stockNo;
    @ExcelProperty(value = "股票名称")
    String stockName;
    @ExcelProperty(value = "申请股数")
    Integer applyQty;
    @ExcelProperty(value = "股价")
    BigDecimal stockPrice;
    @ExcelProperty(value = "券息%")
    BigDecimal stockRate;
}
