#version 150

uniform sampler2D Sampler0;
uniform float Progress;

in vec2 texCoord0;

out vec4 fragColor;

const float PI = 3.14159265359;

void main() {
    vec4 color = texture(Sampler0, texCoord0);

    // Ignore transparent pixels
    if (color.a <= 0.01) {
        discard;
    }

    // Convert UV to centered coordinates (-0.5 to 0.5)
    vec2 uv = texCoord0 - vec2(0.5);

    // atan(y, x)
    float angle = atan(uv.y, uv.x);

    // Rotate so:
    // 0 = top
    angle += PI * 0.5;

    // Convert negative angles
    if (angle < 0.0) {
        angle += PI * 2.0;
    }

    // Normalize to 0..1
    float normalized = angle / (PI * 2.0);

    // Clockwise fill
    if (normalized > Progress) {
        discard;
    }

    fragColor = color;
}