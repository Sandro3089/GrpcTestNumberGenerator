package com.baliasnikov.grpctestnumbergenerator.client.impl;

import java.util.concurrent.CountDownLatch;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.google.protobuf.Empty;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import com.baliasnikov.grpctestnumbergenerator.NumberMessageOuterClass;
import com.baliasnikov.grpctestnumbergenerator.NumberSequenceOuterClass;
import com.baliasnikov.grpctestnumbergenerator.NumberServiceGrpc;
import com.baliasnikov.grpctestnumbergenerator.client.GrpcNumberClient;

/**
 * Реализация клиента GRPC.
 *
 * @author Aleksandr Baliasnikov
 */
@Slf4j
public class GrpcNumberClientImpl implements GrpcNumberClient {

    /** Хост по умолчанию. */
    private static final String HOST = "localhost";

    /** Порт по умолчанию. */
    private static final int PORT = 7777;

    /** Адрес для подключения по умолчанию. */
    private static final String TARGET = String.format("%s:%s", HOST, PORT);

    /** Текущий активный канал связи с сервером. */
    private final Channel channel;

    /** Объект для синхронного обмена данными с сервером. */
    private final NumberServiceGrpc.NumberServiceBlockingStub blockingStub;

    /** Объект для асинхронного обмена данными с сервером. */
    private final NumberServiceGrpc.NumberServiceStub asyncStub;

    /** Текущее последнее число, пришедшее от сервера. */
    private int currentNumber;

    /** Счетчик для блокировки повторного вызова прослушивания стрима сервера и ожидания его завершения. */
    private CountDownLatch serverStreamLatch = new CountDownLatch(0);

    @Override
    public int getCurrentNumber() {
        return currentNumber;
    }

    @Override
    public Channel getChannel() {
        return channel;
    }

    public CountDownLatch getServerStreamLatch() {
        return serverStreamLatch;
    }

    /**
     * Конструктор.
     */
    public GrpcNumberClientImpl() {
        channel = ManagedChannelBuilder.forTarget(TARGET)
                .usePlaintext()
                .build();
        blockingStub = NumberServiceGrpc.newBlockingStub(channel);
        asyncStub = NumberServiceGrpc.newStub(channel);
    }

    /**
     * Конструктор.
     * @param channel Канал связи между сервером и клиентом.
     */
    public GrpcNumberClientImpl(Channel channel) {
        this.channel = channel;
        blockingStub = NumberServiceGrpc.newBlockingStub(channel);
        asyncStub = NumberServiceGrpc.newStub(channel);
    }

    @Override
    @SneakyThrows
    public void generateNewSequence(int firstValue, int lastValue) {
        try {
            log.info("Generate new sequence request, firstValue:{}, lastValue:{}", firstValue, lastValue);
            NumberSequenceOuterClass.NumberSequence numberSequenceRequest = NumberSequenceOuterClass.NumberSequence.newBuilder()
                    .setFirstValue(firstValue)
                    .setLastValue(lastValue)
                    .build();
            blockingStub.generateNewSequence(numberSequenceRequest);
            log.info("Generate new sequence complete");
        } catch (StatusRuntimeException ex) {
            log.error("Generate new sequence failed: {}", ex.getStatus());
            throw new Exception(ex.getMessage());
        }
    }

    @Override
    public void serverStreamAsyncStub() {
        if (serverStreamLatch.getCount() > 0) {
            log.info("Previous ServerStreamAsyncStub Running, return");
            return;
        }
        currentNumber = 0;
        serverStreamLatch = new CountDownLatch(1);
        log.info("ServerStreamAsyncStub Start");
        val clientStreamObserver =
                new StreamObserver<NumberMessageOuterClass.NumberMessage>() {

                    @Override
                    public void onNext(NumberMessageOuterClass.NumberMessage numberMessage) {
                        currentNumber = numberMessage.getCurrentValue();
                        log.info("New value from server: {}", currentNumber);
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.warn("ServerStreamAsyncStub Failed: {}", Status.fromThrowable(t));
                        serverStreamLatch.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        log.info("ServerStreamAsyncStub Finished");
                        serverStreamLatch.countDown();
                    }
                };
        asyncStub.serverStream(Empty.newBuilder().build(), clientStreamObserver);
    }
}
