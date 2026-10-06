package br.com.gojopurin.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // Fixa o fuso horario de Sao Paulo. Sem isso, o servidor na nuvem grava
        // as datas dos pedidos no horario de Londres (3 horas a frente).
        TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
        SpringApplication.run(BackendApplication.class, args);
    }
}
