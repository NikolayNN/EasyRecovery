package by.aurorasoft.easy.recovery;

@FunctionalInterface
public interface EasyRecoveryExceptionHandler {

    EasyRecoveryExceptionHandler NOOP = t -> { /* ничего не делаем */ };

    void handle(Throwable e);
}
