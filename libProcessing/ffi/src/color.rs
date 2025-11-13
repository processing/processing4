/// A sRGB (?) color
#[repr(C)]
#[derive(Debug, Clone, Copy)]
pub struct Color {
    pub r: f32,
    pub g: f32,
    pub b: f32,
    pub a: f32,
}

impl From<Color> for bevy::color::Color {
    fn from(color: Color) -> Self {
        bevy::color::Color::srgba(color.r, color.g, color.b, color.a)
    }
}
