use bevy::{
    mesh::{Indices, VertexAttributeValues},
    prelude::*,
};
use lyon::tessellation::{
    FillVertex, StrokeVertex, VertexId,
    geometry_builder::{
        FillGeometryBuilder, GeometryBuilder, GeometryBuilderError, StrokeGeometryBuilder,
    },
};

pub struct MeshBuilder<'a> {
    mesh: &'a mut Mesh,
    color: Color,
    begin_vertex_count: u32,
}

impl<'a> MeshBuilder<'a> {
    pub fn new(mesh: &'a mut Mesh, color: Color) -> Self {
        Self {
            mesh,
            color,
            begin_vertex_count: 0,
        }
    }

    fn push_vertex(&mut self, position: [f32; 3]) -> VertexId {
        let id = VertexId::from_usize(self.vertex_count());

        if let Some(VertexAttributeValues::Float32x3(positions)) =
            self.mesh.attribute_mut(Mesh::ATTRIBUTE_POSITION)
        {
            positions.push(position);
        }

        if let Some(VertexAttributeValues::Float32x4(colors)) =
            self.mesh.attribute_mut(Mesh::ATTRIBUTE_COLOR)
        {
            colors.push(self.color.to_srgba().to_f32_array());
        }

        if let Some(VertexAttributeValues::Float32x3(normals)) =
            self.mesh.attribute_mut(Mesh::ATTRIBUTE_NORMAL)
        {
            normals.push([0.0, 0.0, 1.0]); // flat normal for 2d
        }

        id
    }

    fn push_index(&mut self, index: u32) {
        if let Some(Indices::U32(indices)) = self.mesh.indices_mut() {
            indices.push(index);
        }
    }

    fn vertex_count(&self) -> usize {
        if let Some(VertexAttributeValues::Float32x3(positions)) =
            self.mesh.attribute(Mesh::ATTRIBUTE_POSITION)
        {
            positions.len()
        } else {
            0
        }
    }
}

impl<'a> GeometryBuilder for MeshBuilder<'a> {
    fn begin_geometry(&mut self) {
        self.begin_vertex_count = self.vertex_count() as u32;
    }

    fn add_triangle(&mut self, a: VertexId, b: VertexId, c: VertexId) {
        self.push_index(a.to_usize() as u32);
        self.push_index(b.to_usize() as u32);
        self.push_index(c.to_usize() as u32);
    }

    fn abort_geometry(&mut self) {
        todo!("Implement abort_geometry if needed");
    }
}

impl<'a> FillGeometryBuilder for MeshBuilder<'a> {
    fn add_fill_vertex(&mut self, vertex: FillVertex) -> Result<VertexId, GeometryBuilderError> {
        let pos = vertex.position();
        let position = [pos.x, pos.y, 0.0];
        Ok(self.push_vertex(position))
    }
}

impl<'a> StrokeGeometryBuilder for MeshBuilder<'a> {
    fn add_stroke_vertex(
        &mut self,
        vertex: StrokeVertex,
    ) -> Result<VertexId, GeometryBuilderError> {
        let pos = vertex.position();
        let position = [pos.x, pos.y, 0.0];
        Ok(self.push_vertex(position))
    }
}
