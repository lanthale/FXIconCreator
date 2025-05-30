package at.itarchitects.fxiconcreator;

import com.github.gino0631.icns.IcnsBuilder;
import com.github.gino0631.icns.IcnsIcons;
import com.github.gino0631.icns.IcnsType;
import com.sun.javafx.iio.common.ImageTools;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.SearchableComboBox;
import org.kordamp.ikonli.IkonProvider;
import org.kordamp.ikonli.javafx.FontIcon;

public class FXIconController implements Initializable {

    private final DirectoryChooser dirChooser;
    private final FileChooser fileChooser;
    private FontIcon iconview;
    private FontIcon iconviewDelete;
    private UtilityTools util;
    private ExecutorService executor;
    private File outFile;
    private String pngString512;
    private PopOver ikonliOver;
    private SimpleStringProperty ikonLiteral;
    private SimpleObjectProperty<Color> selectedColor;
    private ObservableList<FXIconController.IkonData> ikons;
    private ObservableList<Label> ikonLiterals;
    private HashMap<String, String> iconPrefix;

    private ResourceBundle resources;
    @FXML
    private AnchorPane pane;
    @FXML
    private VBox vbox;
    @FXML
    private Button generateButton;
    @FXML
    private CheckBox linuxCheckBox;
    @FXML
    private CheckBox windowsCheckBox;
    @FXML
    private CheckBox macosCheckBox;
    @FXML
    private ImageView imageView;
    @FXML
    private ProgressIndicator progressBar;
    @FXML
    private AnchorPane progressPane;
    @FXML
    private Button selectFilesButton;
    private File selectedImageFile;
    @FXML
    private CheckBox androidCheckBox;
    @FXML
    private CheckBox iosCheckBox;
    @FXML
    private Button selectIconFontButton;
    @FXML
    private FontIcon placeHolderIcon;
    @FXML
    private Label instructionsLabel;
    private ColorPicker colorPicker;
    private Button okButton;
    private Button cancleButton;
    private CheckBox backgroundTransparent;
    private ColorPicker colorPickerBackground;
    @FXML
    private HBox iconBackgroundBox;

    public FXIconController() {
        dirChooser = new DirectoryChooser();
        fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();
        String OS = System.getProperty("os.name").toUpperCase();        
        ikonLiteral = new SimpleStringProperty();
        selectedColor = new SimpleObjectProperty<>();
        generateButton.setDisable(true);
        ikonliOver = new PopOver();
        ikonliOver.setAutoHide(false);
        ikonliOver.setCloseButtonEnabled(true);
        ikonliOver.setDetachable(true);
        ikonliOver.setHeaderAlwaysVisible(true);
        ikonliOver.setTitle("iKonli property selection");
        ikonLiterals = FXCollections.observableArrayList();
        iconPrefix = new HashMap<>();
        setupPrefix();        
        setupPopOver();
    }

