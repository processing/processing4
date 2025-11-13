pub mod error;
pub mod render;

use std::{cell::RefCell, num::NonZero, sync::OnceLock};

use bevy::{
    app::{App, AppExit},
    asset::AssetEventSystems,
    camera::{CameraOutputMode, RenderTarget, visibility::RenderLayers},
    log::tracing_subscriber,
    prelude::*,
    window::{RawHandleWrapper, Window, WindowRef, WindowResolution, WindowWrapper},
};
use raw_window_handle::{
    DisplayHandle, HandleError, HasDisplayHandle, HasWindowHandle, RawDisplayHandle,
    RawWindowHandle, WindowHandle,
};
use render::{activate_cameras, clear_transient_meshes, flush_draw_commands};
use tracing::debug;

use crate::{
    error::Result,
    render::command::{CommandBuffer, DrawCommand},
};

static IS_INIT: OnceLock<()> = OnceLock::new();

thread_local! {
    static APP: OnceLock<RefCell<App>> = OnceLock::default();
}

#[derive(Resource, Default)]
struct WindowCount(u32);

#[derive(Component)]
pub struct Flush;

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
    let (raw_window_handle, raw_display_handle) = {
        use raw_window_handle::{Win32WindowHandle, WindowsDisplayHandle};
        use std::num::NonZeroIsize;
        use windows::Win32::Foundation::HINSTANCE;
        use windows::Win32::System::LibraryLoader::GetModuleHandleW;

        if window_handle == 0 {
            return Err(error::ProcessingError::InvalidWindowHandle);
        }

        // HWND is isize, so cast it
        let hwnd_isize = window_handle as isize;
        let hwnd_nonzero = match NonZeroIsize::new(hwnd_isize) {
            Some(nz) => nz,
            None => return Err(error::ProcessingError::InvalidWindowHandle),
        };

        let mut window = Win32WindowHandle::new(hwnd_nonzero);

        // VK_KHR_win32_surface requires hinstance *and* hwnd
        // SAFETY: GetModuleHandleW(NULL) is safe
        let hinstance = unsafe { GetModuleHandleW(None) }
            .map_err(|_| error::ProcessingError::InvalidWindowHandle)?;

        let hinstance_nonzero = NonZeroIsize::new(hinstance.0 as isize)
            .ok_or(error::ProcessingError::InvalidWindowHandle)?;
        window.hinstance = Some(hinstance_nonzero);

        let display = WindowsDisplayHandle::new();

        (
            RawWindowHandle::Win32(window),
            RawDisplayHandle::Windows(display),
        )
    };

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
        let mut window_count = app.world_mut().resource_mut::<WindowCount>();
        let count = window_count.0;
        window_count.0 += 1;
        let render_layer = RenderLayers::none().with(count as usize);

        let mut window = app.world_mut().spawn((
            Window {
                resolution: WindowResolution::new(width, height)
                    .with_scale_factor_override(scale_factor),
                ..default()
            },
            handle_wrapper,
            CommandBuffer::default(),
            // this doesn't do anything but makes it easier to fetch the render layer for
            // meshes to be drawn to this window
            render_layer.clone(),
        ));

        let window_entity = window.id();
        window.with_children(|parent| {
            // processing has a different coordinate system for 2d rendering:
            // - origin at top-left
            // - x increases to the right, y increases downward
            // - coordinate units are in screen pixels
            let half_width = width as f32 / 2.0;
            let half_height = height as f32 / 2.0;

            let projection = OrthographicProjection {
                near: -1000.0,
                far: 1000.0,
                viewport_origin: Vec2::new(0.0, 0.0), // top left
                scaling_mode: bevy::camera::ScalingMode::Fixed {
                    width: width as f32,
                    height: height as f32,
                },
                scale: 1.0,
                ..OrthographicProjection::default_3d()
            };

            parent.spawn((
                Camera3d::default(),
                Camera {
                    target: RenderTarget::Window(WindowRef::Entity(window_entity)),
                    ..default()
                },
                Projection::Orthographic(projection),
                // position camera to match coordinate system
                Transform::from_xyz(half_width, -half_height, 999.0)
                    .looking_at(Vec3::new(half_width, -half_height, 0.0), Vec3::Y),
                render_layer,
            ));
        });

        Ok(window_entity.to_bits())
    })?;

    Ok(entity_id)
}

