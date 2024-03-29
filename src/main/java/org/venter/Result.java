package org.venter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author venter.zhu
 * @date 2024/3/28 16:31
 */
@Data
public class Result{
    @ExcelProperty(value = "交易账号")
    String operatorCode;
    @ExcelProperty(value = "交易员")
    String operator;
    @ExcelProperty(value = "综合排名")
    Integer rank;
    @ExcelProperty(value = "通道账号")
    String channel;
    @ExcelProperty(value = "股票代码")
    String stockNo;
    @ExcelProperty(value = "股票名称")
    String stockName;
    @ExcelProperty(value = "券池总数")
    Integer stockQty;
    @ExcelProperty(value = "股价")
    BigDecimal stockPrice;
    @ExcelProperty(value = "券息%")
    BigDecimal stockRate;
    @ExcelProperty(value = "申请股数")
    Integer applyQty;
    @ExcelProperty(value = "实际分配")
    Integer qty;
}
