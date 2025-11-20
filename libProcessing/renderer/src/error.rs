use thiserror::Error;

pub type Result<T> = std::result::Result<T, ProcessingError>;

#[derive(Error, Debug)]
pub enum ProcessingError {
    #[error("App was accessed from multiple threads")]
    AppAccess,
    #[error("Error initializing tracing: {0}")]
    Tracing(#[from] tracing::subscriber::SetGlobalDefaultError),
    #[error("Window not found")]
    WindowNotFound,
    #[error("Handle error: {0}")]
    HandleError(#[from] raw_window_handle::HandleError),
    #[error("Invalid window handle provided")]
    InvalidWindowHandle,
}
