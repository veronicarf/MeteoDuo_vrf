package com.romerofernandez.meteoduo;
import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

public class MeteoUtilsTest {

    //===========================================
    //                 celsiusToF
    //===========================================

    // 1º PRUEBA --> intrucimos 0 Cº y debe cambiarlos a 32 Fº
    @Test
    public void celsiusToF_0_is32() {
        Assert.assertEquals(32.0, MeteoUtils.celsiusToF(0), 0.001);
    }

    // 2º PRUEBA --> intrucimos 100 Cº y debe cambiarlos a 212 Fº

    @Test
    public void celsiusToF_100_is212() {
        Assert.assertEquals(212.0, MeteoUtils.celsiusToF(100), 0.001);
    }

    //===========================================
    //                 formatTemp
    //===========================================

    // 3º PRUEBA --> Comprobamos que el texto nos devuelve 10 Cº
    @Test
    public void formatTemp_celsius() {
        Assert.assertEquals("10 °C", MeteoUtils.formatTemp(10, "C"));
    }

    // 4º PRUEBA --> Se valida que el método convierte y formatea correctamente 50ºF
    @Test
    public void formatTemp_fahrenheit() {
        Assert.assertEquals("50 °F", MeteoUtils.formatTemp(10, "F"));
    }

    //===========================================
    //                 CodeTiempo
    //===========================================

    // 5º PRUEBA --> Comprueba que el código 0 devuelve despejado
    @Test
    public void skyFromCode_clear() {
        Assert.assertEquals("Despejado", MeteoUtils.CodeTiempo(0));
    }

    // 6º PRUEBA --> Comprueba que el código 55 devuelve lluvia
    @Test
    public void skyFromCode_rain() {
        Assert.assertEquals("Lluvia", MeteoUtils.CodeTiempo(55));
    }

    // 7º PRUEBA --> Comprueba que el código 96  devuelve tormeta
    @Test
    public void skyFromCode_storm() {
        Assert.assertEquals("Tormenta", MeteoUtils.CodeTiempo(96));
    }

    //===========================================
    //                 safeInt
    //===========================================

    // 8º PRUEBA --> Se verifica que si se intenta acceder a una posición inexistente de un JSONArray,
    // el método devuelve el valor fallback sin provocar errores.
    @Test
    public void safeInt_outOfRange_returnsFallback() throws Exception {
        JSONArray arr = new JSONArray("[1,2,3]");
        Assert.assertEquals(-1, MeteoUtils.safeInt(arr, 10, -1));
    }

    // 9º PRUEBA --> Comprueba que si el array es Null, la app no se cae
    @Test
    public void safeInt_nullArray_returnsFallback() {
        Assert.assertEquals(-1, MeteoUtils.safeInt(null, 0, -1));
    }

    //===========================================
    //                 safeDouble
    //===========================================
    // 10º PRUEBA --> En un array con 2 posiciones , intenta leer la 5º, debe devolver fallback
    @Test
    public void safeDouble_outOfRange_returnsFallback() throws Exception {
        JSONArray arr = new JSONArray("[1.5, 2.5]");
        Assert.assertEquals(-1.0, MeteoUtils.safeDouble(arr, 5, -1.0), 0.001);
    }


}
