uniform vec4 Effect;
uniform vec4 ColorModulator;
in vec2 sheetUV;
in vec4 tint;
out vec4 fragColor;

const float TAU = 6.28318530718;

vec2 direction(float angle) {
    return vec2(cos(angle), sin(angle));
}

vec4 rimUnpack(uint coefficients) {
    return vec4(uvec4(coefficients, coefficients >> 8u, coefficients >> 16u, coefficients >> 24u) & uvec4(255u)) - 128.0;
}
