pub mod error;

// Once-cell for our app
use crate::error::Result;
use bevy::app::App;
use bevy::log::tracing_subscriber;
use std::sync::{Arc, Mutex, OnceLock};
use tracing::{debug, info};

static IS_INIT: OnceLock<()> = OnceLock::new();

thread_local! {
    static APP: OnceLock<App> = OnceLock::default();
}

/// Initialize the app, if not already initialized. Must be called from the main thread and cannot
/// be called concurrently from multiple threads.
pub fn init() -> Result<()> {
    setup_tracing()?;
    info!("Initializing libprocessing");

    let is_init = IS_INIT.get().is_some();
    let thread_has_app = APP.with(|app_lock| app_lock.get().is_some());
    if is_init && !thread_has_app {
        return Err(error::ProcessingError::AppAccess);
    }
    if is_init && thread_has_app {
        debug!("App already initialized");
        return Ok(());
    }

    APP.with(|app_lock| {
        app_lock.get_or_init(|| {
            IS_INIT.get_or_init(|| ());
            let mut app = App::new();

            app.add_plugins(bevy::MinimalPlugins);

            app
        });
    });

    Ok(())
}

fn setup_tracing() -> Result<()> {
    let subscriber = tracing_subscriber::FmtSubscriber::new();
    tracing::subscriber::set_global_default(subscriber)?;
    Ok(())
}