pub fn destroy_surface(window_entity: Entity) -> Result<()> {
    app_mut(|app| {
        if app.world_mut().get::<Window>(window_entity).is_some() {
            app.world_mut().despawn(window_entity);
            let mut window_count = app.world_mut().resource_mut::<WindowCount>();
            window_count.0 = window_count.0.saturating_sub(1);
        }
        Ok(())
    })
}

/// Update window size when resized.
pub fn resize_surface(window_entity: Entity, width: u32, height: u32) -> Result<()> {
    app_mut(|app| {
        if let Some(mut window) = app.world_mut().get_mut::<Window>(window_entity) {
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

            // resources
            app.init_resource::<WindowCount>();

            // rendering
            app.add_systems(First, (clear_transient_meshes, activate_cameras))
                .add_systems(Update, flush_draw_commands.before(AssetEventSystems));

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

macro_rules! camera_mut {
    ($app:expr, $window_entity:expr) => {
        $app.world_mut()
            .query::<(&mut Camera, &ChildOf)>()
            .iter_mut(&mut $app.world_mut())
            .filter_map(|(camera, parent)| {
                if parent.parent() == $window_entity {
                    Some(camera)
                } else {
                    None
                }
            })
            .next()
            .ok_or_else(|| error::ProcessingError::WindowNotFound)?
    };
}

macro_rules! window_mut {
    ($app:expr, $window_entity:expr) => {
        $app.world_mut()
            .get_entity_mut($window_entity)
            .map_err(|_| error::ProcessingError::WindowNotFound)?
    };
}

pub fn begin_draw(_window_entity: Entity) -> Result<()> {
    app_mut(|_app| Ok(()))
}

pub fn flush(window_entity: Entity) -> Result<()> {
    app_mut(|app| {
        window_mut!(app, window_entity).insert(Flush);
        app.update();
        window_mut!(app, window_entity).remove::<Flush>();

        // ensure that the intermediate texture is not cleared
        camera_mut!(app, window_entity).clear_color = ClearColorConfig::None;
        Ok(())
    })
}

pub fn end_draw(window_entity: Entity) -> Result<()> {
    // since we are ending the draw, set the camera to write to the output render target
    app_mut(|app| {
        camera_mut!(app, window_entity).output_mode = CameraOutputMode::Write {
            blend_state: None,
            clear_color: ClearColorConfig::Default,
        };
        Ok(())
    })?;
    // flush any remaining draw commands, this ensures that the frame is presented even if there
    // is no remaining draw commands
    flush(window_entity)?;
    // reset to skipping output for the next frame
    app_mut(|app| {
        camera_mut!(app, window_entity).output_mode = CameraOutputMode::Skip;
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

pub fn background_color(window_entity: Entity, color: Color) -> Result<()> {
    app_mut(|app| {
        let mut camera_query = app.world_mut().query::<(&mut Camera, &ChildOf)>();
        for (mut camera, parent) in camera_query.iter_mut(&mut app.world_mut()) {
            if parent.parent() == window_entity {
                camera.clear_color = ClearColorConfig::Custom(color);
            }
        }
        Ok(())
    })
}

fn setup_tracing() -> Result<()> {
    let subscriber = tracing_subscriber::FmtSubscriber::new();
    tracing::subscriber::set_global_default(subscriber)?;
    Ok(())
}

/// Record a drawing command for a window
pub fn record_command(window_entity: Entity, cmd: DrawCommand) -> Result<()> {
    app_mut(|app| {
        let mut entity_mut = app.world_mut().entity_mut(window_entity);
        if let Some(mut buffer) = entity_mut.get_mut::<CommandBuffer>() {
            buffer.push(cmd);
        }

        Ok(())
    })
}
