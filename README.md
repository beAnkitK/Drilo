# RuntimeShader Sampling Issue – Missing TileMode / maxSampleOffset Support

## Summary

`RuntimeShader` does not support **TileMode (specifically decal)** or any mechanism similar to **maxSampleOffset**, which prevents correct out-of-bounds sampling.

This limitation breaks physically accurate distortion effects such as ripple/wave shaders, where pixel displacement naturally extends beyond the original content bounds.

---

## Problem Description

A ripple shader is applied to an image rendered inside a bounded preview container.

### Conceptual Model

* **Image** -> Water surface
* **Preview container** -> Lake boundary

A ripple originates (via touch or elapsed time) and propagates across the surface.

---

### Tested on Android 13

* Download the sample APK from the **Releases** section, or clone the repository and build locally.

---

## Code Navigation

All relevant source code is located in the **`kotlin/`** directory.

#### Key files:

* **`ShaderSource.kt`**
  Contains the ripple shader source, including the temporary workaround for out-of-bounds sampling.

* **`ShaderScreen.kt`**
  Applies the `RuntimeShader` to the image using the `previewModifier`. This is the main entry point for observing the behavior.

* **`BaseControls.kt`**
  Hosts all UI components for the shader screen, including:

  * Preview pane
  * Interaction controls (e.g., elapsed time slider, clip toggles)

---

## Expected Behavior

The image should behave like a **continuous wave field**, not a bounded texture.

### Key expectation

When ripple distortion displaces sampling coordinates:

* The image should **flow naturally both inside and outside the container bounds**
* The distortion should **not stop or clamp at edges**
* The wave should appear as if the image is a **fluid surface extending beyond the container**

### Visual interpretation

* When the wave expands outward:

  * Parts of the image should **move outside the container**
  * Areas where pixels are displaced away should **reveal the underlying content (transparent/decal behavior)**

* When the wave pulls inward:

  * Pixels should appear to **flow back into the container**
  * Motion should remain **continuous and physically coherent**

### Important property

This requires:

* Sampling outside bounds to return **transparent (decal)** instead of edge color
* Or allowing sufficient sampling range via **maxSampleOffset**

### What should be seen

* Smooth wave propagation across boundaries
* No visible edge stretching
* No abrupt cut-offs
* Clear illusion of a **fluid surface interacting with the container**

---

## Actual Behavior

### Default (No Clipping)

* When the ripple displaces sampling coordinates outside the image bounds:

  * The shader **clamps to edge pixels**, causing **edge color stretching**
* As a result, the wave:

  * Appears **artificial and constrained**
  * Looks **stuck to the image edges**
  * Lacks natural continuation beyond boundaries
* Instead of behaving like a fluid surface, this produces **visible distortion artifacts** and breaks the illusion of wave propagation

---

### With PreviewPane / Image Clipping

These clip options affect **only the actual behavior**. They do not fix the issue, but expose how the current sampling behaves under different constraints.

#### 1. Both Unclipped

* Image is rendered without any clipping
* Ripple can visually extend beyond the container
* However:

  * Out-of-bounds sampling still results in **edge color stretching**
  * No transparency is introduced outside bounds
* The **entire image boundary (including corners)** contributes to stretched pixels

---

#### 2. Preview Clipped, Image Unclipped

* Ripple is **restricted to the preview container**
* Any portion of the wave outside the container is **cut off**
* Inside the container:

  * Distortion still uses **edge clamping**
  * Edge color stretching remains visible during wave motion

---

#### 3. Preview Unclipped, Image Clipped

* Image is clipped to its shape before distortion is displayed
* This changes how boundaries contribute to stretching:

  * **Corners no longer stretch**
  * **Edge pixels still stretch along the image boundary**
* The distortion appears slightly altered, but:

  * The underlying sampling issue remains unchanged

---

#### 4. Both Clipped

* Ripple is **fully confined within the preview container**
* Image is also clipped to its shape
* Behavior combines effects of (2) and (3):

  * Wave is **truncated at container bounds**
  * **Corner regions do not stretch**
  * **Edge pixels still stretch internally**
* Artifacts remain clearly visible within the container during wave propagation

---

## How to Observe the Issue

1. Open the sample app
2. Use default configuration (no clipping changes initially)
3. Adjust **elapsed time slider**
   * Focus on range: **0.5 → 1.0**
4. Observe:
   * How the image flows as a wave
   * Compare **expected fluid motion vs actual constrained motion**

Then:

5. Enable clip options (for actual behavior only)
6. Test all combinations:

   * Both unclipped
   * One clipped
   * Both clipped

Observe how clipping only changes **how the incorrect behavior manifests**, not the root issue.

---

## Additional Observation

* Using **offscreen composition strategy** does **not** resolve the issue
* Sampling behavior remains unchanged regardless of composition mode

---

## Root Cause

`RuntimeShader` currently:

* Clamps sampling to edge pixels when coordinates go out of bounds
* Does not provide:

  * **TileMode.Decal (transparent sampling)**
  * Any equivalent to **maxSampleOffset**

Because of this:

* Distortion shaders cannot sample outside safely
* Wave propagation becomes visually incorrect

---

## Temporary Workaround

Manual bounds check inside shader:

```agsl
if (newPosition.x < 0.0 ||
    newPosition.y < 0.0 ||
    newPosition.x >= size.x ||
    newPosition.y >= size.y) {
    return vec4(0.0);
}
```

### Limitations

* Introduces manual branching
* Breaks natural GPU sampling flow
* Not scalable for complex distortion shaders
* Only approximates decal behavior

---

## Proposed Direction

A proper solution should enable **correct out-of-bounds sampling semantics**.

Possible directions:

* Provide **TileMode.Decal** support in `RuntimeShader`
* Introduce an API equivalent to **maxSampleOffset**
* Or expose another mechanism that allows:

  * Controlled sampling outside bounds
  * Transparent return for out-of-range coordinates

Any solution should integrate with the GPU pipeline rather than relying on manual shader hacks.

---

## Impact (Specific to This Case)

For ripple/distortion shaders:

* Wave motion appears **physically incorrect**
* Distortion is:

  * Edge-constrained
  * Visually discontinuous
* Cannot achieve:

  * Natural fluid-like behavior
  * Proper reveal of underlying content
* Results in:

  * Noticeably degraded visual quality
  * Inability to implement realistic ripple effects

---

## Sample App

The attached sample demonstrates:

* Ripple shader behavior
* Elapsed time control (key observation range: 0.5–1.0)
* Clip configurations to expose current limitations
* Clear comparison between expected and actual wave flow

---

## Conclusion

Accurate distortion effects require **correct out-of-bounds sampling behavior**. Without support for decal or equivalent mechanisms, `RuntimeShader` cannot produce physically consistent ripple/wave effects.

Enabling proper sampling control would resolve this issue and allow realistic distortion rendering.