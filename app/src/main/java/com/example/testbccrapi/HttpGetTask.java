package com.example.testbccrapi;

import android.os.AsyncTask;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HttpGetTask extends AsyncTask<Void, Void, String[]> {

    private static final String TAG = "HttpGetTask";

    private HttpCallback callback;

    public HttpGetTask(HttpCallback callback) {
        this.callback = callback;
    }

    @Override
    protected String[] doInBackground(Void... params) {

        long ahora = System.currentTimeMillis();
        Date fecha = new Date(ahora);

        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String salida = df.format(fecha);

        String precioCompra = "https://gee.bccr.fi.cr/Indicadores/Suscripciones/WS/wsindicadoreseconomicos.asmx/ObtenerIndicadoresEconomicos?Indicador=317&FechaInicio=" + salida + "&FechaFinal=" + salida +" &Nombre=Santiago&SubNiveles=N&CorreoElectronico=Santiago7013@gmail.com&Token=GN3M12IA3N";

        String precioVenta = "https://gee.bccr.fi.cr/Indicadores/Suscripciones/WS/wsindicadoreseconomicos.asmx/ObtenerIndicadoresEconomicos?Indicador=318&FechaInicio=" + salida + "&FechaFinal=" + salida +"&Nombre=Santiago&SubNiveles=N&CorreoElectronico=Santiago7013@gmail.com&Token=GN3M12IA3N";

        String valor1 = obtenerValor(precioCompra);
        String valor2 = obtenerValor(precioVenta);
        return new String[] { valor1, valor2 };
    }

    private String obtenerValor(String urlString) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String xmlString = null;

        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            xmlString = buffer.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error ", e);
            return "Sin internet";
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream", e);
                }
            }
        }
        return parsearXML(xmlString);
    }

    @Override
    protected void onPostExecute(String[] valores) {
        super.onPostExecute(valores);
        if (valores != null) {
            callback.onValoresObtenidos(valores);
        }
    }

    private String parsearXML(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("NUM_VALOR");

            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    return element.getTextContent();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear XML", e);
        }
        return null;
    }
}
