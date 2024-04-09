/*
 * Created by JFormDesigner on Fri Mar 15 14:39:30 CST 2024
 */

package org.venter;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.hash.Hash;
import cn.hutool.core.lang.hash.KetamaHash;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.util.FileUtils;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.intellij.uiDesigner.core.*;
import org.apache.poi.ss.formula.functions.T;
import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

/**
 * @author venter.zhu
 */
public class Test extends JFrame {
    public Test() {
        initComponents();
    }

    private void choseFile(ActionEvent e) {
        // TODO add your code here
        this.fileChooser1 = new JFileChooser();
        JButton button = (JButton) e.getSource();
        if(this.stockPoolBtn == button) {
            this.fileChooser1.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        } else {
            this.fileChooser1.setFileSelectionMode(JFileChooser.FILES_ONLY);
            this.fileChooser1.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".xlsx") || f.getName().endsWith(".xls");
                }
                @Override
                public String getDescription() {
                    return "xlsx,xls";
                }
            });
        }
        int returnValue = this.fileChooser1.showOpenDialog(null);
        if(returnValue == JFileChooser.APPROVE_OPTION) {
            if(button == this.rankFileBtn) {
                this.rankFile.setText(this.fileChooser1.getSelectedFile().getAbsolutePath());
            }else if (button == this.applyFileBtn){
                this.applyFile.setText(this.fileChooser1.getSelectedFile().getAbsolutePath());
            }else if (button == this.stockPoolBtn){
                this.stockPool.setText(this.fileChooser1.getSelectedFile().getAbsolutePath());
            }
        }
    }

    private void confirm(ActionEvent e) {
        // TODO add your code here
        System.out.println(this.applyFile.getText());
        if(StringUtils.isBlank(this.applyFile.getText())){
            JOptionPane.showMessageDialog(null, "请选择交易员股票申请表");
        }
        System.out.println(this.stockPool.getText());
        if(StringUtils.isBlank(this.stockPool.getText())){
            JOptionPane.showMessageDialog(null, "请选择劵池所在文件夹");
        }
        System.out.println(this.rankFile.getText());
        if(StringUtils.isBlank(this.rankFile.getText())){
            JOptionPane.showMessageDialog(null, "请选择交易员股票申请排名表！");
        }
        JButton button = (JButton) e.getSource();
        button.setText("处理中...");
        button.setEnabled(false);
        try {
            this.parseFile(this.rankFile.getText(),this.applyFile.getText(),this.stockPool.getText());
        }catch (Exception ignore){
            ignore.printStackTrace();
        }
        button.setText("开始处理");
        button.setEnabled(true);
    }

    public void parseFile(String rankFileStr,String applyFileStr,String stockPoolDirStr) {
        File rankFile = new File(rankFileStr);
        List<RankFileDTO> rankFileDTOList = new ArrayList<>();
        try (ExcelReader excelReader = EasyExcel.read(rankFile).build()) {
            ReadSheet readSheet = EasyExcel.readSheet(0)
                    .head(RankFileDTO.class)
                    .registerReadListener(new PageReadListener<RankFileDTO>(rankFileDTOList::addAll))
                    .build();
            excelReader.read(readSheet);
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误！");
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
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "出现错误！");
            return;
        }
        File stockPoolFile = new File(stockPoolDirStr);
        Map<String, StockPoolFileDTO> stockMap;
        if(stockPoolFile.isDirectory()){
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
                    JOptionPane.showMessageDialog(null, "出现错误！");
                    return;
                }
                String channel = fileName.split("\\.")[0].trim();
                stockPoolFileDTOListTemp.forEach(e->e.setChannel(channel));
                stockPoolFileDTOList.addAll(stockPoolFileDTOListTemp);
            }
            stockMap = stockPoolFileDTOList.stream().collect(Collectors.toMap(e->e.getChannel() + e.getStockNo(), v->v));
        }else {
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
                JOptionPane.showMessageDialog(null, "出现错误！");
                return;
            }
            String channel = fileName.split("\\.")[0].trim();
            stockMap = stockPoolFileDTOList.stream().peek(e -> e.setChannel(channel)).collect(Collectors.toMap(e->e.getChannel() + e.getStockNo(), v->v));
        }
        Map<String, RankFileDTO> rankMap = rankFileDTOList.stream().collect(Collectors.toMap(k -> k.getChannel().trim() + k.getOperatorCode().trim(), v -> v, (m1, m2) -> m1));
        Map<String,String> operatorMap = rankFileDTOList.stream().collect(Collectors.toMap(k -> k.getOperatorCode().trim(), RankFileDTO::getOperator, (m1, m2) -> m1));
        List<Result> results = new ArrayList<>();
        for (ApplyFileDTO applyFileDTO : applyFileDTOList) {
            RankFileDTO rankFileDTO = rankMap.get(applyFileDTO.getChannel().trim() + applyFileDTO.getOperatorCode().trim());
            Result result = getResult(applyFileDTO,rankFileDTO,operatorMap);
            results.add(result);
        }

