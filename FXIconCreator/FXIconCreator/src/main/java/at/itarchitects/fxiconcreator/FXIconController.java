package at.itarchitects.fxiconcreator;

import com.github.gino0631.icns.IcnsBuilder;
import com.github.gino0631.icns.IcnsIcons;
import com.github.gino0631.icns.IcnsType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    private String pngString256;
    private String pngString128;
    private String pngString64;
    private String pngString32;
    private String pngString16;

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

    public FXIconController() {
        dirChooser = new DirectoryChooser();
        fileChooser = new FileChooser();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();        
    }

    public void createICO(File outFile) {
        try {
            Image img16 = new Image(new FileInputStream(pngString16));
            Image img32 = new Image(new FileInputStream(pngString32));
            Image img64 = new Image(new FileInputStream(pngString64));
            Image img128 = new Image(new FileInputStream(pngString128));
            Image img256 = new Image(new FileInputStream(pngString256));
            Image img512 = new Image(new FileInputStream(pngString512));
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
                builder.add(IcnsType.ICNS_16x16_JPEG_PNG_IMAGE, new FileInputStream(pngString16));
                builder.add(IcnsType.ICNS_32x32_JPEG_PNG_IMAGE, new FileInputStream(pngString32));
                builder.add(IcnsType.ICNS_64x64_JPEG_PNG_IMAGE, new FileInputStream(pngString64));
                builder.add(IcnsType.ICNS_128x128_JPEG_PNG_IMAGE, new FileInputStream(pngString128));
                builder.add(IcnsType.ICNS_256x256_JPEG_PNG_IMAGE, new FileInputStream(pngString256));
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

    public void createPNG() {
    }

    private void createImageList() {
        String tempPath = System.getProperty("java.io.tmpdir");
        List<String> inputFiles = new ArrayList<>();
        Image image = imageView.getImage();
        PixelReader reader = image.getPixelReader();
        WritableImage newImage256 = new WritableImage(reader, (int) image.getWidth() / 2, (int) image.getHeight() / 2);
        WritableImage newImage128 = new WritableImage(reader, (int) image.getWidth() / 4, (int) image.getHeight() / 4);
        WritableImage newImage64 = new WritableImage(reader, (int) image.getWidth() / 8, (int) image.getHeight() / 8);
        WritableImage newImage32 = new WritableImage(reader, (int) image.getWidth() / 16, (int) image.getHeight() / 16);
        WritableImage newImage16 = new WritableImage(reader, (int) image.getWidth() / 32, (int) image.getHeight() / 32);
        try {
            File png512 = new File(tempPath + File.pathSeparator + "icon512.png");
            File png256 = new File(tempPath + File.pathSeparator + "icon256.png");
            File png128 = new File(tempPath + File.pathSeparator + "icon128.png");
            File png64 = new File(tempPath + File.pathSeparator + "icon64.png");
            File png32 = new File(tempPath + File.pathSeparator + "icon32.png");
            File png16 = new File(tempPath + File.pathSeparator + "icon16.png");
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", png512);
            ImageIO.write(SwingFXUtils.fromFXImage(newImage256, null), "png", png256);
            ImageIO.write(SwingFXUtils.fromFXImage(newImage128, null), "png", png128);
            ImageIO.write(SwingFXUtils.fromFXImage(newImage64, null), "png", png64);
            ImageIO.write(SwingFXUtils.fromFXImage(newImage32, null), "png", png32);
            ImageIO.write(SwingFXUtils.fromFXImage(newImage16, null), "png", png16);
            pngString512 = png512.getAbsolutePath();
            pngString256 = png256.getAbsolutePath();
            pngString128 = png128.getAbsolutePath();
            pngString64 = png64.getAbsolutePath();
            pngString32 = png32.getAbsolutePath();
            pngString16 = png16.getAbsolutePath();
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
                    updateProgress(30, 100);
                    File icoFile = new File(result.getAbsolutePath() + File.pathSeparator + "win");
                    icoFile.mkdirs();
                    createICO(icoFile);
                    updateProgress(80, 100);
                }
                if (macosCheckBox.isSelected()) {
                    updateProgress(30, 100);
                    File icnsFile = new File(result.getAbsolutePath() + File.pathSeparator + "osx");
                    icnsFile.mkdirs();
                    createICNS(icnsFile);
                    updateProgress(80, 100);
                }
                if (linuxCheckBox.isSelected()) {
                    updateProgress(30, 100);
                    File pngFile = new File(result.getAbsolutePath() + File.pathSeparator + "linux");
                    pngFile.mkdirs();
                    createPNG();
                    updateProgress(80, 100);
                }
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
                    imageView.setImage(img);
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
                imageView.setImage(img);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXIconController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
