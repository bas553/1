package com.finance;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Map;

public class Main extends Application {
    private final Database db = new Database();
    
    // Summary Cards
    private final Label income = new Label("฿0.00");
    private final Label expense = new Label("฿0.00");
    private final Label balance = new Label("฿0.00");
    private final Label debt = new Label("฿0.00");
    
    // Tables & UI Components
    private final TableView<Transaction> dashboardTable = new TableView<>();
    private final TableView<Transaction> manageTable = new TableView<>();
    private final PieChart overviewChart = new PieChart();
    private final BarChart<Number, String> topExpenseChart = 
        new BarChart<>(new NumberAxis(), new CategoryAxis());

    @Override
    public void start(Stage stage) {
        db.init();

        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #121318;");

        // --- Left Sidebar Navigation ---
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20, 15, 20, 15));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #1a1c23; -fx-border-color: #2a2d3a; -fx-border-width: 0 1 0 0;");

        Label logo = new Label("💰 MONEY VIEW");
        logo.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 15 0;");

        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-tab-max-height: 0; -fx-tab-min-height: 0;");

        Button btnDashboard = createNavButton("📊 ภาพรวม (Overview)");
        Button btnTransaction = createNavButton("📝 บันทึกรายการ");
        Button btnNote = createNavButton("📌 โน้ต Desktop");
        Button btnLoan = createNavButton("🧮 เงินกู้ / ดอกเบี้ย");

        btnDashboard.setOnAction(e -> tabPane.getSelectionModel().select(0));
        btnTransaction.setOnAction(e -> tabPane.getSelectionModel().select(1));
        btnNote.setOnAction(e -> tabPane.getSelectionModel().select(2));
        btnLoan.setOnAction(e -> tabPane.getSelectionModel().select(3));

        sidebar.getChildren().addAll(logo, btnDashboard, btnTransaction, btnNote, btnLoan);

        // --- Tabs Setup ---
        tabPane.getTabs().addAll(
            dashboardTab(),
            transactionTab(),
            noteTab(),
            loanTab()
        );

        mainLayout.setLeft(sidebar);
        mainLayout.setCenter(tabPane);

        Scene scene = new Scene(mainLayout, 1280, 800);
        applyDarkThemeStyle(scene);

        stage.setTitle("Finance Desktop - Dark Dashboard");
        stage.setScene(scene);
        stage.show();

        refresh();
    }

    private Button createNavButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e6ed; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 15;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2a2d3a; -fx-text-fill: #ff7043; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 15; -fx-background-radius: 6;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #e0e6ed; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 10 15;"));
        return btn;
    }

    // --- Dashboard Tab (สำหรับดูภาพรวมเท่านั้น) ---
    private Tab dashboardTab() {
        Tab tab = new Tab();

        HBox cards = new HBox(15,
            createCard("รายรับทั้งหมด", income, "#66bb6a"),
            createCard("รายจ่ายทั้งหมด", expense, "#ef5350"),
            createCard("คงเหลือสุทธิ", balance, "#42a5f5"),
            createCard("หนี้สินสะสม", debt, "#ffca28")
        );

        overviewChart.setTitle("สัดส่วนค่าใช้จ่าย");
        overviewChart.setLegendVisible(true);

        topExpenseChart.setTitle("5 หมวดหมู่จ่ายสูงสุด");
        topExpenseChart.setLegendVisible(false);

        HBox chartsBox = new HBox(15, createChartContainer(overviewChart), createChartContainer(topExpenseChart));
        chartsBox.setPrefHeight(280);

        VBox tableBox = new VBox(10, createSectionLabel("รายการล่าสุด"), dashboardTable);
        VBox.setVgrow(dashboardTable, Priority.ALWAYS);

        setupTableColumns(dashboardTable);

        VBox content = new VBox(20, cards, chartsBox, tableBox);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #121318;");

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #121318;");

        tab.setContent(scrollPane);
        return tab;
    }

    private VBox createCard(String title, Label valueLabel, String colorHex) {
        Label t = new Label(title);
        t.setStyle("-fx-text-fill: #a0a5b5; -fx-font-size: 13px; -fx-font-weight: bold;");
        valueLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        
        VBox card = new VBox(8, t, valueLabel);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle("-fx-background-color: #1a1c23; -fx-background-radius: 8; -fx-border-color: #2a2d3a; -fx-border-radius: 8;");
        return card;
    }

    private VBox createChartContainer(Chart chart) {
        VBox box = new VBox(chart);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: #1a1c23; -fx-background-radius: 8; -fx-border-color: #2a2d3a; -fx-border-radius: 8;");
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 16px; -fx-font-weight: bold;");
        return label;
    }

    private void setupTableColumns(TableView<Transaction> targetTable) {
        TableColumn<Transaction, String> d = new TableColumn<>("วันที่");
        d.setCellValueFactory(x -> x.getValue().dateProperty());
        TableColumn<Transaction, String> type = new TableColumn<>("ประเภท");
        type.setCellValueFactory(x -> x.getValue().typeProperty());
        TableColumn<Transaction, String> account = new TableColumn<>("แหล่งเงิน");
        account.setCellValueFactory(x -> x.getValue().accountProperty());
        TableColumn<Transaction, String> category = new TableColumn<>("หมวดหมู่");
        category.setCellValueFactory(x -> x.getValue().categoryProperty());
        TableColumn<Transaction, String> amount = new TableColumn<>("จำนวนเงิน");
        amount.setCellValueFactory(x ->
            new javafx.beans.property.SimpleStringProperty(
                money(x.getValue().getAmount())
            )
        );
        TableColumn<Transaction, String> desc = new TableColumn<>("รายละเอียด");
        desc.setCellValueFactory(x -> x.getValue().descriptionProperty());
        
        targetTable.getColumns().setAll(d, type, account, category, amount, desc);
        targetTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    // --- Transaction Tab (สำหรับเพิ่ม/ลบ รายการ) ---
    private Tab transactionTab() {
        Tab tab = new Tab();

        DatePicker date = new DatePicker(LocalDate.now());
        ComboBox<String> type = new ComboBox<>();
        type.getItems().addAll("รายรับ", "รายจ่าย");
        type.setValue("รายจ่าย");

        ComboBox<String> account = new ComboBox<>();
        account.getItems().addAll("เงินสด", "ธนาคาร", "บัตรเครดิต", "สินเชื่อ");
        account.setValue("เงินสด");

        ComboBox<String> category = new ComboBox<>();
        category.getItems().addAll("อาหาร", "เดินทาง", "บ้าน", "ช้อปปิ้ง", "บันเทิง", "การศึกษา", "สุขภาพ", "เงินเดือน", "ธุรกิจ", "อื่นๆ");
        category.setValue("อาหาร");

        TextField amount = new TextField();
        amount.setPromptText("จำนวนเงิน");
        TextField desc = new TextField();
        desc.setPromptText("รายละเอียด");

        Button add = new Button("＋ บันทึกรายการ");
        add.setStyle("-fx-background-color: #ff7043; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        add.setOnAction(e -> {
            try {
                double a = Double.parseDouble(amount.getText().replace(",", ""));
                if (a <= 0) throw new NumberFormatException();
                db.addTransaction(date.getValue().toString(), type.getValue(),
                        account.getValue(), category.getValue(), a, desc.getText());
                amount.clear(); desc.clear(); refresh();
                new Alert(Alert.AlertType.INFORMATION, "บันทึกรายการเรียบร้อย").showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "กรุณาตรวจสอบจำนวนเงิน").showAndWait();
            }
        });

        GridPane form = new GridPane();
        form.setHgap(15); form.setVgap(15); form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: #1a1c23; -fx-background-radius: 8; -fx-border-color: #2a2d3a; -fx-border-radius: 8;");

        form.addRow(0, createStyledLabel("วันที่"), date, createStyledLabel("ประเภท"), type, createStyledLabel("แหล่งเงิน"), account);
        form.addRow(1, createStyledLabel("หมวดหมู่"), category, createStyledLabel("จำนวนเงิน"), amount, createStyledLabel("รายละเอียด"), desc);
        form.add(add, 5, 2);

        // ปุ่มลบรายการประจำหน้านี้
        Button delete = new Button("🗑 ลบรายการที่เลือกจากตารางด้านล่าง");
        delete.setStyle("-fx-background-color: #ef5350; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        delete.setOnAction(e -> {
            Transaction selected = manageTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                db.deleteTransaction(selected.getId());
                refresh();
                new Alert(Alert.AlertType.INFORMATION, "ลบรายการเรียบร้อยแล้ว").showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "กรุณาคลิกเลือกรายการในตารางก่อนกดลบ").showAndWait();
            }
        });

        setupTableColumns(manageTable);
        VBox tableBox = new VBox(10, createSectionLabel("จัดการรายการทั้งหมด"), manageTable);
        VBox.setVgrow(manageTable, Priority.ALWAYS);

        VBox content = new VBox(15, createSectionLabel("เพิ่มข้อมูลรายการเงิน"), form, delete, tableBox);
        content.setPadding(new Insets(20));
        tab.setContent(content);
        return tab;
    }

    private Label createStyledLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        return l;
    }

    // --- Note Tab ---
    private Tab noteTab() {
        Tab tab = new Tab();

        TextArea note = new TextArea(db.loadNote());
        note.setPromptText("เขียนแผนการเงิน เช่น เดือนนี้ต้องจ่ายค่าเช่า...");
        note.setWrapText(true);

        Button save = new Button("💾 บันทึกโน้ต");
        save.setStyle("-fx-background-color: #42a5f5; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        save.setOnAction(e -> db.saveNote(note.getText()));

        Button floating = new Button("📌 เปิดเป็นหน้าต่างลอย");
        floating.setStyle("-fx-background-color: #2a2d3a; -fx-text-fill: #ffffff; -fx-padding: 8 16; -fx-cursor: hand;");
        floating.setOnAction(e -> {
            Stage s = new Stage();
            s.setTitle("💡 Finance Note");
            s.setAlwaysOnTop(true);
            TextArea t = new TextArea(note.getText());
            t.setWrapText(true);
            Button b = new Button("บันทึกและปิด");
            b.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
            b.setOnAction(x -> { db.saveNote(t.getText()); s.close(); note.setText(t.getText()); });
            VBox v = new VBox(8, new Label("💰 แผนการเงิน"), t, b);
            v.setPadding(new Insets(10));
            v.setStyle("-fx-background-color: #fff8a8;");
            VBox.setVgrow(t, Priority.ALWAYS);
            s.setScene(new Scene(v, 350, 430));
            s.show();
        });

        HBox bar = new HBox(10, save, floating);
        VBox v = new VBox(15, createSectionLabel("สมุดโน้ตส่วนตัว"), bar, note);
        v.setPadding(new Insets(20));
        VBox.setVgrow(note, Priority.ALWAYS);
        tab.setContent(v);
        return tab;
    }

    // --- Loan Tab ---
    private Tab loanTab() {
        Tab tab = new Tab();

        TextField principal = new TextField(); principal.setPromptText("เงินต้น");
        TextField rate = new TextField(); rate.setPromptText("ดอกเบี้ยต่อปี %");
        TextField months = new TextField(); months.setPromptText("จำนวนเดือน");
        Label result = new Label("กรอกข้อมูลแล้วกดคำนวณ");
        result.setStyle("-fx-text-fill: #ffca28; -fx-font-size: 15px; -fx-font-weight: bold;");

        TableView<String[]> loanTable = new TableView<>();

        Button calc = new Button("คำนวณลดต้นลดดอก");
        calc.setStyle("-fx-background-color: #ff7043; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-cursor: hand;");
        calc.setOnAction(e -> {
            try {
                double p = Double.parseDouble(principal.getText().replace(",", ""));
                double annual = Double.parseDouble(rate.getText().replace(",", ""));
                int n = Integer.parseInt(months.getText());
                double r = annual / 100 / 12;
                double payment = r == 0 ? p / n : p * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
                double bal = p, totalInterest = 0;
                loanTable.getItems().clear();

                for (int i = 1; i <= n; i++) {
                    double interest = bal * r;
                    double principalPart = (i == n) ? bal : payment - interest;
                    double pay = principalPart + interest;
                    bal = Math.max(0, bal - principalPart);
                    totalInterest += interest;
                    loanTable.getItems().add(new String[]{
                        String.valueOf(i), money(pay), money(interest),
                        money(principalPart), money(bal)
                    });
                }
                result.setText("ค่างวดประมาณ " + money(payment) +
                        " / เดือน   |   ดอกเบี้ยรวม " + money(totalInterest) +
                        "   |   ชำระรวม " + money(p + totalInterest));
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "กรุณากรอกข้อมูลให้ถูกต้อง").showAndWait();
            }
        });

        loanTable.getColumns().addAll(
            col("งวด", 0), col("ค่างวด", 1), col("ดอกเบี้ย", 2), col("ตัดเงินต้น", 3), col("คงเหลือ", 4)
        );
        loanTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox inputs = new HBox(10, principal, rate, months, calc);
        VBox v = new VBox(15, createSectionLabel("คำนวณสินเชื่อ / ดอกเบี้ย"), inputs, result, loanTable);
        v.setPadding(new Insets(20));
        VBox.setVgrow(loanTable, Priority.ALWAYS);
        tab.setContent(v);
        return tab;
    }

    private TableColumn<String[], String> col(String title, int index) {
        TableColumn<String[], String> c = new TableColumn<>(title);
        c.setCellValueFactory(x -> new javafx.beans.property.SimpleStringProperty(x.getValue()[index]));
        return c;
    }

    // --- Update Data & Charts ---
    private void refresh() {
        double[] s = db.summary();
        income.setText(money(s[0]));
        expense.setText(money(s[1]));
        balance.setText(money(s[0] - s[1]));
        debt.setText(money(s[2]));

        // Refresh ตารางทั้ง 2 หน้า
        dashboardTable.getItems().setAll(db.transactions());
        manageTable.getItems().setAll(db.transactions());

        // Refresh Overview PieChart
        overviewChart.getData().clear();
        Map<String, Double> expMap = db.getExpenseByCategory();
        expMap.forEach((category, amount) -> overviewChart.getData().add(new PieChart.Data(category, amount)));

        // Refresh Top Expense BarChart
        topExpenseChart.getData().clear();
        XYChart.Series<Number, String> series = new XYChart.Series<>();
        Map<String, Double> topMap = db.getTopExpenses(5);
        topMap.forEach((category, amount) -> series.getData().add(new XYChart.Data<>(amount, category)));
        topExpenseChart.getData().add(series);
    }

    private void applyDarkThemeStyle(Scene scene) {
        scene.getStylesheets().add("data:text/css," +
            ".text-field { -fx-background-color: #2a2d3a; -fx-text-fill: #ffffff; -fx-prompt-text-fill: #a0a5b5; -fx-border-color: #3f4354; -fx-border-radius: 4; -fx-font-weight: bold; }" +
            ".combo-box { -fx-background-color: #2a2d3a; -fx-border-color: #3f4354; -fx-border-radius: 4; }" +
            ".combo-box .cell { -fx-text-fill: #ffffff !important; -fx-font-weight: bold; }" +
            ".combo-box .arrow { -fx-background-color: #ffffff; }" +
            ".combo-box-popup .list-cell { -fx-text-fill: #ffffff; -fx-background-color: #2a2d3a; }" +
            ".combo-box-popup .list-cell:hover { -fx-background-color: #ff7043; -fx-text-fill: #ffffff; }" +
            ".date-picker { -fx-background-color: #2a2d3a; }" +
            ".date-picker .text-field { -fx-background-color: #2a2d3a; -fx-text-fill: #ffffff; -fx-font-weight: bold; }" +
            ".date-picker .arrow-button { -fx-background-color: #3f4354; }" +
            ".date-picker .arrow-button .arrow { -fx-background-color: #ffffff; }" +
            ".date-picker-popup { -fx-background-color: #2a2d3a; }" +
            ".date-picker-popup .day-cell { -fx-text-fill: #ffffff; -fx-background-color: #1a1c23; }" +
            ".date-picker-popup .day-cell:hover { -fx-background-color: #ff7043; }" +
            ".date-picker-popup .header { -fx-background-color: #2a2d3a; }" +
            ".date-picker-popup .spinner .button .left-arrow, .date-picker-popup .spinner .button .right-arrow { -fx-background-color: #ffffff; }" +
            ".chart-title { -fx-text-fill: #ffffff !important; -fx-font-weight: bold; -fx-font-size: 14px; }" +
            ".chart-legend-item { -fx-text-fill: #ffffff !important; -fx-font-weight: bold; }" +
            ".pie-chart-pie-label { -fx-fill: #ffffff !important; -fx-font-weight: bold; }" +
            ".axis { -fx-tick-label-fill: #ffffff !important; -fx-font-size: 11px; }" +
            ".axis-label { -fx-text-fill: #ffffff !important; }" +
            ".axis-tick-mark { -fx-stroke: #ffffff; }" +
            ".chart-vertical-grid-lines, .chart-horizontal-grid-lines { -fx-stroke: #2a2d3a; }" +
            ".table-view { -fx-background-color: #1a1c23; -fx-border-color: #2a2d3a; }" +
            ".table-view .column-header-background { -fx-background-color: #2a2d3a; }" +
            ".table-view .column-header, .table-view .filler { -fx-background-color: transparent; }" +
            ".table-view .column-header .label { -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT; }" +
            ".table-row-cell { -fx-background-color: #1a1c23; -fx-text-fill: #ffffff; }" +
            ".table-row-cell:odd { -fx-background-color: #16171d; }" +
            ".table-row-cell:selected { -fx-background-color: #ff7043; }" +
            ".table-cell { -fx-text-fill: #ffffff; }" +
            ".text-area { -fx-background-color: #1a1c23; -fx-text-fill: #ffffff; -fx-border-color: #2a2d3a; -fx-prompt-text-fill: #a0a5b5; }" +
            ".text-area .content { -fx-background-color: #1a1c23; }"
        );
    }

    static String money(double n) { return String.format("฿%,.2f", n); }

    public static void main(String[] args) { launch(args); }
}