    private void setupPopOver() {
        VBox content = new VBox();
        content.setPadding(new Insets(10, 10, 10, 10));
        content.setSpacing(10);

        ikons = FXCollections.observableArrayList();
        HBox fontSelBox = new HBox();
        fontSelBox.setSpacing(5);
        fontSelBox.setAlignment(Pos.CENTER_RIGHT);
        fontSelBox.getChildren().add(new Label("Font Family"));
        SearchableComboBox<IkonData> cbox = new SearchableComboBox<>();
        cbox.setMinWidth(160);
        cbox.setMaxWidth(160);
        cbox.setItems(ikons);
        Task<IkonData> iconProviderTask = new Task<>() {
            @Override
            protected IkonData call() throws Exception {
                if (null != IkonProvider.class.getModule().getLayer()) {
                    for (IkonProvider provider : ServiceLoader.load(IkonProvider.class.getModule().getLayer(), IkonProvider.class)) {
                        if (!FXIconController.IkonData.of(provider).name.equalsIgnoreCase("ikonli")) {
                            updateValue(FXIconController.IkonData.of(provider));
                        }
                    }
                } else {
                    for (IkonProvider provider : ServiceLoader.load(IkonProvider.class)) {
                        //System.out.println("Name2: " + FXIconController.IkonData.of(provider).ikonProvider.getIkon().getName());
                        if (!FXIconController.IkonData.of(provider).name.equalsIgnoreCase("ikonli")) {
                            updateValue(FXIconController.IkonData.of(provider));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void updateValue(IkonData v) {
                super.updateValue(v);
                Platform.runLater(() -> {
                    ikons.add(v);
                });
            }

        };
        iconProviderTask.setOnSucceeded((t) -> {
            cbox.getSelectionModel().select(21);
        });
        executor.submit(iconProviderTask);
        fontSelBox.getChildren().add(cbox);
        content.getChildren().add(fontSelBox);
        HBox iconSelBox = new HBox();
        iconSelBox.setSpacing(5);
        iconSelBox.setAlignment(Pos.CENTER_RIGHT);
        iconSelBox.getChildren().add(new Label("Icon:"));
        SearchableComboBox<Label> cboxIcons = new SearchableComboBox<>();
        cboxIcons.setMinWidth(160);
        cboxIcons.setMaxWidth(160);
        cboxIcons.setItems(ikonLiterals);
        cbox.valueProperty().addListener((o) -> {
            Task<Label> iconLabelTask = new Task<>() {
                @Override
                protected Label call() throws Exception {
                    Field[] fields = ikons.get(cbox.getSelectionModel().getSelectedIndex()).ikonProvider.getIkon().getFields();
                    ikonLiterals.clear();
                    String prefix = iconPrefix.get(cbox.getValue().ikonProvider.getIkon().getName());
                    for (int i = 0; i < fields.length; i++) {
                        Field field = fields[i];
                        if (!field.getName().equalsIgnoreCase("NONE")) {
                            String iconStr = (prefix + field.getName().replace('_', '-')).toLowerCase();
                            Label entry = new Label();
                            entry.setFont(new Font(20));
                            entry.setText(iconStr);
                            try {
                                entry.setGraphic(new FontIcon(iconStr));
                                ikonLiterals.add(entry);
                            } catch (IllegalArgumentException | UnsupportedOperationException e) {
                            }
                        }
                    }
                    return null;
                }

                @Override
                protected void updateValue(Label v) {
                    super.updateValue(v);
                    Platform.runLater(() -> {
                        ikonLiterals.add(v);
                    });
                }

            };
            if (ikonLiterals.isEmpty()) {
                PauseTransition pause = new PauseTransition(Duration.millis(3000));
                pause.setOnFinished((t) -> {
                    executor.submit(iconLabelTask);
                });
                pause.play();
            } else {
                executor.submit(iconLabelTask);
            }
        });
        cboxIcons.valueProperty().addListener((o) -> {
            Label get = ikonLiterals.get(cboxIcons.getSelectionModel().getSelectedIndex());
            ikonLiteral.setValue(get.getText());
            placeHolderIcon.setIconLiteral(ikonLiteral.get());
        });
        iconSelBox.getChildren().add(cboxIcons);
        content.getChildren().add(iconSelBox);

        HBox hb2 = new HBox();
        hb2.setSpacing(5);
        hb2.setAlignment(Pos.CENTER_RIGHT);
        hb2.getChildren().add(new Label("Foreground color:"));
        colorPicker = new ColorPicker();
        colorPicker.valueProperty().bindBidirectional(selectedColor);
        selectedColor.addListener((o) -> {
            placeHolderIcon.setIconColor(selectedColor.get());
        });
        hb2.getChildren().add(colorPicker);
        content.getChildren().add(hb2);

        HBox hb3 = new HBox();
        hb3.setSpacing(5);
        hb3.setAlignment(Pos.CENTER);
        hb3.getChildren().add(new Label("Background:"));
        backgroundTransparent = new CheckBox("Transparent");
        backgroundTransparent.setSelected(true);
        hb3.getChildren().add(backgroundTransparent);
        content.getChildren().add(hb3);

        HBox hb4 = new HBox();
        hb4.setSpacing(5);
        hb4.setAlignment(Pos.CENTER_RIGHT);
        hb4.getChildren().add(new Label("Background color:"));
        colorPickerBackground = new ColorPicker();
        colorPickerBackground.setDisable(true);
        colorPickerBackground.valueProperty().addListener((o) -> {
            iconBackgroundBox.setBackground(Background.fill(colorPickerBackground.getValue()));
        });
        backgroundTransparent.selectedProperty().addListener((ov, t, t1) -> {
            if (t == false && t1 == true) {
                colorPickerBackground.setDisable(true);
            }
            if (t == true && t1 == false) {
                colorPickerBackground.setDisable(false);
                iconBackgroundBox.setBackground(Background.fill(colorPickerBackground.getValue()));
            }
        });
        hb4.getChildren().add(colorPickerBackground);
        content.getChildren().add(hb4);

        HBox hbButtons = new HBox();
        hbButtons.setSpacing(5);
        HBox.setMargin(hbButtons, new Insets(10, 10, 10, 10));
        //hbButtons.setMinSize(200, 200);
        hbButtons.setAlignment(Pos.CENTER);
        okButton = new Button("OK");
        okButton.setMinWidth(80);
        cancleButton = new Button("Cancle");
        cancleButton.setMinWidth(80);
        hbButtons.getChildren().add(okButton);
        hbButtons.getChildren().add(cancleButton);
        content.getChildren().add(hbButtons);
        ikonliOver.setContentNode(content);
        cancleButton.setOnAction((t) -> {
            ikonliOver.hide();
        });
        okButton.setOnAction((t) -> {
            ikonliOver.hide();
            iconBackgroundBox.setBackground(Background.EMPTY);
            placeHolderIcon.setIconLiteral(ikonLiteral.get());
            createImageFromIcon();
        });
    }

    private void setupPrefix() {
        iconPrefix.put("org.kordamp.ikonli.antdesignicons.AntDesignIconsFilled", "antf-");
        iconPrefix.put("org.kordamp.ikonli.antdesignicons.AntDesignIconsOutlined", "anto-");
        iconPrefix.put("org.kordamp.ikonli.bootstrapicons.BootstrapIcons", "bi-");        
        iconPrefix.put("org.kordamp.ikonli.bpmn.Bpmn", "bpmn-");
        iconPrefix.put("org.kordamp.ikonli.captainicon.Captainicon", "captainicon-");
        iconPrefix.put("org.kordamp.ikonli.codicons.Codicons", "codicon-");
        iconPrefix.put("org.kordamp.ikonli.dashicons.Dashicons", "dashicons-");
        iconPrefix.put("org.kordamp.ikonli.devicons.Devicons", "di-");
        iconPrefix.put("org.kordamp.ikonli.elusive.Elusive", "eli-");
        iconPrefix.put("org.kordamp.ikonli.entypo.Entypo", "enty-");
        iconPrefix.put("org.kordamp.ikonli.evaicons.Evaicons", "eva-");
        iconPrefix.put("org.kordamp.ikonli.feather.Feather", "fth-");
        iconPrefix.put("org.kordamp.ikonli.fileicons.FileIcons", "file-");
        iconPrefix.put("org.kordamp.ikonli.fluentui.FluentUiFilledAL", "fltfal-");
        iconPrefix.put("org.kordamp.ikonli.fluentui.FluentUiFilledMZ", "fltfmz-");
        iconPrefix.put("org.kordamp.ikonli.fluentui.FluentUiRegularAL", "fltfal-");
        iconPrefix.put("org.kordamp.ikonli.fluentui.FluentUiRegularMZ", "fltfmz-");
        iconPrefix.put("org.kordamp.ikonli.fontawesome.FontAwesome", "fa-");
        iconPrefix.put("org.kordamp.ikonli.fontawesome5.FontAwesomeBrands", "fab-");        
        iconPrefix.put("org.kordamp.ikonli.fontelico.Fontelico", "ftl-");
        iconPrefix.put("org.kordamp.ikonli.foundation.Foundation", "fi-");
        iconPrefix.put("org.kordamp.ikonli.hawcons.HawconsFilled", "hwf-");
        iconPrefix.put("org.kordamp.ikonli.hawcons.HawconsStroke", "hwf-");                        
        iconPrefix.put("org.kordamp.ikonli.jam.Jam", "jam-");
        iconPrefix.put("org.kordamp.ikonli.ligaturesymbols.LigatureSymbols", "lsf-");        
        iconPrefix.put("org.kordamp.ikonli.linecons.Linecons", "li-");
        iconPrefix.put("org.kordamp.ikonli.maki.Maki", "maki-");
        iconPrefix.put("org.kordamp.ikonli.maki2.Maki2", "maki2-");        
        iconPrefix.put("org.kordamp.ikonli.material.Material", "gmi-");        
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignA", "mdi2a-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignB", "mdi2b-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignC", "mdi2c-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignD", "mdi2d-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignE", "mdi2e-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignF", "mdi2f-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignG", "mdi2g-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignH", "mdi2h-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignI", "mdi2i-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignJ", "mdi2j-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignK", "mdi2k-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignL", "mdi2l-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignM", "mdi2m-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignN", "mdi2n-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignO", "mdi2o-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignP", "mdi2p-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignQ", "mdi2q-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignR", "mdi2r-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignS", "mdi2s-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignT", "mdi2t-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignU", "mdi2u-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignV", "mdi2v-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignW", "mdi2w-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignX", "mdi2x-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignY", "mdi2y-");
        iconPrefix.put("org.kordamp.ikonli.materialdesign2.MaterialDesignZ", "mdi2z-");
        iconPrefix.put("org.kordamp.ikonli.medicons.Medicons", "medicon-");        
        iconPrefix.put("org.kordamp.ikonli.microns.Microns", "mu-");
        iconPrefix.put("org.kordamp.ikonli.ociicons.Ociicons", "oci-");
        iconPrefix.put("org.kordamp.ikonli.openiconic.Openiconic", "oi-");
        iconPrefix.put("org.kordamp.ikonli.prestashopicons.PrestaShopIcons", "ps-");
        iconPrefix.put("org.kordamp.ikonli.remixicon.RemixiconAL", "remixal-");
        iconPrefix.put("org.kordamp.ikonli.remixicon.RemixiconMZ", "remixmz-");
        iconPrefix.put("org.kordamp.ikonli.runestroicons.Runestroicons", "ri-");
        iconPrefix.put("org.kordamp.ikonli.simpleicons.SimpleIcons", "si-");
        iconPrefix.put("org.kordamp.ikonli.simplelineicons.SimpleLineIcons", "sil-");
        iconPrefix.put("org.kordamp.ikonli.subway.Subway", "subway-");
        iconPrefix.put("org.kordamp.ikonli.themify.Themify", "ti-");
        iconPrefix.put("org.kordamp.ikonli.typicons.Typicons", "typ-");
        iconPrefix.put("org.kordamp.ikonli.unicons.UniconsLine", "unil-");
        iconPrefix.put("org.kordamp.ikonli.unicons.UniconsMonochrome", "unil-");
        iconPrefix.put("org.kordamp.ikonli.unicons.UniconsSolid", "unil-");
        iconPrefix.put("org.kordamp.ikonli.weathericons.WeatherIcons", "wi-");
        iconPrefix.put("org.kordamp.ikonli.websymbols.Websymbols", "ws-");
        iconPrefix.put("org.kordamp.ikonli.whhg.WhhgAL", "whhgal-");
        iconPrefix.put("org.kordamp.ikonli.whhg.WhhgMZ", "whhgmz-");
        iconPrefix.put("org.kordamp.ikonli.win10.Win10", "win10-");
        iconPrefix.put("org.kordamp.ikonli.zondicons.Zondicons", "zondi-");
    }

    public void createICO(File outFile) {
        try {
            Image img512 = new Image(new FileInputStream(pngString512));
            Image img16 = scaleImage(img512, 512, 512, 16, 16);
            Image img32 = scaleImage(img512, 512, 512, 32, 32);
            Image img64 = scaleImage(img512, 512, 512, 64, 64);
            Image img128 = scaleImage(img512, 512, 512, 128, 128);
            Image img256 = scaleImage(img512, 512, 512, 256, 256);
            List<BufferedImage> imgList = new ArrayList<>();
            imgList.add(SwingFXUtils.fromFXImage(img16, null));
            imgList.add(SwingFXUtils.fromFXImage(img32, null));
            imgList.add(SwingFXUtils.fromFXImage(img64, null));
            imgList.add(SwingFXUtils.fromFXImage(img128, null));
            imgList.add(SwingFXUtils.fromFXImage(img256, null));
            imgList.add(SwingFXUtils.fromFXImage(img512, null));
            boolean[] compress = new boolean[]{true, true, true, true, true, true};

            ICOEncoder.write(imgList, null, compress, outFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createICNS(File outFile) {
        try {
            try ( IcnsBuilder builder = IcnsBuilder.getInstance()) {
                Image img512 = new Image(new FileInputStream(pngString512));

                builder.add(IcnsType.ICNS_16x16_JPEG_PNG_IMAGE, getImageAsStream(img512, 16, 16));
                builder.add(IcnsType.ICNS_32x32_JPEG_PNG_IMAGE, getImageAsStream(img512, 32, 32));
                builder.add(IcnsType.ICNS_64x64_JPEG_PNG_IMAGE, getImageAsStream(img512, 64, 64));
                builder.add(IcnsType.ICNS_128x128_JPEG_PNG_IMAGE, getImageAsStream(img512, 128, 128));
                builder.add(IcnsType.ICNS_256x256_JPEG_PNG_IMAGE, getImageAsStream(img512, 256, 256));
                builder.add(IcnsType.ICNS_512x512_JPEG_PNG_IMAGE, new FileInputStream(pngString512));

                try ( IcnsIcons builtIcons = builder.build()) {
                    FileOutputStream os = new FileOutputStream(outFile);
                    builtIcons.writeTo(os);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createPNG(File outFile) {
        FileInputStream input = null;
        try {
            input = new FileInputStream(pngString512);
            Files.copy(input, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createIOS(File outFile) {
        FileInputStream input = null;
        try {
            input = new FileInputStream(pngString512);
            Image img512 = new Image(new FileInputStream(pngString512));
            Image img180 = scaleImage(img512, 512, 512, 180, 180);
            Image img120 = scaleImage(img512, 512, 512, 120, 120);
            Image img167 = scaleImage(img512, 512, 512, 167, 167);
            Image img152 = scaleImage(img512, 512, 512, 152, 152);
            Image img1024 = scaleImage(img512, 512, 512, 1024, 1024);

            ImageIO.write(SwingFXUtils.fromFXImage(img512, null), "png", new File(outFile.getParent() + File.separator + "icon512.png"));
            ImageIO.write(SwingFXUtils.fromFXImage(img180, null), "png", new File(outFile.getParent() + File.separator + "icon180.png"));
            ImageIO.write(SwingFXUtils.fromFXImage(img120, null), "png", new File(outFile.getParent() + File.separator + "icon120.png"));
            ImageIO.write(SwingFXUtils.fromFXImage(img167, null), "png", new File(outFile.getParent() + File.separator + "icon167.png"));
            ImageIO.write(SwingFXUtils.fromFXImage(img152, null), "png", new File(outFile.getParent() + File.separator + "icon152.png"));
            ImageIO.write(SwingFXUtils.fromFXImage(img1024, null), "png", new File(outFile.getParent() + File.separator + "icon1024.png"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ByteArrayInputStream getImageAsStream(Image source, int targetWidth, int targetHeigth) {
        Image scaledImg = scaleImage(source, 512, 512, targetWidth, targetHeigth);
        BufferedImage bufImage = SwingFXUtils.fromFXImage(scaledImg, null);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufImage, "png", output);
        } catch (IOException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(output.toByteArray(), 0, output.size());
        return in;
    }

    private Image scaleImage(Image img, int srcWidth, int srcHeight, int targetWidth, int targetHeight) {
        byte[] pixelData = new byte[srcWidth * srcHeight * 4];
        img.getPixelReader().getPixels(0, 0, srcWidth, srcHeight, PixelFormat.getByteBgraInstance(), pixelData, 0, srcWidth * 4);
        ByteBuffer buffer = ByteBuffer.wrap(pixelData);
        ByteBuffer scaledBuffer = ImageTools.scaleImage(buffer, srcWidth, srcHeight, 4, targetWidth, targetHeight, true);
        WritableImage scaledImage = new WritableImage(targetWidth, targetHeight);
        scaledImage.getPixelWriter().setPixels(0, 0, targetWidth, targetHeight, PixelFormat.getByteBgraInstance(), scaledBuffer.array(), 0, targetWidth * 4);
        return scaledImage;
    }

    private void createImageList() {
        String tempPath = System.getProperty("java.io.tmpdir") + File.separator + "fxiconcreator";
        File tmp = new File(tempPath);
        if (tmp.exists()) {
            for (File file : tmp.listFiles()) {
                if (!file.isDirectory()) {
                    file.delete();
                }
            }
        } else {
            tmp.mkdirs();
        }
        Image image = imageView.getImage();
        PixelReader reader = image.getPixelReader();
        try {
            File png512 = new File(tempPath + File.separator + "icon512.png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", png512);
            pngString512 = png512.getAbsolutePath();
        } catch (IOException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    @FXML
    private void generateAction(ActionEvent event) {
        dirChooser.setTitle("Select output directory");
        final File result = dirChooser.showDialog(generateButton.getParent().getScene().getWindow());
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, 100);
                createImageList();
                updateProgress(20, 100);
                if (windowsCheckBox.isSelected()) {
                    updateProgress(10, 100);
                    File icoDir = new File(result.getAbsolutePath() + File.separator + "win");
                    icoDir.mkdirs();
                    File icoFile = new File(result.getAbsolutePath() + File.separator + "win" + File.separator + "icon.ico");
                    createICO(icoFile);
                    updateProgress(20, 100);
                }
                if (macosCheckBox.isSelected()) {
                    updateProgress(20, 100);
                    File icnsDir = new File(result.getAbsolutePath() + File.separator + "osx");
                    icnsDir.mkdirs();
                    File icnsFile = new File(result.getAbsolutePath() + File.separator + "osx" + File.separator + "icon.icns");
                    createICNS(icnsFile);
                    updateProgress(40, 100);
                }
                if (linuxCheckBox.isSelected()) {
                    updateProgress(40, 100);
                    File pngDir = new File(result.getAbsolutePath() + File.separator + "linux");
                    pngDir.mkdirs();
                    File pngFile = new File(result.getAbsolutePath() + File.separator + "linux" + File.separator + "icon.png");
                    createPNG(pngFile);
                    updateProgress(60, 100);
                }
                if (androidCheckBox.isSelected()) {
                    updateProgress(60, 100);
                    File pngDir = new File(result.getAbsolutePath() + File.separator + "android");
                    pngDir.mkdirs();
                    File pngFile = new File(result.getAbsolutePath() + File.separator + "android" + File.separator + "icon.png");
                    createPNG(pngFile);
                    updateProgress(70, 100);
                }
                if (iosCheckBox.isSelected()) {
                    updateProgress(70, 100);
                    File pngDir = new File(result.getAbsolutePath() + File.separator + "ios");
                    pngDir.mkdirs();
                    File pngFile = new File(result.getAbsolutePath() + File.separator + "ios" + File.separator + "icon.png");
                    createIOS(pngFile);
                    updateProgress(80, 100);
                }
                updateProgress(100, 100);
                return null;
            }
        };
        task.setOnScheduled((t) -> {
            progressPane.toFront();
            progressPane.setVisible(true);
        });
        task.setOnSucceeded((t) -> {
            progressPane.setVisible(false);
        });
        task.setOnFailed((t) -> {
            progressPane.setVisible(false);
        });
        progressBar.progressProperty().bind(task.progressProperty());
        executor.submit(task);
    }

    @FXML
    private void dropOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            try {
                event.acceptTransferModes(TransferMode.ANY);
                List<File> files = event.getDragboard().getFiles();
                selectedImageFile = files.get(0);
                if (selectedImageFile != null) {
                    Image img = new Image(new FileInputStream(selectedImageFile));
                    if (img.getWidth() != 512) {
                        Alert alert = new Alert(AlertType.ERROR, "Provided image has the wrong with/height (w:" + img.getWidth() + ", h:" + img.getHeight() + ")!\n\nMust be 512 x 512px", ButtonType.OK);
                        alert.showAndWait();
                    } else {
                        imageView.setImage(img);
                        generateButton.setDisable(false);
                        placeHolderIcon.setVisible(false);
                        instructionsLabel.setVisible(false);
                    }
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        event.consume();
    }

    @FXML
    private void selectionButton(ActionEvent event) {
        try {
            fileChooser.setTitle("Select input file");
            fileChooser.setSelectedExtensionFilter(new ExtensionFilter("PNG Image files", "png"));
            selectedImageFile = fileChooser.showOpenDialog(generateButton.getParent().getScene().getWindow());
            if (selectedImageFile != null) {
                Image img = new Image(new FileInputStream(selectedImageFile));
                if (img.getWidth() != 512) {
                    Alert alert = new Alert(AlertType.ERROR, "Provided image has the wrong with/height (w:" + img.getWidth() + ", h:" + img.getHeight() + ")!\n\nMust be 512 x 512px", ButtonType.OK);
                    alert.showAndWait();
                } else {
                    imageView.setImage(img);
                    generateButton.setDisable(false);
                    placeHolderIcon.setVisible(false);
                    instructionsLabel.setVisible(false);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void selectIconFontButtonAction(ActionEvent event) {
        if (imageView.getImage() != null) {
            imageView.setImage(null);
            placeHolderIcon.setVisible(true);
            instructionsLabel.setVisible(true);
            if (iconBackgroundBox.isDisable()) {
                iconBackgroundBox.setBackground(Background.fill(colorPickerBackground.getValue()));
            }
        }
        ikonliOver.show(selectIconFontButton);
    }

    private void createImageFromIcon() {
        WritableImage fontImage = new WritableImage(512, 512);
        SnapshotParameters sp = new SnapshotParameters();
        if (backgroundTransparent.isSelected()) {
            sp.setFill(Paint.valueOf("transparent"));
        } else {
            sp.setFill(colorPickerBackground.getValue());
        }
        placeHolderIcon.snapshot(sp, fontImage);
        imageView.setImage(fontImage);
        generateButton.setDisable(false);
        placeHolderIcon.setVisible(false);
        instructionsLabel.setVisible(false);
    }

    private static class IkonData implements Comparable<IkonData> {

        private String name;
        private IkonProvider ikonProvider;

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(IkonData o) {
            return name.compareTo(o.name);
        }

        static IkonData of(IkonProvider ikonProvider) {
            IkonData ikonData = new IkonData();
            ikonData.ikonProvider = ikonProvider;
            ikonData.name = ikonProvider.getIkon().getSimpleName();
            return ikonData;
        }
    }

}
