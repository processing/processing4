mod error;

/// Initialize libProcessing.
///
/// SAFETY:
/// - This is called from the main thread if the platform requires it.
/// - This can only be called once.
#[unsafe(no_mangle)]
pub extern "C" fn processing_init() {
    error::clear_error();
    error::check(|| renderer::init());
}

/// Create a WebGPU surface from a native window handle.
/// Returns a window ID (entity ID) that should be used for subsequent operations.
/// Returns 0 on failure.
///
/// SAFETY:
/// - Init has been called.
/// - window_handle is a valid GLFW window pointer.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_create_surface(
    window_handle: u64,
    width: u32,
    height: u32,
    scale_factor: f32,
) -> u64 {
    error::clear_error();
    error::check(|| renderer::create_surface(window_handle, width, height, scale_factor))
        .unwrap_or(0)
}

/// Update window size when resized.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_window_resized(window_id: u64, width: u32, height: u32) {
    error::clear_error();
    error::check(|| renderer::window_resized(window_id, width, height));
}

/// Step the application forward.
///
/// SAFETY:
/// - Init has been called and exit has not been called.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_update() {
    error::clear_error();
    error::check(|| renderer::update());
}

/// Shuts down internal resources with given exit code, but does *not* terminate the process.
///
/// SAFETY:
/// - This is called from the same thread as init.
/// - Caller ensures that update is never called again after exit.
#[unsafe(no_mangle)]
pub extern "C" fn processing_exit(exit_code: u8) {
    error::clear_error();
    error::check(|| renderer::exit(exit_code));
}
