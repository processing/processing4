use std::env;
use std::path::PathBuf;

fn main() {
    let crate_dir = env::var("CARGO_MANIFEST_DIR").unwrap();
    let output_dir = PathBuf::from(&crate_dir).join("include");

    std::fs::create_dir_all(&output_dir).expect("Failed to create include directory");

    let output_file = output_dir.join("processing.h");
    let config_path = PathBuf::from(&crate_dir).join("cbindgen.toml");

    cbindgen::Builder::new()
        .with_config(cbindgen::Config::from_file(&config_path).expect("Failed to load cbindgen.toml"))
        .with_crate(&crate_dir)
        .generate()
        .expect("Unable to generate bindings")
        .write_to_file(&output_file);

    println!("cargo:rerun-if-changed=src/lib.rs");
    println!("cargo:rerun-if-changed=cbindgen.toml");
    println!("cargo:warning=Generated header at: {}", output_file.display());
}
