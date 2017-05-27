package in.gvc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;


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


public class NewEncoder extends JFrame {
    Webcam webcam;
    public int clicks = 0;
    Dimension size = WebcamResolution.QVGA.getSize();

    public Thread click_checker;

    public boolean loop = true;

    public JButton btSnapMe = new JButton("SnapShot");

    NewEncoder() throws IOException, InterruptedException {

        webcam = Webcam.getWebcams().get(1);
        webcam.setViewSize(size);
        //webcam.open(true);

        /*String input = "";
        Scanner scan = new Scanner(System.in);
        while (!input.equals("stop"))
        {
            input = scan.next();
            if (input.equals("capture"))
            {
                capture();
            }
        }*/

        /*SerialPort comPort = SerialPort.getCommPorts()[0];
        comPort.openPort();*/

        WebcamPanel panel = new WebcamPanel(webcam, size, false);
        panel.setFillArea(true);

        setTitle("Video Capture");
        setLayout(new FlowLayout());
        panel.start();
        add(panel);

        add(btSnapMe);

        JLabel label = new JLabel("some text");

        add(label);
        pack();
        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                loop = false;
                e.getWindow().dispose();
                System.exit(0);
            }
        });

        click_checker = new Thread(click_check_runnable);
        click_checker.start();

        btSnapMe.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                clicks++;
            }
        });


        // *050,62,62#
        /*InputStream in = comPort.getInputStream();
        try
        {
            while(true)
            {
                while(comPort.bytesAvailable() == 0)
                    Thread.sleep(200);
                String str="";
                int j=0;
                for (j=0;j<11;j++)
                {
                    str += ((char) in.read());
                }

                System.out.println(str);
                StringBuilder builder = new StringBuilder(str);
                if( !(builder.substring(0,1).equals("*") &&
                        builder.substring(builder.length()-1,builder.length()).equals("#")))
                {
                    continue;
                }

                String speed = builder.subSequence(1,builder.indexOf(",")).toString();
                builder.delete(0, builder.indexOf(",")+1);
                String lat = builder.substring(0,builder.indexOf(",")).toString();
                builder.delete(0,builder.indexOf(",")+1);
                String lon = builder.substring(0,builder.length()-1);

                if(Float.parseFloat(speed) > 80)
                {
                    btSnapMe.doClick();
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
        in.close();
        comPort.closePort();*/


    }

    public static void main(String[] args) throws IOException, InterruptedException {

        new NewEncoder();

    }

    public void capture() throws IOException, InterruptedException {


        BufferedImage image = ConverterFactory.convertToType(webcam.getImage(), BufferedImage.TYPE_3BYTE_BGR);
        HttpClient httpClient = new DefaultHttpClient();
        try
        {
            URIBuilder uriBuilder = new URIBuilder("https://southeastasia.api.cognitive.microsoft.com/vision/v1.0/ocr?");

            uriBuilder.setParameter("language", "en");
            uriBuilder.setParameter("detectOrientation ", "true");

            URI uri = uriBuilder.build();
            HttpPost request = new HttpPost(uri);

            // Request headers - replace this example key with your valid subscription key.
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "070f08e4ebe04af78077ff6822d4d862");

            // Request body. Replace the example URL with the URL of a JPEG image containing text.
            /*tringEntity requestEntity = new StringEntity("{\"url\":\"http://img1.exportersindia.com/product_images/bc-full/dir_34/1015772/aluminium-number-plate-01-298087.jpg\"}");
            */
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            ByteArrayEntity reqEntity = new ByteArrayEntity(bytes, ContentType.APPLICATION_OCTET_STREAM);


            /*MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            File file = new File("temp.jpeg");
            ImageIO.write(image, "jpeg", file);
            multipartEntity.addPart("file", new FileBody(file));*/

            request.setEntity(reqEntity);

            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                String str = EntityUtils.toString(entity);


                JSONArray array = new JSONArray();
                array.put(0,str);
                parseit(array,"regions");
                System.out.println();

            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

    }

    public void parseit(JSONArray array,String key)
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
                    System.out.println();
                    for(int j=0;j<temp_array.length();j++)
                    {
                        JSONObject obj1 = new JSONObject(temp_array.get(j).toString());
                        System.out.print(obj1.getString("text")+" ");
                    }
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
                        clicks--;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }
    };
}