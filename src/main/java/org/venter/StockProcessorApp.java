package org.venter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class StockProcessorApp extends Application {

    private TextField rankingFileField = new TextField();
    private TextField applicationFileField = new TextField();
    private TextField folderField = new TextField();

    @Override
    public void start(Stage primaryStage) {
        // 输入框设置
        setupTextField(rankingFileField);
        setupTextField(applicationFileField);
        setupTextField(folderField);

        // 创建三行输入区域
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 20, 30));

        // 第一行：排名数据
        Label label1 = createBoldLabel("排名数据：");
        Button browseBtn1 = new Button("浏览...");
        browseBtn1.setOnAction(e -> selectExcelFile(rankingFileField));
        grid.add(label1, 0, 0);
        grid.add(rankingFileField, 1, 0);
        grid.add(browseBtn1, 2, 0);

        // 第二行：股票申请表
        Label label2 = createBoldLabel("股票申请表：");
        Button browseBtn2 = new Button("浏览...");
        browseBtn2.setOnAction(e -> selectExcelFile(applicationFileField));
        grid.add(label2, 0, 1);
        grid.add(applicationFileField, 1, 1);
        grid.add(browseBtn2, 2, 1);

        // 第三行：股票到券（文件夹）
        Label label3 = createBoldLabel("股票到券：");
        Button browseBtn3 = new Button("浏览...");
        browseBtn3.setOnAction(e -> selectFolder(folderField));
        grid.add(label3, 0, 2);
        grid.add(folderField, 1, 2);
        grid.add(browseBtn3, 2, 2);

        // 开始处理按钮
        Button processButton = new Button("开始处理");
        processButton.getStyleClass().add("process-button");
        processButton.setOnAction(this::onProcessClicked);
        processButton.setPrefSize(130, 38);

        // 按钮区域
        HBox buttonBox = new HBox(processButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 20, 0));

        // 主布局
        VBox root = new VBox(10);
        root.getChildren().addAll(grid, buttonBox);
        root.setAlignment(Pos.TOP_CENTER);

        // 场景 & 样式
        Scene scene = new Scene(root, 650, 280);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setTitle("股票数据处理工具");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("input-label");
        return label;
    }

    private void setupTextField(TextField field) {
        field.setPrefColumnCount(25);
        field.setEditable(false);
        field.getStyleClass().add("file-path-field");
    }

    private void selectExcelFile(TextField targetField) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("选择 Excel 文件");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel 文件", "*.xlsx", "*.xls")
        );
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            targetField.setText(file.getAbsolutePath());
        }
    }

    private void selectFolder(TextField targetField) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("选择文件夹");
        File folder = chooser.showDialog(null);
        if (folder != null) {
            targetField.setText(folder.getAbsolutePath());
        }
    }

    private void onProcessClicked(javafx.event.ActionEvent actionEvent) {
        String ranking = rankingFileField.getText().trim();
        String app = applicationFileField.getText().trim();
        String folder = folderField.getText().trim();

        if (ranking.isEmpty() || app.isEmpty() || folder.isEmpty()) {
            showAlert("请确保所有路径都已选择！");
            return;
        }

        try {
            Processer processer = new Processer();
            processer.parseFile(ranking, app, folder);
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        showAlert(
                "✅ 开始处理数据...\n\n" +
                        "排名数据: " + ranking + "\n" +
                        "申请表: " + app + "\n" +
                        "输出目录: " + folder
        );
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}