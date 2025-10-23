package org.venter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.hash.KetamaHash;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import javafx.scene.control.Alert;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author venter.zhu
 * @date 2025/10/23 11:09
 */
public class Processer {

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void parseFile(String rankFileStr, String applyFileStr, String stockPoolDirStr) {
        File rankFile = new File(rankFileStr);
        List<RankFileDTO> rankFileDTOList = new ArrayList<>();
        try (ExcelReader excelReader = EasyExcel.read(rankFile).build()) {
            ReadSheet readSheet = EasyExcel.readSheet(0)
                    .head(RankFileDTO.class)
                    .registerReadListener(new PageReadListener<RankFileDTO>(rankFileDTOList::addAll))
                    .build();
            excelReader.read(readSheet);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("出现错误！");
            return;
        }
        File applyFile = new File(applyFileStr);
        List<ApplyFileDTO> applyFileDTOList = new ArrayList<>();
        try (ExcelReader excelReader = EasyExcel.read(applyFile).build()) {
            ReadSheet readSheet = EasyExcel.readSheet("details")
                    .head(ApplyFileDTO.class)
                    .registerReadListener(new PageReadListener<ApplyFileDTO>(applyFileDTOList::addAll))
                    .build();
            excelReader.read(readSheet);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("出现错误！");
            return;
        }
        File stockPoolFile = new File(stockPoolDirStr);
        Map<String, StockPoolFileDTO> stockMap;
        if (stockPoolFile.isDirectory()) {
            List<File> files = FileUtil.loopFiles(stockPoolFile.getAbsolutePath());
            List<StockPoolFileDTO> stockPoolFileDTOList = new ArrayList<>();
            for (File file : files) {
                List<StockPoolFileDTO> stockPoolFileDTOListTemp = new ArrayList<>();
                String fileName = file.getName();
                try (ExcelReader excelReader = EasyExcel.read(file).build()) {
                    ReadSheet readSheet = EasyExcel.readSheet(0)
                            .head(StockPoolFileDTO.class)
                            .registerReadListener(new PageReadListener<StockPoolFileDTO>(stockPoolFileDTOListTemp::addAll))
                            .build();
                    excelReader.read(readSheet);
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert( "出现错误！");
                    return;
                }
                String channel = fileName.split("\\.")[0].trim();
                stockPoolFileDTOListTemp.forEach(e -> e.setChannel(channel));
                stockPoolFileDTOList.addAll(stockPoolFileDTOListTemp);
            }
            stockMap = stockPoolFileDTOList.stream().collect(Collectors.toMap(e -> e.getChannel() + e.getStockNo(), v -> v));
        } else {
            String fileName = stockPoolFile.getName();
            List<StockPoolFileDTO> stockPoolFileDTOList = new ArrayList<>();
            try (ExcelReader excelReader = EasyExcel.read(stockPoolFile).build()) {
                ReadSheet readSheet = EasyExcel.readSheet(0)
                        .head(StockPoolFileDTO.class)
                        .registerReadListener(new PageReadListener<StockPoolFileDTO>(stockPoolFileDTOList::addAll))
                        .build();
                excelReader.read(readSheet);
            } catch (Exception e) {
                e.printStackTrace();
                showAlert( "出现错误！");
                return;
            }
            String channel = fileName.split("\\.")[0].trim();
            stockMap = stockPoolFileDTOList.stream().peek(e -> e.setChannel(channel)).collect(Collectors.toMap(e -> e.getChannel() + e.getStockNo(), v -> v));
        }
        Map<String, RankFileDTO> rankMap = rankFileDTOList.stream().collect(Collectors.toMap(k -> k.getChannel().trim() + k.getOperatorCode().trim(), v -> v, (m1, m2) -> m1));
        Map<String, String> operatorMap = rankFileDTOList.stream().collect(Collectors.toMap(k -> k.getOperatorCode().trim(), RankFileDTO::getOperator, (m1, m2) -> m1));
        List<Result> results = new ArrayList<>();
        for (ApplyFileDTO applyFileDTO : applyFileDTOList) {
            RankFileDTO rankFileDTO = rankMap.get(applyFileDTO.getChannel().trim() + applyFileDTO.getOperatorCode().trim());
            Result result = getResult(applyFileDTO, rankFileDTO, operatorMap);
            results.add(result);
        }

//        Map<String, List<RankFileDTO>> rank = rankFileDTOList.stream().collect(Collectors.groupingBy(RankFileDTO::getChannel));
        Map<String, List<Result>> resultMap = results.stream().peek(e -> {
            if (Objects.isNull(e.getRank())) {
                e.setRank(999);
            }
        }).collect(Collectors.groupingBy(e -> e.getChannel() + e.getStockNo()));

        for (Map.Entry<String, List<Result>> stringListEntry : resultMap.entrySet()) {
            String key = stringListEntry.getKey();
            List<Result> value = stringListEntry.getValue();
            StockPoolFileDTO stockPoolFileDTO = stockMap.get(key);
            if (Objects.isNull(stockPoolFileDTO)) {
                continue;
            }
            Integer stockQty = stockPoolFileDTO.getQty();
            value.sort(Comparator.comparing(Result::getRank));
            for (Result result : value) {
                result.setStockQty(stockPoolFileDTO.getQty());
                if (stockQty == 0) {
                    result.setQty(0);
                    continue;
                }
                if (stockQty < result.getApplyQty()) {
                    result.setQty(stockQty);
                    stockQty = 0;
                } else {
                    result.setQty(result.getApplyQty());
                    stockQty = stockQty - result.getApplyQty();
                }
            }
        }
        // 导出文件
        // 排一下序，方便查看
        KetamaHash ketamaHash = new KetamaHash();
        results.sort((o1, o2) -> {
            int i = ketamaHash.hash32(o1.getStockNo() + o1.getChannel()) + o1.getRank();
            int j = ketamaHash.hash32(o2.getStockNo() + o2.getChannel()) + o2.getRank();
            return Integer.compare(i, j);
        });
        String exportFile = (applyFile.isDirectory() ? applyFile.getAbsolutePath() : applyFile.getParent()) + File.separator + "股票分配结果" + System.currentTimeMillis() + ".xlsx";
        try (ExcelWriter excelWriter = EasyExcel.write(exportFile).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(0, "数据").head(Result.class).build();
            excelWriter.write(results, writeSheet);
        }
    }

