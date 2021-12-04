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
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javax.imageio.ImageIO;
import net.sf.image4j.codec.ico.ICOEncoder;
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

    public FXIconController() {
        dirChooser = new DirectoryChooser();
        fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();
        generateButton.setDisable(true);
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
        progressPane.setVisible(true);
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
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
