pub mod error;

use crate::error::Result;
use bevy::app::{App, AppExit};
use bevy::log::tracing_subscriber;
use bevy::prelude::*;
use bevy::window::{
    RawHandleWrapper, Window, WindowResolution, WindowWrapper,
};
use raw_window_handle::{
    DisplayHandle, HandleError, HasDisplayHandle, HasWindowHandle, RawDisplayHandle,
    RawWindowHandle, WindowHandle,
};
use std::cell::RefCell;
use std::num::NonZero;
use std::sync::OnceLock;
use tracing::debug;

static IS_INIT: OnceLock<()> = OnceLock::new();

thread_local! {
    static APP: OnceLock<RefCell<App>> = OnceLock::default();
}

fn app<T>(cb: impl FnOnce(&App) -> Result<T>) -> Result<T> {
    let res = APP.with(|app_lock| {
        let app = app_lock
            .get()
            .ok_or_else(|| error::ProcessingError::AppAccess)?
            .borrow();
        cb(&app)
    })?;
    Ok(res)
}

fn app_mut<T>(cb: impl FnOnce(&mut App) -> Result<T>) -> Result<T> {
    let res = APP.with(|app_lock| {
        let mut app = app_lock
            .get()
            .ok_or_else(|| error::ProcessingError::AppAccess)?
            .borrow_mut();
        cb(&mut app)
    })?;
    Ok(res)
}

struct GlfwWindow {
    window_handle: RawWindowHandle,
    display_handle: RawDisplayHandle,
}

// SAFETY:
//  - RawWindowHandle and RawDisplayHandle are just pointers
//  - The actual window is managed by Java and outlives this struct
//  - GLFW is thread-safe-ish, see https://www.glfw.org/faq#29---is-glfw-thread-safe
//
// Note: we enforce that all calls to init/update/exit happen on the main thread, so
// there should be no concurrent access to the window from multiple threads anyway.
unsafe impl Send for GlfwWindow {}
unsafe impl Sync for GlfwWindow {}

impl HasWindowHandle for GlfwWindow {
    fn window_handle(&self) -> core::result::Result<WindowHandle<'_>, HandleError> {
        // SAFETY:
        //  - Handles passed from Java are valid
        Ok(unsafe { WindowHandle::borrow_raw(self.window_handle) })
    }
}

impl HasDisplayHandle for GlfwWindow {
    fn display_handle(&self) -> core::result::Result<DisplayHandle<'_>, HandleError> {
        // SAFETY:
        //  - Handles passed from Java are valid
        Ok(unsafe { DisplayHandle::borrow_raw(self.display_handle) })
    }
}

/// Create a WebGPU surface from a native window handle.
///
/// Currently, this just creates a bevy window with the given parameters and
/// stores the raw window handle for later use by the renderer, which will
/// actually create the surface.
pub fn create_surface(
    window_handle: u64,
    width: u32,
    height: u32,
    scale_factor: f32,
) -> Result<u64> {
    #[cfg(target_os = "macos")]
    let (raw_window_handle, raw_display_handle) = {
        use raw_window_handle::{AppKitDisplayHandle, AppKitWindowHandle};

        // GLFW gives us NSWindow*, but AppKitWindowHandle needs NSView*
        // so we have to do some objc magic to grab the right pointer
        let ns_view_ptr = {
            use objc2::rc::Retained;
            use objc2_app_kit::{NSView, NSWindow};

            // SAFETY:
            //  - window_handle is a valid NSWindow pointer from the GLFW window
            let ns_window = window_handle as *mut NSWindow;
            if ns_window.is_null() {
                return Err(error::ProcessingError::InvalidWindowHandle);
            }

            // SAFETY:
            // - The contentView is owned by NSWindow and remains valid as long as the window exists
            let ns_window_ref = unsafe { &*ns_window };
            let content_view: Option<Retained<NSView>> = ns_window_ref.contentView();

            match content_view {
                Some(view) => {
                    let view_ptr = Retained::as_ptr(&view) as *mut std::ffi::c_void;
                    view_ptr
                }
                None => {
                    return Err(error::ProcessingError::InvalidWindowHandle);
                }
            }
        };

        let window = AppKitWindowHandle::new(std::ptr::NonNull::new(ns_view_ptr).unwrap());
        let display = AppKitDisplayHandle::new();
        (
            RawWindowHandle::AppKit(window),
            RawDisplayHandle::AppKit(display),
        )
    };

    #[cfg(target_os = "windows")]
    let (raw_window_handle, raw_display_handle) =
        { todo!("implemnt windows raw window handle conversion") };

    #[cfg(target_os = "linux")]
    let (raw_window_handle, raw_display_handle) =
        { todo!("implement linux raw window handle conversion") };

    let glfw_window = GlfwWindow {
        window_handle: raw_window_handle,
        display_handle: raw_display_handle,
    };

    let window_wrapper = WindowWrapper::new(glfw_window);
    let handle_wrapper = RawHandleWrapper::new(&window_wrapper)?;

    let entity_id = app_mut(|app| {
        let entity = app
            .world_mut()
            .spawn((
                Window {
                    resolution: WindowResolution::new(width, height)
                        .with_scale_factor_override(scale_factor),
                    ..default()
                },
                handle_wrapper,
            ))
            .id();

        // TODO: spawn a camera for this window with a render target of this window

        Ok(entity.to_bits())
    })?;

    Ok(entity_id)
}

/// Update window size when resized.
pub fn window_resized(window_id: u64, width: u32, height: u32) -> Result<()> {
    app_mut(|app| {
        let entity = Entity::from_bits(window_id);
        if let Some(mut window) = app.world_mut().get_mut::<Window>(entity) {
            window.resolution.set_physical_resolution(width, height);
            Ok(())
        } else {
            Err(error::ProcessingError::WindowNotFound)
        }
    })
}

/// Initialize the app, if not already initialized. Must be called from the main thread and cannot
/// be called concurrently from multiple threads.
pub fn init() -> Result<()> {
    setup_tracing()?;
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

            app.add_plugins(
                DefaultPlugins
                    .build()
                    .disable::<bevy::log::LogPlugin>()
                    .disable::<bevy::winit::WinitPlugin>()
                    .disable::<bevy::render::pipelined_rendering::PipelinedRenderingPlugin>()
                    .set(WindowPlugin {
                        primary_window: None,
                        exit_condition: bevy::window::ExitCondition::DontExit,
                        ..default()
                    }),
            );

            // this does not mean, as one might imagine, that the app is "done", but rather is part
            // of bevy's plugin lifecycle prior to "starting" the app. we are manually driving the app
            // so we don't need to call `app.run()`
            app.finish();
            app.cleanup();
            RefCell::new(app)
        });
    });

    Ok(())
}
pub fn update() -> Result<()> {
    app_mut(|app| {
        app.update();
        Ok(())
    })
}

pub fn exit(exit_code: u8) -> Result<()> {
    app_mut(|app| {
        app.world_mut().write_message(match exit_code {
            0 => AppExit::Success,
            _ => AppExit::Error(NonZero::new(exit_code).unwrap()),
        });

        // one final update to process the exit message
        app.update();
        Ok(())
    })
}

fn setup_tracing() -> Result<()> {
    let subscriber = tracing_subscriber::FmtSubscriber::new();
    tracing::subscriber::set_global_default(subscriber)?;
    Ok(())
}
