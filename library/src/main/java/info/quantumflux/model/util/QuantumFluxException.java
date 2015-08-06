package info.quantumflux.model.util;

public class QuantumFluxException extends RuntimeException {

    public QuantumFluxException() {
    }

    public QuantumFluxException(String detailMessage) {
        super(detailMessage);
    }

    public QuantumFluxException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public QuantumFluxException(Throwable throwable) {
        super(throwable);
    }
}
