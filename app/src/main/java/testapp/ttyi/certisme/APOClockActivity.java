package testapp.ttyi.certisme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class APOClockActivity extends AppCompatActivity {

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apoclock);

        readClkXML();

        Button clkIn_btn = (Button) findViewById(R.id.clockIn_btn);
        Button clkOut_btn = (Button) findViewById(R.id.clockOut_btn);
        Button next_btn = (Button) findViewById(R.id.next_btn);

        if(!checkClkOut()) {
            clkIn_btn.setEnabled(false);
            clkOut_btn.setEnabled(true);
        }
        else {
            clkIn_btn.setEnabled(true);
            clkOut_btn.setEnabled(false);
        }

        clkIn_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd    HH:mm:ss");

                Date now = new Date();
                String clkInTD = formatter.format(now);

                // Condition to check if the person has checked out yet?
                if (checkClkOut()) clockedIn(clkInTD);
                else {
                    new AlertDialog.Builder(APOClockActivity.this)
                            .setTitle("Not Clocked Out")
                            .setMessage("You have not clocked out yet from your previous session.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
                Log.d("MyLog", "Pass here 1");

            }

        });

        clkOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd    HH:mm:ss");
                Date now = new Date();
                String clkOutTD = formatter.format(now);

                if (!checkClkOut()) clockedOut(clkOutTD);
                else {
                    new AlertDialog.Builder(APOClockActivity.this)
                            .setTitle("Already Clocked Out")
                            .setMessage("You have already clocked out.")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }

                // TODO: Check if user has already clocked in/haven't clocked out
                // TODO: Overwrite ClkIn1, move previous ClkIn1 to ClkIn2 and ClkOut1 to ClkOut2 (ClkOut1 become NULl)

            }

        });

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

                Intent intent = new Intent(APOClockActivity.this, QRTutActivity.class);
                intent.putExtra("EXTRA_FIN", "A1234567A"); // Hardcoded FIN just for the demo XD
                startActivity(intent);

            }

        });

    }

    public boolean checkClkOut() {

        Log.d("MyLog", "Pass here checkClkOut()");

        String clkO1 = null;

        try {

            String filename = "clk.xml";
            String path = "/storage/sdcard/CertIsMe/" + filename;
            File f2 = new File(path);  //
            Uri xmlUri = Uri.fromFile(f2);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f2);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("clock");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    clkO1 = eElement.getElementsByTagName("ClkOut1").item(0).getTextContent();

                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }

        return !clkO1.equals("0");

    }

    public void readClkXML() {

        Log.d("MyLog", "Pass here readClKXML()");

        String a = null, b = null, c = null, d = null, e = null, f = null;

        try {

            String filename = "clk.xml";
            String path = "/storage/sdcard/CertIsMe/" + filename;
            File f2 = new File(path);  //
            Uri xmlUri = Uri.fromFile(f2);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f2);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("clock");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    //System.out.println("Staff id : " + eElement.getAttribute("id"));
                    //System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    //System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    //System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
                    //System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
                    a = eElement.getElementsByTagName("ClkIn1").item(0).getTextContent();
                    b = eElement.getElementsByTagName("ClkOut1").item(0).getTextContent();
                    c = eElement.getElementsByTagName("ClkIn2").item(0).getTextContent();
                    d = eElement.getElementsByTagName("ClkOut2").item(0).getTextContent();
                    e = eElement.getElementsByTagName("ClkIn3").item(0).getTextContent();
                    f = eElement.getElementsByTagName("ClkOut3").item(0).getTextContent();


                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }
        TextView txtView = (TextView) findViewById(R.id.lastClkIn_textView);
        txtView.setText("Last Attendance Record:" +
                "\nClock In    :   " + a +
                "\nClock Out :   " + b +
                "\n\n\nPrevious Attendance Record:" +
                "\nClock In    :   " + c +
                "\nClock Out :   " + d +
                "\n\n\nPrevious Attendance Record:" +
                "\nClock In    :   " + e +
                "\nClock Out :   " + f);

    }

    public void clockedIn(String clkInTime) {

        String a = null, b = null, c = null, d = null, e = null, f = null;
        Button clkIn_btn = (Button) findViewById(R.id.clockIn_btn);
        Button clkOut_btn = (Button) findViewById(R.id.clockOut_btn);

        try {

            String filename = "clk.xml";
            String path = "/storage/sdcard/CertIsMe/" + filename;
            File f2 = new File(path);  //
            Uri xmlUri = Uri.fromFile(f2);

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f2);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("clock");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                    Element eElement = (Element) nNode;

                    //System.out.println("Staff id : " + eElement.getAttribute("id"));
                    //System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
                    //System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
                    //System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
                    //System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());
                    a = eElement.getElementsByTagName("ClkIn1").item(0).getTextContent();
                    b = eElement.getElementsByTagName("ClkOut1").item(0).getTextContent();
                    c = eElement.getElementsByTagName("ClkIn2").item(0).getTextContent();
                    d = eElement.getElementsByTagName("ClkOut2").item(0).getTextContent();


                }
            }
        } catch (Exception e4) {
            e4.printStackTrace();
        }

        try {

            // http://stackoverflow.com/questions/8296182/how-to-get-the-uri-of-a-image-stored-on-the-sdcard
            String filename = "clk.xml";
            String path = "/storage/sdcard/CertIsMe/" + filename;
            File f5 = new File(path);  //
            Uri xmlUri = Uri.fromFile(f5);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(f5);

            Log.d("MyLog", "Pass here 3");

            // Get the root element
            Node clock = doc.getFirstChild();

            // Get the staff element , it may not working if tag has spaces, or
            // whatever weird characters in front...it's better to use
            // getElementsByTagName() to get it directly.
            // Node staff = company.getFirstChild();

            // Get the staff element by tag name directly
            //Node staff = doc.getElementsByTagName("ClkIn1").item(0);

            // loop the staff child node
            NodeList list = clock.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {

                Node node = list.item(i);

                // get the salary element, and update the value
                if ("ClkIn1".equals(node.getNodeName())) {
                    node.setTextContent(clkInTime);
                }
                if ("ClkOut1".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }
                if ("ClkIn2".equals(node.getNodeName())) {
                    node.setTextContent(a);
                }
                if ("ClkOut2".equals(node.getNodeName())) {
                    node.setTextContent(b);
                }
                if ("ClkIn3".equals(node.getNodeName())) {
                    node.setTextContent(c);
                }
                if ("ClkOut3".equals(node.getNodeName())) {
                    node.setTextContent(d);
                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        }

        Log.d("MyLog", "Pass here 4");
        readClkXML();

        clkIn_btn.setEnabled(false);
        clkOut_btn.setEnabled(true);


    }

    public void clockedOut(String clkOutTime) {

        Log.d("MyLog", "Pass here 2");
        Button clkIn_btn = (Button) findViewById(R.id.clockIn_btn);
        Button clkOut_btn = (Button) findViewById(R.id.clockOut_btn);

        try {

            // http://stackoverflow.com/questions/8296182/how-to-get-the-uri-of-a-image-stored-on-the-sdcard
            String filename = "clk.xml";
            String path = "/storage/sdcard/CertIsMe/" + filename;
            File f = new File(path);  //
            Uri xmlUri = Uri.fromFile(f);

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(f);

            // Get the root element
            Node clock = doc.getFirstChild();

            // Get the staff element , it may not working if tag has spaces, or
            // whatever weird characters in front...it's better to use
            // getElementsByTagName() to get it directly.
            // Node staff = company.getFirstChild();

            // Get the staff element by tag name directly
            //Node staff = doc.getElementsByTagName("ClkIn1").item(0);

            // loop the staff child node
            NodeList list = clock.getChildNodes();

            for (int i = 0; i < list.getLength(); i++) {

                Node node = list.item(i);

                // get the salary element, and update the value
                if ("ClkOut1".equals(node.getNodeName())) {
                    node.setTextContent(clkOutTime);
                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);


        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        }

        Log.d("MyLog", "Pass here 4");
        readClkXML();

        clkIn_btn.setEnabled(true);
        clkOut_btn.setEnabled(false);

    }

}

/* All credits go to:
http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
+ my own code to check dat XML
 */
