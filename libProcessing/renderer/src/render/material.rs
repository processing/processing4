use bevy::{prelude::*, render::alpha::AlphaMode};

#[derive(Clone, PartialEq, Eq, Hash, Debug)]
pub struct MaterialKey {
    pub transparent: bool,
}

impl MaterialKey {
    pub fn to_material(&self) -> StandardMaterial {
        StandardMaterial {
            base_color: Color::WHITE,
            unlit: true,
            cull_mode: None,
            alpha_mode: if self.transparent {
                AlphaMode::Blend
            } else {
                AlphaMode::Opaque
            },
            ..default()
        }
    }
}