    private Result getResult(ApplyFileDTO applyFileDTO, RankFileDTO rankFileDTO, Map<String, String> operatorMap) {
        Result result = new Result();
        if (Objects.isNull(rankFileDTO)) {
            result.setRank(999);
            String operator = operatorMap.get(applyFileDTO.getOperatorCode());
            if (StringUtils.isNotBlank(operator)) {
                result.setOperator(operator);
            }
            result.setOperatorCode(applyFileDTO.getOperatorCode());
            result.setChannel(applyFileDTO.getChannel());
            result.setStockNo(applyFileDTO.getStockNo());
            result.setStockName(applyFileDTO.getStockName());
            result.setApplyQty(applyFileDTO.getApplyQty());
            result.setStockPrice(applyFileDTO.getStockPrice());
            result.setStockRate(applyFileDTO.getStockRate());
        } else {
            result.setOperator(rankFileDTO.getOperator());
            result.setRank(rankFileDTO.getRank());
            result.setOperatorCode(rankFileDTO.getOperatorCode());
            result.setChannel(rankFileDTO.getChannel());
            result.setStockNo(applyFileDTO.getStockNo());
            result.setStockName(applyFileDTO.getStockName());
            result.setApplyQty(applyFileDTO.getApplyQty());
            result.setStockPrice(applyFileDTO.getStockPrice());
            result.setStockRate(applyFileDTO.getStockRate());
        }
        return result;
    }
}
