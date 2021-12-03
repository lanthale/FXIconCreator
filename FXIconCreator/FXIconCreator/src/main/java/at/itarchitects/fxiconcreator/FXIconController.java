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
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import net.sf.image4j.codec.ico.ICOEncoder;
import org.kordamp.ikonli.javafx.FontIcon;

public class FXIconController implements Initializable {

    private final FileChooser fileChooser = new FileChooser();
    private FontIcon iconview;
    private FontIcon iconviewDelete;
    private UtilityTools util;
    private ExecutorService executor;

    private ResourceBundle resources;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        executor = Executors.newSingleThreadExecutor();
    }    

    public void createICO() {
        try {            
            Image img16 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-16.png"));
            Image img32 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-32.png"));
            Image img64 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-64.png"));
            Image img128 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-128.png"));
            Image img256 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-256.png"));
            Image img512 = new Image(new FileInputStream("/Users/selfemp/Desktop/papa2/qr-512.png"));
            List<BufferedImage> imgList = new ArrayList<>();
            imgList.add(SwingFXUtils.fromFXImage(img16, null));
            imgList.add(SwingFXUtils.fromFXImage(img32, null));
            imgList.add(SwingFXUtils.fromFXImage(img64, null));
            imgList.add(SwingFXUtils.fromFXImage(img128, null));
            imgList.add(SwingFXUtils.fromFXImage(img256, null));
            imgList.add(SwingFXUtils.fromFXImage(img512, null));
            boolean[] compress = new boolean[]{true, true, true, true, true, true};

            ICOEncoder.write(imgList, null, compress, new File("/Users/selfemp/Desktop/papa2/qr.ico"));            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void createICNS() {
        try {
            try ( IcnsBuilder builder = IcnsBuilder.getInstance()) {
                builder.add(IcnsType.ICNS_16x16_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-16.png"));
                builder.add(IcnsType.ICNS_32x32_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-32.png"));
                builder.add(IcnsType.ICNS_64x64_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-64.png"));
                builder.add(IcnsType.ICNS_128x128_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-128.png"));
                builder.add(IcnsType.ICNS_256x256_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-256.png"));
                builder.add(IcnsType.ICNS_512x512_JPEG_PNG_IMAGE, new FileInputStream("/Users/selfemp/Desktop/papa2/qr-512.png"));

                try ( IcnsIcons builtIcons = builder.build()) {
                    FileOutputStream os = new FileOutputStream("/Users/selfemp/Desktop/papa2/qr.icns", false);
                    builtIcons.writeTo(os);
                }
            }            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
    
    

}
