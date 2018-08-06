
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FXMLMainController {

    @FXML
    private Pane PanelDownload;
    
    @FXML
    private Button btnStopDownload;
            
   @FXML
    private TextField tbNumMedia;

    @FXML
    private AnchorPane panel;

    @FXML
    private TextField tbUrlTred;

    @FXML
    private CheckBox cbImages;

    @FXML
    private CheckBox cbVideo;

    @FXML
    private ComboBox<String> cbSave;
    @FXML
    private Text LabelDownload;

    @FXML
    private ImageView imgClose;

    @FXML
    void Close(MouseEvent event) { System.exit(0);}
    private double xOffset;
    private double yOffset;
    @FXML
            //Mouse move
    void MouseMove(MouseEvent event) {
            Stage stage = (Stage) panel.getScene().getWindow();
            xOffset = stage.getX() - event.getScreenX();
            yOffset = stage.getY() - event.getScreenY();
    }

    @FXML
    void Move(MouseEvent event) {
        Stage stage = (Stage) panel.getScene().getWindow();
        stage.setX(event.getScreenX() + xOffset);
        stage.setY(event.getScreenY() + yOffset);
    }
    @FXML
    void MousePointingCloseNormal(MouseEvent event) {imgClose.setOpacity(1);}
    
      @FXML
    void MousePointingClose(MouseEvent event) { imgClose.setOpacity(0.6);}
    
        @FXML
    void btnStopDownload(MouseEvent event) {
        PanelDownload.setVisible(false);
        btnStopDownload.setVisible(false);
    }
    
    public String GetJson(String urlString){
         try {
             URL url = new URL(urlString);
             HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
             urlConnection.setRequestMethod("GET");
             urlConnection.connect();
             InputStream inputStream = urlConnection.getInputStream();
             StringBuilder buffer = new StringBuilder();
             
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String line;
             while ((line = reader.readLine()) != null) {
                 buffer.append(line);
             }
             return buffer.toString();
         } catch (IOException ex) {
             return null;
         } 
    }
    
    
    
    @FXML
    void initialize() {
        PanelDownload.setVisible(false);
        btnStopDownload.setVisible(false);
        cbSave.getItems().addAll(
            "Видео и картинки в разные папки",
            "Видео и картинки в одну папку");
        // filter tbNum
        tbNumMedia.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                tbNumMedia.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });  
    }
    //Возвращает список того, что нужно скачивать (Фильтрует общий список файлов)
    private List<FileList> FilterFile(List<FileList> ListUrs) {
     List<FileList> ListUr = ListUrs;
     List<FileList> ListUrFilter = new ArrayList<>();
     try{
        for (FileList File : ListUr) {
            if(!cbVideo.isSelected() && (File.fullname.endsWith(".webm") || File.fullname.endsWith(".mp4"))) {continue;}
            if(!cbImages.isSelected() && (File.fullname.endsWith(".png")  || File.fullname.endsWith(".jpeg"))) {continue;}
          if(ConvertIntoNumeric(tbNumMedia.getText()) > 0){
          if(ListUrFilter.size() > (Integer.parseInt(tbNumMedia.getText()) - 1)){ return ListUrFilter;}}
            ListUrFilter.add(File);
        }
     return ListUrFilter;
     }
     catch (Exception ex) { return ListUrFilter;}
    }
    //работает и хуй с ним
    private int ConvertIntoNumeric(String xVal)
    {try{ 
         return Integer.parseInt(xVal);}
     catch(NumberFormatException ex) {
         return 0; 
      }
    }
    
    @FXML // Кнопка 
    void DownLoad(MouseEvent event) throws ParseException, InterruptedException  {
        try {
            if("".equals(tbUrlTred.getText())){
                  Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Укажите ссылку");
            alert.showAndWait();
             return;
            }
            String JsonString = GetJson(tbUrlTred.getText().replace(".html", ".json")) ; //Get string json
            JSONObject jo = (JSONObject)new JSONParser().parse(JsonString);
            String BoardName = (String) jo.get("Board");
            String CurrentThread = (String) jo.get("current_thread");
            List<FileList> ListUr = new ArrayList<>();
            Iterator i = ((JSONArray)( (JSONObject) ((JSONArray) jo.get("threads")).iterator().next()).get("posts")).iterator();
            
            while (i.hasNext()) {
                JSONObject fs = (JSONObject) i.next();
                JSONArray  filesArr= (JSONArray)fs.get("files");
                Iterator iv = filesArr.iterator();
                while (iv.hasNext()) {
                    try{
                        JSONObject slide2 = (JSONObject) iv.next();
                        if(slide2.get("fullname") != null && slide2.get("path") != null){ListUr.add(new FileList(slide2.get("fullname").toString(), "https://2ch.hk" + slide2.get("path").toString()));}
                    }
                    catch (ArithmeticException e) {
                        System.out.println("naxui eto govno");
                    }
                    
                }
            }
            String baseDir = new File(".").getCanonicalPath();
            List<FileList> ListUrFilter = FilterFile(ListUr);
            PanelDownload.setVisible(true);
            LabelDownload.setText(0 + "/" + ListUrFilter.size()); 
             Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        File folderDate = new File(baseDir + "/" + new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime()) + "(" + BoardName + "- Thread " + CurrentThread +")" );
                                if (!folderDate.exists()) {
                                    folderDate.mkdir();
                                }
                            for (FileList File : ListUrFilter) {
                            if(cbSave.getSelectionModel().getSelectedItem() == "Видео и картинки в одну папку"){ 
                            File folder = new File(folderDate.getPath() + "/all");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                             download(File.path, folderDate.getPath() + "/all/");
                            String separator = "/";
                            String results[] = LabelDownload.getText().split(Pattern.quote(separator));
                            int done = Integer.parseInt(results[0]) + 1;
                            Platform.runLater((() -> LabelDownload.setText(done + "/" + ListUrFilter.size()))); 
                            }
                            else{
                                File folder = new File(folderDate.getPath() + "/video");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                folder = new File(folderDate.getPath() + "/Image");
                                if (!folder.exists()) {
                                    folder.mkdir();
                                }
                                
                                if(File.fullname.endsWith(".webm") || File.fullname.endsWith(".mp4")){
                                    download(File.path, folderDate.getPath() + "/video/");
    
                                   String separator = "/";
                                   String results[] = LabelDownload.getText().split(Pattern.quote(separator));
                                   int done = Integer.parseInt(results[0]) + 1;
                                   Platform.runLater((() -> LabelDownload.setText(done + "/" + ListUrFilter.size()))); 
                                }

                                if(File.fullname.endsWith(".jpg") || File.fullname.endsWith(".png")){download(File.path, folderDate.getPath() + "/Image/");
                                   String separator = "/";
                                   String results[] = LabelDownload.getText().split(Pattern.quote(separator));
                                   int done = Integer.parseInt(results[0]) + 1;
                                   Platform.runLater((() -> LabelDownload.setText(done + "/" + ListUrFilter.size()))); 
                                }
                                
                            }
                        }
                       Platform.runLater((() -> LabelDownload.setText("Готово!"))); 
                       btnStopDownload.setVisible(true);
                        return null;
                    }
                };
            }
        };
        service.start();
        //10/10 решение (нет)

        } catch (IOException | ParseException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Упс, ошибка");
            alert.setContentText("Попробуй перезапустить программу, и/или изменить настройки");
            alert.showAndWait();
        }
                
               
    }//DownLoad    
    
    //Скачиваем файл по сылки и сохраняем
    private static Path download(String sourceURL, String targetDirectory) throws IOException
{
    URL url = new URL(sourceURL);
    String fileName = sourceURL.substring(sourceURL.lastIndexOf('/') + 1, sourceURL.length());
    Path targetPath = new File(targetDirectory + File.separator + fileName).toPath();
    Files.copy(url.openStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

    return targetPath;
}
    
    
 // 
}
