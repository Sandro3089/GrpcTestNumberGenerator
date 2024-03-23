package com.baliasnikov.grpctestnumbergenerator.server;

import net.devh.boot.grpc.server.service.GrpcService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import com.google.protobuf.Empty;

import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.stub.StreamObserver;

import com.baliasnikov.grpctestnumbergenerator.NumberMessageOuterClass;
import com.baliasnikov.grpctestnumbergenerator.NumberSequenceOuterClass;
import com.baliasnikov.grpctestnumbergenerator.NumberServiceGrpc;

/**
 * Реализация сервера GRPC.
 *
 * @author Aleksandr Baliasnikov
 */
@Slf4j
@GrpcService
public class GrpcNumberService extends NumberServiceGrpc.NumberServiceImplBase {

    /** Текущая последовательность чисел для стриминга клиенту */
    private NumberSequenceOuterClass.NumberSequence currentSequence;

    @Override
    public void generateNewSequence(NumberSequenceOuterClass.NumberSequence request,
                                    StreamObserver<Empty> responseObserver) {
        log.info("generateNewSequence request: firstValue: {}, lastValue: {}", request.getFirstValue(), request.getLastValue());
        currentSequence = request;
        responseObserver.onNext(Empty
                .newBuilder()
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void serverStream(Empty request, StreamObserver<NumberMessageOuterClass.NumberMessage> responseObserver) {
        try {
            if (currentSequence != null) {
                // Сохраним последовательность для стриминга в новую переменную
                val sendSequence = NumberSequenceOuterClass.NumberSequence
                        .newBuilder(currentSequence)
                        .build();
                // Отправка числа клиенту, начиная с firstValue + 1 до lastValue раз в 2 секунды
                for (int i = sendSequence.getFirstValue() + 1; i <= sendSequence.getLastValue(); i++) {
                    log.info("Send to client number: {}", i);
                    responseObserver.onNext(NumberMessageOuterClass.NumberMessage
                            .newBuilder()
                            .setCurrentValue(i)
                            .build());
                    Thread.sleep(2000);
                }
                responseObserver.onCompleted();
            } else {
                // Не было запроса с последовательностью от клиента
                responseObserver.onError(new StatusException(Status.FAILED_PRECONDITION));
            }
        } catch (Exception ex) {
            log.error("Server stream failed: {}", ex.getMessage());
            responseObserver.onError(new StatusException(Status.ABORTED));
        }
    }
}
