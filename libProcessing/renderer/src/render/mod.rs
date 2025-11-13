pub mod command;
pub mod material;
pub mod mesh_builder;
mod primitive;

use bevy::{camera::visibility::RenderLayers, ecs::system::SystemParam, prelude::*};
use command::{CommandBuffer, DrawCommand};
use material::MaterialKey;
use primitive::{TessellationMode, empty_mesh};

use crate::{Flush, render::primitive::rect};

#[derive(Component)]
pub struct TransientMesh;

#[derive(SystemParam)]
pub struct RenderContext<'w, 's> {
    commands: Commands<'w, 's>,
    meshes: ResMut<'w, Assets<Mesh>>,
    materials: ResMut<'w, Assets<StandardMaterial>>,
    batch: Local<'s, BatchState>,
    state: Local<'s, RenderState>,
}

#[derive(Default)]
struct BatchState {
    current_mesh: Option<Mesh>,
    material_key: Option<MaterialKey>,
    draw_index: u32,
    render_layers: RenderLayers,
    surface_entity: Option<Entity>,
}

#[derive(Debug)]
pub struct RenderState {
    // drawing state
    pub fill_color: Option<Color>,
    pub stroke_color: Option<Color>,
    pub stroke_weight: f32,
}

impl Default for RenderState {
    fn default() -> Self {
        Self {
            fill_color: Some(Color::WHITE),
            stroke_color: Some(Color::BLACK),
            stroke_weight: 1.0,
        }
    }
}

impl RenderState {
    pub fn new() -> Self {
        Self::default()
    }

    pub fn has_fill(&self) -> bool {
        self.fill_color.is_some()
    }

    pub fn has_stroke(&self) -> bool {
        self.stroke_color.is_some()
    }

    pub fn fill_is_transparent(&self) -> bool {
        self.fill_color.map(|c| c.alpha() < 1.0).unwrap_or(false)
    }

    pub fn stroke_is_transparent(&self) -> bool {
        self.stroke_color.map(|c| c.alpha() < 1.0).unwrap_or(false)
    }
}

pub fn flush_draw_commands(
    mut ctx: RenderContext,
    mut query: Query<(Entity, &mut CommandBuffer, &RenderLayers), With<Flush>>,
) {
    for (surface_entity, mut cmd_buffer, render_layers) in query.iter_mut() {
        let draw_commands = std::mem::take(&mut cmd_buffer.commands);
        ctx.batch.render_layers = render_layers.clone();
        ctx.batch.surface_entity = Some(surface_entity);
        ctx.batch.draw_index = 0; // Reset draw index for each flush

        for cmd in draw_commands {
            match cmd {
                DrawCommand::Fill(color) => {
                    ctx.state.fill_color = Some(color);
                }
                DrawCommand::NoFill => {
                    ctx.state.fill_color = None;
                }
                DrawCommand::StrokeColor(color) => {
                    ctx.state.stroke_color = Some(color);
                }
                DrawCommand::NoStroke => {
                    ctx.state.stroke_color = None;
                }
                DrawCommand::StrokeWeight(weight) => {
                    ctx.state.stroke_weight = weight;
                }
                DrawCommand::Rect { x, y, w, h, radii } => {
                    add_fill(&mut ctx, |mesh, color| {
                        rect(mesh, x, y, w, h, radii, color, TessellationMode::Fill)
                    });

                    add_stroke(&mut ctx, |mesh, color, weight| {
                        rect(
                            mesh,
                            x,
                            y,
                            w,
                            h,
                            radii,
                            color,
                            TessellationMode::Stroke(weight),
                        )
                    });
                }
            }
        }

        flush_batch(&mut ctx);
    }
}

pub fn activate_cameras(
    mut cameras: Query<&mut Camera>,
    mut surfaces: Query<&Children, With<Flush>>,
) {
    for mut camera in cameras.iter_mut() {
        camera.is_active = false;
    }

    for children in surfaces.iter_mut() {
        for child in children.iter() {
            if let Ok(mut camera) = cameras.get_mut(child) {
                camera.is_active = true;
            }
        }
    }
}

pub fn clear_transient_meshes(
    mut commands: Commands,
    surfaces: Query<&Children, With<Flush>>,
    transient_meshes: Query<(), With<TransientMesh>>,
) {
    // for all flushing surfaces, despawn all transient meshes that rendered in a previous frame
    for children in surfaces.iter() {
        for child in children.iter() {
            if transient_meshes.contains(child) {
                commands.entity(child).despawn();
            }
        }
    }
}

fn spawn_mesh(ctx: &mut RenderContext, mesh: Mesh, z_offset: Option<f32>) {
    let Some(material_key) = &ctx.batch.material_key else {
        return;
    };
    let Some(surface_entity) = ctx.batch.surface_entity else {
        return;
    };

    let mesh_handle = ctx.meshes.add(mesh);
    let material_handle = ctx.materials.add(material_key.to_material());

    let components = (
        Mesh3d(mesh_handle),
        MeshMaterial3d(material_handle),
        TransientMesh,
        ctx.batch.render_layers.clone(),
    );

    let mesh_id = if let Some(z) = z_offset {
        ctx.commands
            .spawn((components, Transform::from_xyz(0.0, 0.0, z)))
            .id()
    } else {
        ctx.commands.spawn(components).id()
    };

    ctx.commands.entity(surface_entity).add_child(mesh_id);
}

fn add_fill(ctx: &mut RenderContext, tessellate: impl FnOnce(&mut Mesh, Color)) {
    let Some(color) = ctx.state.fill_color else {
        return;
    };
    let material_key = MaterialKey {
        transparent: ctx.state.fill_is_transparent(),
    };

    // when the material changes, flush the current batch
    if ctx.batch.material_key.as_ref() != Some(&material_key) {
        flush_batch(ctx);
        ctx.batch.material_key = Some(material_key);
        ctx.batch.current_mesh = Some(empty_mesh());
    }

    // accumulate geometry into the current mega mesh
    if let Some(ref mut mesh) = ctx.batch.current_mesh {
        tessellate(mesh, color);
    }
}

fn add_stroke(ctx: &mut RenderContext, tessellate: impl FnOnce(&mut Mesh, Color, f32)) {
    let Some(color) = ctx.state.stroke_color else {
        return;
    };
    let stroke_weight = ctx.state.stroke_weight;
    let material_key = MaterialKey {
        transparent: ctx.state.stroke_is_transparent(),
    };

    // when the material changes, flush the current batch
    if ctx.batch.material_key.as_ref() != Some(&material_key) {
        flush_batch(ctx);
        ctx.batch.material_key = Some(material_key);
        ctx.batch.current_mesh = Some(empty_mesh());
    }

    // accumulate geometry into the current mega mesh
    if let Some(ref mut mesh) = ctx.batch.current_mesh {
        tessellate(mesh, color, stroke_weight);
    }
}

fn flush_batch(ctx: &mut RenderContext) {
    if let Some(mesh) = ctx.batch.current_mesh.take() {
        // we defensively apply a small z-offset based on draw_index to preserve painter's algorithm
        let z_offset = ctx.batch.draw_index as f32 * -0.001;
        spawn_mesh(ctx, mesh, Some(z_offset));
        ctx.batch.draw_index += 1;
    }
    ctx.batch.material_key = None;
}
