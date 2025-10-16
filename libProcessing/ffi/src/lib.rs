mod error;

#[unsafe(no_mangle)]
pub extern "C" fn processing_init() {
    error::check(renderer::init());
}