package com.example.testbccrapi;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements HttpCallback {

    private TextView precioVenta;
    private TextView precioCompra;

    private TextView usdTextView;
    private TextView crcTextView;

    private double precioDeCompra;
    private double precioDeVenta;

    private EditText calcular;
    private EditText resultadoCalc;
    private boolean isSwapped = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Cambios al action bar
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.redActionBar)));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"#ffe5f5\">" + getString(R.string.app_name) + "</font>"));
        precioVenta = findViewById(R.id.valorVenta);
        precioCompra = findViewById(R.id.valorCompra);

        //Linkeo de atributos
        calcular = findViewById(R.id.calcular);
        resultadoCalc = findViewById(R.id.resultadoCalculo);
        ImageButton swapButton = findViewById(R.id.swapBtn);
        usdTextView = findViewById(R.id.usdTextView);
        crcTextView = findViewById(R.id.crcTextView);

        //Task para obtener precio compra/venta del dolar
        new HttpGetTask(this).execute();

        calcular.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                calcular();
            }
        });

        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                calcular.setText("");
                resultadoCalc.setText("");
                String temp = calcular.getHint().toString();
                calcular.setHint(resultadoCalc.getHint().toString());
                resultadoCalc.setHint(temp);

                String temp2 = crcTextView.getText().toString();
                crcTextView.setText(usdTextView.getText().toString());
                usdTextView.setText(temp2);

                isSwapped = !isSwapped;
                calcular();
            }
        });
    }
    @SuppressLint("SetTextI18n")
    @Override
    public void onValoresObtenidos(String[] valores) {
        if (valores != null && valores.length == 2 && !Objects.equals(valores[1], "Sin internet")) {

            // Formatear el valor de valores[0] con dos decimales
            double precioCompraDecimal = Double.parseDouble(valores[0]);
            precioDeCompra = precioCompraDecimal;
            @SuppressLint("DefaultLocale") String precioCompraFormateado = String.format("%.2f", precioCompraDecimal);

            // Formatear el valor de valores[1] con dos decimales
            double precioVentaDecimal = Double.parseDouble(valores[1]);
            precioDeVenta = precioVentaDecimal;
            @SuppressLint("DefaultLocale") String precioVentaFormateado = String.format("%.2f", precioVentaDecimal);

            // Establecer los valores formateados en sus respectivos TextViews
            precioCompra.setText(precioCompra.getText() + precioCompraFormateado);
            precioVenta.setText(precioVenta.getText() + precioVentaFormateado);

        }else {
            precioCompra.setText(R.string.sin_internet);
            precioVenta.setText(R.string.sin_internet);
            precioCompra.setTextColor(getResources().getColor(R.color.redActionBar));
            precioVenta.setTextColor(getResources().getColor(R.color.redActionBar));
        }
    }

    private void calcular() {
        double cantidad;
        if (!calcular.getText().toString().isEmpty()) {
            cantidad = Double.parseDouble(calcular.getText().toString());
            if (isSwapped) {
                // Realiza el cálculo de dólares a colones
                double resultado = cantidad * precioDeCompra;
                resultadoCalc.setText(String.valueOf(resultado));
            } else {
                // Realiza el cálculo de colones a dólares
                double resultado = cantidad / precioDeVenta;

                resultadoCalc.setText(String.valueOf(resultado));
            }
        }else {
            resultadoCalc.setText("");
        }
    }
}