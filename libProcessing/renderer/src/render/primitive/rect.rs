use bevy::{
    mesh::{Indices, VertexAttributeValues},
    prelude::*,
};
use lyon::{geom::Point, path::Path};

use crate::render::primitive::{TessellationMode, tessellate_path};

fn rect_path(x: f32, y: f32, w: f32, h: f32, radii: [f32; 4]) -> Path {
    let mut path_builder = Path::builder();
    let [tl, tr, br, bl] = radii;

    // tl
    path_builder.begin(Point::new(x + tl, y));

    // tl -> tr
    if tr > 0.0 {
        path_builder.line_to(Point::new(x + w - tr, y));
        path_builder.quadratic_bezier_to(Point::new(x + w, y), Point::new(x + w, y + tr));
    } else {
        path_builder.line_to(Point::new(x + w, y));
    }

    // tr -> br
    if br > 0.0 {
        path_builder.line_to(Point::new(x + w, y + h - br));
        path_builder.quadratic_bezier_to(Point::new(x + w, y + h), Point::new(x + w - br, y + h));
    } else {
        path_builder.line_to(Point::new(x + w, y + h));
    }

    // br -> bl
    if bl > 0.0 {
        path_builder.line_to(Point::new(x + bl, y + h));
        path_builder.quadratic_bezier_to(Point::new(x, y + h), Point::new(x, y + h - bl));
    } else {
        path_builder.line_to(Point::new(x, y + h));
    }

    // bl -> tl
    if tl > 0.0 {
        path_builder.line_to(Point::new(x, y + tl));
        path_builder.quadratic_bezier_to(Point::new(x, y), Point::new(x + tl, y));
    }

    path_builder.end(true);
    path_builder.build()
}

pub fn rect(
    mesh: &mut Mesh,
    x: f32,
    y: f32,
    w: f32,
    h: f32,
    radii: [f32; 4], // [tl, tr, br, bl]
    color: Color,
    mode: TessellationMode,
) {
    if radii == [0.0; 4] && matches!(mode, TessellationMode::Fill) {
        simple_rect(mesh, x, y, w, h, color);
    } else {
        let path = rect_path(x, y, w, h, radii);
        tessellate_path(mesh, &path, color, mode);
    }
}

fn simple_rect(mesh: &mut Mesh, x: f32, y: f32, w: f32, h: f32, color: Color) {
    let base_idx = if let Some(VertexAttributeValues::Float32x3(positions)) =
        mesh.attribute(Mesh::ATTRIBUTE_POSITION)
    {
        positions.len() as u32
    } else {
        0
    };

    if let Some(VertexAttributeValues::Float32x3(positions)) =
        mesh.attribute_mut(Mesh::ATTRIBUTE_POSITION)
    {
        positions.push([x, y, 0.0]);
        positions.push([x + w, y, 0.0]);
        positions.push([x + w, y + h, 0.0]);
        positions.push([x, y + h, 0.0]);
    }

    if let Some(VertexAttributeValues::Float32x4(colors)) =
        mesh.attribute_mut(Mesh::ATTRIBUTE_COLOR)
    {
        let color_array = color.to_srgba().to_f32_array();
        for _ in 0..4 {
            colors.push(color_array);
        }
    }

    if let Some(VertexAttributeValues::Float32x3(normals)) =
        mesh.attribute_mut(Mesh::ATTRIBUTE_NORMAL)
    {
        for _ in 0..4 {
            normals.push([0.0, 0.0, 1.0]);
        }
    }

    if let Some(Indices::U32(indices)) = mesh.indices_mut() {
        indices.push(base_idx + 0);
        indices.push(base_idx + 1);
        indices.push(base_idx + 2);

        indices.push(base_idx + 0);
        indices.push(base_idx + 2);
        indices.push(base_idx + 3);
    }
}
