mod rect;

use bevy::{
    asset::RenderAssetUsages,
    mesh::{Indices, PrimitiveTopology},
    prelude::*,
};
use lyon::{
    path::Path,
    tessellation::{
        FillOptions, FillTessellator, LineCap, LineJoin, StrokeOptions, StrokeTessellator,
    },
};
pub use rect::rect;

use super::mesh_builder::MeshBuilder;

pub enum TessellationMode {
    Fill,
    Stroke(f32),
}

pub fn tessellate_path(mesh: &mut Mesh, path: &Path, color: Color, mode: TessellationMode) {
    let mut builder = MeshBuilder::new(mesh, color);
    match mode {
        TessellationMode::Fill => {
            let mut tessellator = FillTessellator::new();
            tessellator
                .tessellate_path(path, &FillOptions::default(), &mut builder)
                .expect("Failed to tessellate fill");
        }
        TessellationMode::Stroke(weight) => {
            let mut tessellator = StrokeTessellator::new();
            let options = StrokeOptions::default()
                .with_line_width(weight)
                .with_line_cap(LineCap::Round)
                .with_line_join(LineJoin::Round);

            tessellator
                .tessellate_path(path, &options, &mut builder)
                .expect("Failed to tessellate stroke");
        }
    }
}

pub fn empty_mesh() -> Mesh {
    let mut mesh = Mesh::new(
        PrimitiveTopology::TriangleList,
        RenderAssetUsages::default(),
    );

    mesh.insert_attribute(Mesh::ATTRIBUTE_POSITION, Vec::<[f32; 3]>::new());
    mesh.insert_attribute(Mesh::ATTRIBUTE_COLOR, Vec::<[f32; 4]>::new());
    mesh.insert_attribute(Mesh::ATTRIBUTE_NORMAL, Vec::<[f32; 3]>::new());
    mesh.insert_indices(Indices::U32(Vec::new()));

    mesh
}
