package in.gvc;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.xuggle.xuggler.video.ConverterFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Created by arpit on 29/5/17.
 */
public class Controller {
    Webcam webcam;
    public int clicks = 0;
    Dimension size = WebcamResolution.QVGA.getSize();
    WebcamPanel panel;

    public Thread click_checker;

    JButton btSnapMe;

    public boolean loop = true;
    public java.util.List<Webcam> webcamList;

    static final NewJFrame newJFrame = new NewJFrame();

    Controller()
    {
        click_checker = new Thread(click_check_runnable);
        click_checker.start();

        webcamList = Webcam.getWebcams();

        btSnapMe = new JButton("Snapshot");
        btSnapMe.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                clicks++;
            }
        });
    }

    public JPanel getCameraPanel()
    {
        return panel;
    }
    public JButton getSnapButton()
    {
        return btSnapMe;
    }

    public static void main(String arg[])
    {
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {


                final Controller controller = new Controller();

                newJFrame.jTextArea1.setLineWrap(true);
                newJFrame.jComboBox1.removeAllItems();
                for(Webcam web : controller.webcamList)
                {
                    newJFrame.jComboBox1.addItem(web.getName());
                }

                controller.webcam = controller.webcamList.get(0);
                controller.webcam.setViewSize(controller.size);

                controller.panel = new WebcamPanel(controller.webcam, controller.size, false);
                controller.panel.setFillArea(true);
                controller.panel.start();


                newJFrame.jPanel1.setLayout(new BorderLayout());
                newJFrame.jPanel1.add(controller.getCameraPanel(),BorderLayout.CENTER);
                newJFrame.jButton2.setText("SnapShot");
                newJFrame.jButton2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {

                        controller.clicks++;
                        newJFrame.jTextArea1.setText("Please wait your image is being processed....");
                        newJFrame.jButton2.setEnabled(false);
                    }
                });
                newJFrame.setTitle("Image Recognizer");
                newJFrame.jButton1.setText("New Photo");
                newJFrame.jButton1.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {

                        controller.panel.resume();
                        newJFrame.jButton2.setEnabled(true);
                    }
                });

                newJFrame.jComboBox1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        int index = newJFrame.jComboBox1.getSelectedIndex();
                        controller.panel = new WebcamPanel(controller.webcamList.get(index), controller.size, false);
                        controller.panel.setFillArea(true);
                        controller.panel.start();
                    }
                });

                newJFrame.setVisible(true);

                Image image = null;
                URL url = null;
                try {
                    url = new URL("https://www.google.com/a/gvc.in/images/logo.gif");
                    image = ImageIO.read(url);
                    JLabel background = new JLabel(new ImageIcon(image));
                    newJFrame.jPanel2.setLayout(new BorderLayout());
                    newJFrame.jPanel2.add(background,BorderLayout.NORTH);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void capture() throws IOException, InterruptedException {

        panel.pause();
        BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
        HttpClient httpClient = new DefaultHttpClient();
        try
        {
            URIBuilder uriBuilder = new URIBuilder("https://southeastasia.api.cognitive.microsoft.com/vision/v1.0/ocr?");

            uriBuilder.setParameter("language", "en");
            uriBuilder.setParameter("detectOrientation ", "true");

            URI uri = uriBuilder.build();
            HttpPost request = new HttpPost(uri);

            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "070f08e4ebe04af78077ff6822d4d862");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            ByteArrayEntity reqEntity = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);

            request.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                String str1 = EntityUtils.toString(entity);



                str="";
                JSONArray array = new JSONArray();
                array.put(0,str1);
                parseit(array,"regions");
                str+="\n";
                //label.setText(str);
                newJFrame.jTextArea1.setText(str);

            }



            SendMail.SendSimpleMessage(bytes,str);

        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }
    static String str="";
    public static void parseit(JSONArray array, String key)
    {
        for(int i=0;i<array.length();i++)
        {
            //System.out.println(array.get(i).toString());
            JSONObject obj = new JSONObject(array.get(i).toString());
            if(obj.has(key))
            {
                JSONArray temp_array = obj.getJSONArray(key);
                if(key.equals("regions"))
                    parseit(temp_array,"lines");
                else if(key.equals("lines"))
                    parseit(temp_array,"words");
                else if(key.equals("words")) {

                    for(int j=0;j<temp_array.length();j++)
                    {
                        JSONObject obj1 = new JSONObject(temp_array.get(j).toString());
                        //System.out.print(obj1.getString("text")+" ");
                        str+=obj1.getString("text")+" ";
                    }str+="\n";

                }
            }
        }
    }


    Runnable click_check_runnable = new Runnable() {
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException Ee) {
                }
                if (clicks > 0) {
                    try {
                        capture();
                        //System.out.println("got a click");
                        clicks--;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };
}
