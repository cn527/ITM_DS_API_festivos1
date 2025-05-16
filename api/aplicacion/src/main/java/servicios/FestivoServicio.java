package com.festivos.api.aplicacion.servicios;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.festivos.api.dominio.entidades.Festivo;
import com.festivos.api.infraestructura.repositorios.IFestivoRepositorio;
import com.festivos.api.core.servicios.IFestivoServicio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.ParseException;

@Service
public class FestivoServicio implements IFestivoServicio {

    @Autowired
    private IFestivoRepositorio repositorio;

    @Override
    public String validarFecha(Date fecha) {
        if (fecha == null) {
            return "La fecha ingresada no es válida.";
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int anio = cal.get(Calendar.YEAR);

        List<Festivo> festivos = repositorio.obtenerTodos();

        for (Festivo festivo : festivos) {
            Date fechaFestivo = calcularFechaFestivo(festivo, anio);
            if (fechaFestivo != null && mismaFecha(fecha, fechaFestivo)) {
                return formatearFecha(fecha) + " es festivo: " + festivo.getNombre();
            }
        }

        return formatearFecha(fecha) + " no es festivo";
    }

    @Override
    public Date calcularFechaFestivo(Festivo festivo, int anio) {
        Date fecha;
        switch (festivo.getTipo().getId()) {
            case 1: // Festivo de fecha fija
                fecha = new Date(anio - 1900, festivo.getMes() - 1, festivo.getDia());
                break;
            case 2: // Festivo que se traslada al siguiente lunes
                fecha = new Date(anio - 1900, festivo.getMes() - 1, festivo.getDia());
                fecha = getSiguienteLunes(fecha);
                break;
            case 3: // Festivo relacionado con la Semana Santa
                Date pascua = getInicioSemanaSanta(anio);
                fecha = agregarDias(pascua, festivo.getDiasPascua());
                break;
            case 4: // Festivo relacionado con la Semana Santa que se traslada al siguiente lunes
                Date pascuaBase = getInicioSemanaSanta(anio);
                Date fechaRelativa = agregarDias(pascuaBase, festivo.getDiasPascua());
                fecha = getSiguienteLunes(fechaRelativa);
                break;
            default:
                fecha = null;
        }
        return fecha;
    }

    @Override
    public boolean mismaFecha(Date f1, Date f2) {
        if (f1 == null || f2 == null)
            return false;
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(f1);
        c2.setTime(f2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
    }

    @Override
    public String formatearFecha(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        int dia = cal.get(Calendar.DAY_OF_MONTH);
        int mes = cal.get(Calendar.MONTH) + 1;
        int anio = cal.get(Calendar.YEAR);
        return String.format("%02d de %s de %d", dia, nombreMes(mes), anio);
    }

    @Override
    public String nombreMes(int mes) {
        String[] meses = { "enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre" };
        return meses[mes - 1];
    }

    @Override
    public Date getInicioSemanaSanta(int año) {
        // Algoritmo de Gauss para calcular la fecha de Pascua
        int a = año % 19;
        int b = año / 100;
        int c = año % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        
        return new Date(año - 1900, mes - 1, dia);
    }

    @Override
    public Date agregarDias(Date fecha, int dias) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        calendario.add(Calendar.DATE, dias);
        return calendario.getTime();
    }

    @Override
    public Date getSiguienteLunes(Date fecha) {
        Calendar calendario = Calendar.getInstance();
        calendario.setTime(fecha);
        
        int diaSemana = calendario.get(Calendar.DAY_OF_WEEK);
        if (diaSemana == Calendar.MONDAY) {
            return fecha; // Ya es lunes, no hay cambio
        }
        
        // Calcular días hasta el próximo lunes
        int diasHastaLunes = Calendar.MONDAY - diaSemana;
        if (diasHastaLunes <= 0) {
            diasHastaLunes += 7; // Si es después del lunes, ir al siguiente
        }
        
        return agregarDias(fecha, diasHastaLunes);
    }

    @Override
    public Date strToDate(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);

        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            System.err.println("Fecha inválida: " + date);
            return null;
        }
    }
}
