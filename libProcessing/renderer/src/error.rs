use thiserror::Error;

pub type Result<T> = std::result::Result<T, ProcessingError>;


#[derive(Error, Debug)]
pub enum ProcessingError {
    #[error("App was accessed from multiple threads")]
    AppAccess,
    #[error("Error initializing tracing: {0}")]
    Tracing(#[from] tracing::subscriber::SetGlobalDefaultError),
}