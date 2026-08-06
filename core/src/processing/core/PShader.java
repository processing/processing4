/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2012-21 The Processing Foundation
  Copyright (c) 2004-12 Ben Fry and Casey Reas
  Copyright (c) 2001-04 Massachusetts Institute of Technology

  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/

package processing.core;

/**
 * This class encapsulates a shader program, including a vertex and a fragment
 * shader. It is the renderer-agnostic type that sketches declare and pass to
 * <b>shader()</b> and <b>filter()</b>; each renderer supplies its own
 * implementation (for example a GLSL-backed shader under P2D/P3D, or a WGSL
 * one under the WebGPU renderer).
 *
 * Use the <b>loadShader()</b> function to load your shader code, rather than
 * constructing a renderer-specific implementation directly.
 *
 * @webref rendering:shaders
 * @webBrief This class encapsulates a shader program, including a vertex and a
 *           fragment shader
 */
public interface PShader {

  /**
   * Sets the uniform variables inside the shader to modify the effect while the
   * program is running.
   *
   * @webref rendering:shaders
   * @webBrief Sets a variable within the shader
   * @param name the name of the uniform variable to modify
   * @param x first component of the variable to modify
   */
  void set(String name, int x);

  /**
   * @param y second component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[2], vec2)
   */
  void set(String name, int x, int y);

  /**
   * @param z third component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[3], vec3)
   */
  void set(String name, int x, int y, int z);

  /**
   * @param w fourth component of the variable to modify. The variable has to be declared with an array/vector type in the shader (i.e.: int[4], vec4)
   */
  void set(String name, int x, int y, int z, int w);

  void set(String name, float x);
  void set(String name, float x, float y);
  void set(String name, float x, float y, float z);
  void set(String name, float x, float y, float z, float w);

  /**
   * @param vec modifies all the components of an array/vector uniform variable. PVector can only be used if the type of the variable is vec3.
   */
  void set(String name, PVector vec);

  void set(String name, boolean x);
  void set(String name, boolean x, boolean y);
  void set(String name, boolean x, boolean y, boolean z);
  void set(String name, boolean x, boolean y, boolean z, boolean w);

  void set(String name, int[] vec);

  /**
   * @param ncoords number of coordinates per element, max 4
   */
  void set(String name, int[] vec, int ncoords);

  void set(String name, float[] vec);
  void set(String name, float[] vec, int ncoords);

  void set(String name, boolean[] vec);
  void set(String name, boolean[] boolvec, int ncoords);

  /**
   * @param mat matrix of values
   */
  void set(String name, PMatrix2D mat);

  void set(String name, PMatrix3D mat);

  /**
   * @param use3x3 enforces the matrix is 3 x 3
   */
  void set(String name, PMatrix3D mat, boolean use3x3);

  /**
   * @param tex sets the sampler uniform variable to read from this image texture
   */
  void set(String name, PImage tex);
}