//        Map<String, List<RankFileDTO>> rank = rankFileDTOList.stream().collect(Collectors.groupingBy(RankFileDTO::getChannel));
        Map<String, List<Result>> resultMap = results.stream().peek(e->{if(Objects.isNull(e.getRank())){e.setRank(999);}}).collect(Collectors.groupingBy(e->e.getChannel() + e.getStockNo()));

        for (Map.Entry<String, List<Result>> stringListEntry : resultMap.entrySet()) {
            String key = stringListEntry.getKey();
            List<Result> value = stringListEntry.getValue();
            StockPoolFileDTO stockPoolFileDTO = stockMap.get(key);
            if(Objects.isNull(stockPoolFileDTO)){
                continue;
            }
            Integer stockQty = stockPoolFileDTO.getQty();
            value.sort(Comparator.comparing(Result::getRank));
            for (Result result : value) {
                result.setStockQty(stockPoolFileDTO.getQty());
                if(stockQty == 0){
                    result.setQty(0);
                    continue;
                }
                if(stockQty < result.getApplyQty()){
                    result.setQty(stockQty);
                    stockQty = 0;
                }else {
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
        String exportFile = (applyFile.isDirectory()?applyFile.getAbsolutePath():applyFile.getParent()) + File.separator +  "股票分配结果" + System.currentTimeMillis()+ ".xlsx";
        try (ExcelWriter excelWriter = EasyExcel.write(exportFile).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet(0, "数据").head(Result.class).build();
            excelWriter.write(results, writeSheet);
        }

    }

    private Result getResult(ApplyFileDTO applyFileDTO, RankFileDTO rankFileDTO,Map<String,String> operatorMap) {
        Result result = new Result();
        if(Objects.isNull(rankFileDTO)) {
            result.setRank(999);
            String operator = operatorMap.get(applyFileDTO.getOperatorCode());
            if(StringUtils.isNotBlank(operator)){
                result.setOperator(operator);
            }
            result.setOperatorCode(applyFileDTO.getOperatorCode());
            result.setChannel(applyFileDTO.getChannel());
            result.setStockNo(applyFileDTO.getStockNo());
            result.setStockName(applyFileDTO.getStockName());
            result.setApplyQty(applyFileDTO.getApplyQty());
            result.setStockPrice(applyFileDTO.getStockPrice());
            result.setStockRate(applyFileDTO.getStockRate());
        }else{
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

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label1 = new JLabel();
        rankFile = new JTextField();
        rankFileBtn = new JButton();
        label2 = new JLabel();
        applyFile = new JTextField();
        applyFileBtn = new JButton();
        label3 = new JLabel();
        stockPool = new JTextField();
        stockPoolBtn = new JButton();
        parseFileBtn = new JButton();
        fileChooser1 = new JFileChooser();

        //======== this ========
        setMinimumSize(new Dimension(500, 400));
        var contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new GridLayoutManager(5, 13, new Insets(0, 0, 0, 0), 9, 9));

                //---- label1 ----
                label1.setText("\u6392\u540d\u6570\u636e");
                contentPanel.add(label1, new GridConstraints(1, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
                contentPanel.add(rankFile, new GridConstraints(1, 2, 1, 6,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    new Dimension(200, 0), null, null));

                //---- rankFileBtn ----
                rankFileBtn.setText("\u9009\u62e9\u6587\u4ef6");
                rankFileBtn.addActionListener(e -> choseFile(e));
                contentPanel.add(rankFileBtn, new GridConstraints(1, 8, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- label2 ----
                label2.setText("\u80a1\u7968\u7533\u8bf7\u8868");
                contentPanel.add(label2, new GridConstraints(2, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
                contentPanel.add(applyFile, new GridConstraints(2, 2, 1, 6,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    new Dimension(200, 0), null, null));

                //---- applyFileBtn ----
                applyFileBtn.setText("\u9009\u62e9\u6587\u4ef6");
                applyFileBtn.addActionListener(e -> choseFile(e));
                contentPanel.add(applyFileBtn, new GridConstraints(2, 8, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- label3 ----
                label3.setText("\u80a1\u7968\u5230\u52b5");
                contentPanel.add(label3, new GridConstraints(3, 0, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
                contentPanel.add(stockPool, new GridConstraints(3, 2, 1, 6,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    new Dimension(200, 10), null, null));

                //---- stockPoolBtn ----
                stockPoolBtn.setText("\u9009\u62e9\u6587\u4ef6\u5939");
                stockPoolBtn.addActionListener(e -> choseFile(e));
                contentPanel.add(stockPoolBtn, new GridConstraints(3, 8, 1, 1,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));

                //---- parseFileBtn ----
                parseFileBtn.setText("\u5f00\u59cb\u5904\u7406");
                parseFileBtn.addActionListener(e -> confirm(e));
                contentPanel.add(parseFileBtn, new GridConstraints(4, 2, 1, 3,
                    GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                    null, null, null));
            }
            dialogPane.add(contentPanel, BorderLayout.NORTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label1;
    private JTextField rankFile;
    private JButton rankFileBtn;
    private JLabel label2;
    private JTextField applyFile;
    private JButton applyFileBtn;
    private JLabel label3;
    private JTextField stockPool;
    private JButton stockPoolBtn;
    private JButton parseFileBtn;
    private JFileChooser fileChooser1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
