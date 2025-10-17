use std::cell::RefCell;
use std::ffi::{c_char, CString};
use renderer::error::ProcessingError;

thread_local! {
    static LAST_ERROR: RefCell<Option<CString>> = RefCell::new(None);
}

/// Check if the last operation resulted in an error. Returns a pointer to an error message, or null
/// if there was no error.
#[unsafe(no_mangle)]
pub extern "C" fn processing_check_error() -> *const c_char {
    LAST_ERROR.with(|last| {
        last.borrow()
            .as_ref()
            .map(|s| s.as_ptr())
            .unwrap_or(std::ptr::null())
    })
}

/// Set the last error message.
pub fn set_error(error_msg: &str) {
    LAST_ERROR.with(|last| {
        *last.borrow_mut() = Some(CString::new(error_msg).unwrap_or_else(|_| {
            CString::new("Failed to allocate error message".to_string()).unwrap()
        }));
    });
}

/// Clear the last error message.
pub fn clear_error() {
    LAST_ERROR.with(|last| {
        *last.borrow_mut() = None;
    });
}


/// Check the result of an operation, setting the last error if there was one.
pub fn check<T>(result: Result<T, ProcessingError>) -> Option<T> {
    match result {
        Ok(value) => {
            Some(value)
        }
        Err(err) => {
            set_error(&err.to_string());
            None
        }
    }
}