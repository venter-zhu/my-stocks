package org.venter;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author venter.zhu
 * @date 2024/3/20 15:48
 */
@Data
public class RankFileDTO {
    @ExcelProperty(value = "通道")
    String channel;
    @ExcelProperty(value = "交易员代码")
    String operatorCode;
    @ExcelProperty(value = "交易员")
    String operator;
    @ExcelProperty(value = "综合排名")
    Integer rank;
}
