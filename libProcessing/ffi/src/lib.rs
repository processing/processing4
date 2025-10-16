#[unsafe(no_mangle)]
pub extern "C" fn processing_add(left: u64, right: u64) -> u64 {
    renderer::add(left, right)
}