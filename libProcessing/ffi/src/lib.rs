use bevy::prelude::Entity;
use renderer::render::command::DrawCommand;

use crate::color::Color;

mod color;
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

/// Destroy the surface associated with the given window ID.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_destroy_surface(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::destroy_surface(window_entity));
}

/// Update window size when resized.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_resize_surface(window_id: u64, width: u32, height: u32) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::resize_surface(window_entity, width, height));
}

/// Set the background color for the given window.
///
/// SAFETY:
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_background_color(window_id: u64, color: Color) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::background_color(window_entity, color.into()));
}

/// Begins the draw for the given window.
///
/// SAFETY:
/// - Init has been called and exit has not been called.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_begin_draw(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::begin_draw(window_entity));
}

/// Flushes recorded draw commands for the given window.
///
/// SAFETY:
/// - Init has been called and exit has not been called.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_flush(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::flush(window_entity));
}

/// Ends the draw for the given window and presents the frame.
///
/// SAFETY:
/// - Init has been called and exit has not been called.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_end_draw(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::end_draw(window_entity));
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

/// Set the fill color.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_set_fill(window_id: u64, r: f32, g: f32, b: f32, a: f32) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    let color = bevy::color::Color::srgba(r, g, b, a);
    error::check(|| renderer::record_command(window_entity, DrawCommand::Fill(color)));
}

/// Set the stroke color.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_set_stroke_color(window_id: u64, r: f32, g: f32, b: f32, a: f32) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    let color = bevy::color::Color::srgba(r, g, b, a);
    error::check(|| renderer::record_command(window_entity, DrawCommand::StrokeColor(color)));
}

/// Set the stroke weight.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_set_stroke_weight(window_id: u64, weight: f32) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::record_command(window_entity, DrawCommand::StrokeWeight(weight)));
}

/// Disable fill for subsequent shapes.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_no_fill(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::record_command(window_entity, DrawCommand::NoFill));
}

/// Disable stroke for subsequent shapes.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_no_stroke(window_id: u64) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| renderer::record_command(window_entity, DrawCommand::NoStroke));
}

/// Draw a rectangle.
///
/// SAFETY:
/// - Init and create_surface have been called.
/// - window_id is a valid ID returned from create_surface.
/// - This is called from the same thread as init.
#[unsafe(no_mangle)]
pub extern "C" fn processing_rect(
    window_id: u64,
    x: f32,
    y: f32,
    w: f32,
    h: f32,
    tl: f32,
    tr: f32,
    br: f32,
    bl: f32,
) {
    error::clear_error();
    let window_entity = Entity::from_bits(window_id);
    error::check(|| {
        renderer::record_command(
            window_entity,
            DrawCommand::Rect {
                x,
                y,
                w,
                h,
                radii: [tl, tr, br, bl],
            },
        )
    });
}
