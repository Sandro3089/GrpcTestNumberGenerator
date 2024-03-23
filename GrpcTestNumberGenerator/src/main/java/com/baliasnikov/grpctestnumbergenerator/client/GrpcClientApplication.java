package com.baliasnikov.grpctestnumbergenerator.client;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import com.baliasnikov.grpctestnumbergenerator.client.impl.GrpcNumberClientImpl;

/**
 * Запуск приложения тестового клиента GRPC.
 *
 * @author Aleksandr Baliasnikov
 */
@Slf4j
public class GrpcClientApplication {

    /**
     * Метод для запуска клиента.
     * @param args Аргументы командной строки.
     */
    public static void main(String[] args) {
        // Адрес для подключения
        val target = "localhost:7777";
        // Создаем канал связи, без SSL/TLS
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        val client = new GrpcNumberClientImpl(channel);
        try {
            val firstValue = 0;
            val lastValue = 30;
            // Генерация новой последовательности чисел
            client.generateNewSequence(firstValue, lastValue);
            val counter = 50;
            var currentNumber = 0;
            var lastServerNumber = 0;
            log.info("Start log numbers");
            // Старт просулшивания стрима сервера
            client.serverStreamAsyncStub();
            // Цикл вывода в лог текущего числа раз в секунду от 0 до 50
            for (int i = 0; i < counter; i++) {
                val serverNumber = client.getCurrentNumber();
                // Если последнее число от сервера не менялось, выводим в лог "текущее число + 1",
                // Иначе - "Текущее число + Последнее число сервера + 1"
                currentNumber = lastServerNumber == serverNumber ?
                        currentNumber + 1 :
                        currentNumber + client.getCurrentNumber() + 1;
                lastServerNumber = serverNumber;
                log.info("CurrentNumber: {}", currentNumber);
                Thread.sleep(1000);
            }
            log.info("Finish log numbers");
            // Ожидание завершения стрима сервера
            if (!client.getServerStreamLatch().await(1, TimeUnit.MINUTES))
                log.info("Server stream timeout expired, finish listening");
        } catch (Exception ex) {
            log.error(ex.getMessage());
        } finally {
            // Закрываем канал связи в конце работы
            channel.shutdown();
            log.info("Grpc channel shutdown");
        }
    }
}
