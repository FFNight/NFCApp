package testapp.ttyi.certisme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
import org.xmlpull.v1.XmlSerializer;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

// Chen yang
public class APOClockActivity extends AppCompatActivity {

    private XmlPullParserFactory xmlFactoryObject;
    public volatile boolean parsingComplete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apoclock);

       if(!fileExistance("clk.xml")) {
           Log.d("MyLog", "Enter createClkXML()");
           createClkXML();
           Log.d("MyLog", "Back from createClkXML()");
       };


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

            FileInputStream inFile = openFileInput("clk.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inFile);

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

            FileInputStream inFile = openFileInput("clk.xml");

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            //Document doc = dBuilder.parse(f2); //Original
            Document doc = dBuilder.parse(inFile);

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

            //FileOutputStream inFile = openFileOutput("clk.xml", Context.MODE_APPEND);


            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(this.openFileInput("clk.xml"));

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

            FileInputStream inFile = openFileInput("clk.xml");

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.openFileInput("clk.xml"));

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
            StreamResult result = new StreamResult(this.openFileOutput("clk.xml", Context.MODE_PRIVATE));
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
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.openFileInput("clk.xml"));

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
            StreamResult result = new StreamResult(this.openFileOutput("clk.xml", Context.MODE_PRIVATE));
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

    public boolean fileExistance(String fname){
        File file = getBaseContext().getFileStreamPath(fname);
        return file.exists();
    }

    public void createClkXML() {

        String a = null, b = null, c = null, d = null, e = null, f = null;

        try {

            Log.d("MyLog", "Enter here");
            // Create new file I think
            /*
            File file = new File("clk.xml");

            file.getParentFile().mkdirs();
            file.createNewFile();
            Log.d("MyLog", "After file.createNewFile()");
            */

            //File file = new File("data/data/testapp.ttyi.certisme/files/clk.xml"); // Only works on Ting Yi's PC for some reason
            File file = new File(this.getFilesDir(), "clk.xml"); // Use this as it works on Chen Yang's PC (integrated)
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e00) {
                    e00.printStackTrace();
                }
            }

            FileOutputStream stream = new FileOutputStream(file);
            try {
                stream.write(("<?xml version=\"1.0\"?>\n" +
                        " <clock>\n" +
                        "     <ClkIn1>DATE 1</ClkIn1>\n" +
                        "     <ClkOut1>DATE 2</ClkOut1>\n" +
                        "     <ClkIn2>DATE 1</ClkIn2>\n" +
                        "     <ClkOut2>DATE 2</ClkOut2>\n" +
                        "     <ClkIn3>DATE 1</ClkIn3>\n" +
                        "     <ClkOut3>DATE 2</ClkOut3>\n" +
                        " </clock> ").getBytes());
                Log.d("MyLog", "File written successfully???");
            } finally {
                stream.close();
            }

            Log.d("MyLog", "Pass try block");

            // http://stackoverflow.com/questions/8296182/how-to-get-the-uri-of-a-image-stored-on-the-sdcard

            /*
            FileInputStream inFile = openFileInput("clk.xml");

            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(this.openFileInput("clk.xml"));

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
                    node.setTextContent("0");
                }
                if ("ClkOut1".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }
                if ("ClkIn2".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }
                if ("ClkOut2".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }
                if ("ClkIn3".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }
                if ("ClkOut3".equals(node.getNodeName())) {
                    node.setTextContent("0");
                }

            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(this.openFileOutput("clk.xml", Context.MODE_PRIVATE));
            transformer.transform(source, result);

        */

        } catch (IOException ioe) {
            ioe.printStackTrace();
            Log.d("MyLog", "IOException occurred");
        }


    }



}

/* All credits go to:
http://www.mkyong.com/java/how-to-modify-xml-file-in-java-dom-parser/
http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
+ my own code to check dat XML
 */
