package dev.beankitk.drilo

private val rippleShaderPart1 = """
  uniform shader layer;
  uniform vec2 size;
  uniform vec2 origin;
  uniform float elapsedTime;
  uniform float amplitude;
  uniform float frequency;
  uniform float decay;
  uniform float speed;
    
  vec4 main(vec2 position) {
    float distance = length(position - origin);
    float delay = distance / speed;
    float time = elapsedTime - delay;
    time = max(0.0, time);
    
    float rippleAmount = amplitude * sin(frequency * time) * exp(-decay * time);
    vec2 n = normalize(position - origin);
    vec2 newPosition = position + rippleAmount * n;
"""

private val rippleShaderClipHack = """
    if (newPosition.x < 0.0 ||
        newPosition.y < 0.0 ||
        newPosition.x >= size.x ||
        newPosition.y >= size.y) {
        return vec4(0.0);
    }
"""

private val rippleShaderPart2 = """
    vec4 color = layer.eval(newPosition);
    color.rgb += 0.3 * (rippleAmount / amplitude) * color.a;
    return color;
  }
"""

val actualRippleShader = rippleShaderPart1 + rippleShaderPart2
val expectedRippleShader = rippleShaderPart1 + rippleShaderClipHack + rippleShaderPart2