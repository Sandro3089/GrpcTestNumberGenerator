package com.baliasnikov.grpctestnumbergenerator.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Основной (базовый) класс приложения - сервера GRPC.
 *
 * @author Aleksandr Baliasnikov
 */
@SpringBootApplication
public class GrpcTestNumberGeneratorApplication {

	/**
	 * Запуск приложения - сервера GRPC.
	 * @param args Аргументы командной строки.
	 */
	public static void main(String[] args) {
		SpringApplication.run(GrpcTestNumberGeneratorApplication.class, args);
	}

}
