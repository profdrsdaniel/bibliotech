package br.ulbra.bibliotech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicacao BiblioTech quando executada como servico REST.
 *
 * <p>Sobe um servidor web embutido que expoe os endpoints da API. As regras de
 * negocio continuam nas camadas de dominio, regras, servico e repositorio, que
 * sao Java puro e nao dependem do Spring.</p>
 */
@SpringBootApplication
public class BibliotechApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotechApplication.class, args);
    }
}
