package com.baliasnikov.grpctestnumbergenerator.client;

import java.util.concurrent.CountDownLatch;

import io.grpc.Channel;

/**
 * Интерфейс клиента GRPC.
 *
 * @author Aleksandr Baliasnikov
 */
public interface GrpcNumberClient {

    /**
     * Запрос на генерацию новой последовательности чисел для стриминга.
     * @param firstValue Минимальное число.
     * @param lastValue Максимальное число.
     */
    void generateNewSequence(int firstValue, int lastValue);

    /**
     * Старт прослушивания стрима сервера.
     */
    void serverStreamAsyncStub() throws Exception;

    /**
     * Получить последнее число, пришедшее от сервера.
     * @return Последнее число, пришедшее от сервера.
     */
    int getCurrentNumber();

    /**
     * Получить объект текущего канала связи с сервером.
     * @return Объект текущего канала связи с сервером.
     */
    Channel getChannel();

    /**
     * Получить объект счётчика для контроля завершения стрима сервера.
     * @return Объект счётчика для контроля завершения стрима сервера.
     */
    CountDownLatch getServerStreamLatch();
}
