package processing.webgpu;

/**
 * Unchecked exception thrown for WebGPU-related errors.
 * <p>
 * WebGPU operations can fail for various reasons, such as unsupported hardware, but are not
 * expected to be recoverable by the Processing application.
 */
public class PWebGPUException extends RuntimeException {
    public PWebGPUException(String message) {
        super(message);
    }
}
