package ui;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextArea;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utilities.Utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;


public class MyTab extends Tab {

    private JFXButton button;
    private SplitPane splitPane;
    private JFXTextArea textArea;
    private WebView webView;


    private Color colorTheme;

    private String filePath = "";
    boolean isSaved = true;

    MyTab(String name, JFXTabPane tabPane, Color colorTheme) {
        super(name);
        this.colorTheme=colorTheme;
        splitPane = new SplitPane();
        setTextArea(new JFXTextArea());
        setWebView(new WebView());

        addListeners();

        setContent(splitPane);
        setGraphic(createTabButton(colorTheme));

        ((JFXButton) getGraphic()).setOnAction(e -> {
            if (!isSaved) {
                checkIfUserWantsToSaveFile();
            }
            tabPane.getTabs().remove(this);
        });

    }
    private JFXButton createTabButton(Color colorTheme) {
        button = new JFXButton();

        button.setText("X");
        button.setPrefWidth(10);
        button.setPrefHeight(10);
        button.getStyleClass().add("tab-button");
        updateButtonColor(colorTheme);
        return button;
    }

    private void addListeners() {
        setOnCloseRequest(e -> checkIfUserWantsToSaveFile());
    }

    void checkIfUserWantsToSaveFile() {
        if (!isSaved) {

            Stage saveFileConfirmationStage = new Stage();
            saveFileConfirmationStage.initModality(Modality.WINDOW_MODAL);
            JFXButton buttonOk = new JFXButton("OK"), buttonCancel= new JFXButton("Cancel");

            buttonOk.setOnAction(e -> {
                checkSaveInCurrentPath();
                saveFileConfirmationStage.close();
            });
            buttonCancel.setOnAction(e -> saveFileConfirmationStage.close());

            HBox hbox = new HBox( buttonOk, buttonCancel);
            hbox.setPadding(new Insets(30));
            VBox vbox = new VBox(new Text("Save file \"" + getText().replace(" (*)", "") + "\"?"), hbox);
            vbox.setAlignment(Pos.CENTER);
            vbox.setPadding(new Insets(30));

            JFXDecorator saveFileConfirmationDecorator = new JFXDecorator(saveFileConfirmationStage, vbox);

            Scene saveFileConfirmationScene = new Scene(saveFileConfirmationDecorator);

            saveFileConfirmationScene.getStylesheets().add("/css/JMarkPad.css");
            saveFileConfirmationDecorator.setStyle("-fx-decorator-color: " + Utilities.toRGB(colorTheme) + ";");
            saveFileConfirmationStage.setScene(saveFileConfirmationScene);
            saveFileConfirmationStage.setResizable(false);
            saveFileConfirmationStage.showAndWait();

        }
    }


    void checkSaveInCurrentPath() {
        File file = null;
        if (filePath.isEmpty()) {
            try {
                FileChooser fc = new FileChooser();
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
                fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown files (*.md)", "*.md"));

                Properties properties = new Properties();
                properties.load(new FileInputStream("jmarkpad.properties"));
                String folderPath = properties.getProperty("folderPath");

                if (folderPath != null) {
                    fc.setInitialDirectory(new File(folderPath));
                }

                file = fc.showSaveDialog(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            file = new File(filePath);
        }

        save(file);

    }

    void saveAs() {
        try {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("All files (*.*)", "*.*"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Markdown files (*.md)", "*.md"));

            Properties properties = new Properties();
            properties.load(new FileInputStream("jmarkpad.properties"));
            String folderPath = properties.getProperty("folderPath");

            if (folderPath != null) {
                fc.setInitialDirectory(new File(folderPath));
            }

            File file = fc.showSaveDialog(new Stage());
            save(file);
			
			folderPath = file.getParent();
			properties.setProperty("folderPath", String.valueOf(folderPath));
			properties.store(new FileOutputStream("jmarkpad.properties"), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void save(File file){
        if (file != null) {
            try {
                FileWriter fileWriter = new FileWriter(file);

                fileWriter.write(getTextArea().getText());
                fileWriter.close();
                filePath = file.getAbsolutePath();
                setSaved(true);
                setText(file.getName());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }




    void updateButtonColor(Color colorTheme){
        this.colorTheme=colorTheme;
        button.setStyle("-fx-background-color: " + Utilities.toRGB(colorTheme) + ";");
    }

    //Getters and setters
    @SuppressWarnings("unused")
    void setTextArea(JFXTextArea textArea) {
        this.textArea = textArea;
        textArea.textProperty().addListener(o -> {
            webView.getEngine().loadContent(Utilities.reparse(textArea.getText()), "text/html");
            setSaved(false);
        });
        if (splitPane.getItems().size() > 1) {
            splitPane.getItems().remove(0);
        }
        splitPane.getItems().add(0, textArea);

        setContent(splitPane);
    }

    @SuppressWarnings("unused")
    private void setWebView(WebView webView) {
        this.webView = webView;

        if (splitPane.getItems().size() > 1) {
            splitPane.getItems().remove(1);
        }
        splitPane.getItems().add(1, webView);

        setContent(splitPane);

    }

    @SuppressWarnings("unused")
    private JFXTextArea getTextArea() {
        return textArea;
    }

    @SuppressWarnings("unused")
    public WebView getWebView() {
        return webView;
    }

    @SuppressWarnings("unused")
    private void setSaved(boolean isSaved) {
        this.isSaved = isSaved;
        if (isSaved) {
            setText(getText().replace(" (*)", ""));
        } else {
            setText(getText().replace(" (*)", "") + " (*)");
        }

    }

    @SuppressWarnings("unused")
    String getFilePath() {
        return filePath;
    }

    @SuppressWarnings("unused")
    void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}