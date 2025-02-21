<img alt="Processing Logo" src="https://processing.org/favicon.svg" width="250">


# Vulkan in Processing



This project aims to add a new Vulkan-based renderer to the Processing framework (https://github.com/processing/processing4).

Currently, it's part of a university dissertation project. However later I plan to release this as a library rather than just being a copy+paste of the whole of Processing.

## How it works

![updated-design-1](https://github.com/user-attachments/assets/1ed4bbb1-88d4-4c30-8ce0-eec2ca128483)

(*Note: this diagram isn't fully up-to-date*)

This framework works by keeping most of the PGraphicsOpenGL code intact and simply emulating OpenGL behaviour through a thin OpenGL-to-Vulkan translation layer. This layer also is specifically optimised to work with certain elements of Processing. This layer is bounded to the PGL abstraction layer. We use LWJGL to use the Vulkan API.

## Features
- New PV3D and PV2D renderers
- OpenGL-GLSL to Vulkan-GLSL shader converter
- Automatic multithreading, meaning sketches can utilise 100% of the CPU.


## What works/doesn't work

### WORKING:
- Primitive 2D shapes (rect, ellipse, line, etc)
- Primitive 3D shapes (box, sphere, etc)
- Textures
- Depth buffer
- Immediate rendering mode

### PARTIALLY WORKING:
- Retained rendering mode (works mostly, but still some bugs, particularly with the StaticParticlesRetained example sketch)
- Custom PShaders (uniform arrays and uniforms that add up to over 256 bytes do not work yet, also GL-to-VK shader converter is still imperfect and some complex shaders will fail to compile)
- PGL (highly recommend against its usage in Processing sketches)
- hint() function (not fully tested yet)

### NOT WORKING / NOT IMPLEMENTED YET:
- Anti-aliasing
- PGraphics (rendering to an off-screen buffer)
- Keeping the previous framebuffer, i.e. drawing without calling background()
- Texture mipmapping
- Materials and lighting (the uniforms for the lighting shaders exceeds the 256 byte limit)
- Probably loads more I haven't noticed yet
- Resizing the window
- Some window GLFW stuff, such as keyboard input, mouse clicking and scrolling, window name and icon, etc.

### Notes:
- I recently got a new laptop and multithreading seems to cause slowdowns due to Vulkan commands being WAY faster than on my old laptop... I'll need to fix that.
- I'd like to expose Vulkan in sketches using PVK, similar to PGL. My hope is that it could help make learning Vulkan substantially easier than setting up and doing everything from scratch.
- There's some things in Processing that could work better if it used pure Vulkan rather than the GL-to-VK translation layer; PShapes and Vulkan's fixed command buffers, and improved retained mode to name a couple.
- Need to install external LWJGL JAR's to the core/library folder... I'll need to figure out how to make ANT install this automatically.
- PGraphicsOpenGL has modifications to use multiple buffers to avoid ThreadNodes writing to the same buffer at the same time.

## Performance
TODO
