#version 150

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
in vec4 vertexColor;
in vec2 sheetUV;
out vec4 fragColor;

vec4 word(int address) {
    int stride = textureSize(Sampler0, 0).x;
    return texelFetch(Sampler0, ivec2(address % stride, address / stride), 0);
}

ivec4 bytes(int address) {
    return ivec4(floor(word(address) * 255.0 + 0.5));
}

int knotTime(int start, int index) {
    return bytes(start + index / 4)[index % 4];
}

vec4 sampleCurve(ivec2 pixel, ivec2 dimensions, ivec2 grid) {
    ivec2 extent = dimensions * grid;
    pixel = (pixel + extent) % extent;
    ivec2 local = pixel % dimensions;
    ivec4 header = bytes(2 + local.x + local.y * dimensions.x);
    int start = header.r + 256 * header.g + 65536 * header.b;
    int count = header.a;
    int values = start + (count + 3) / 4;
    if (count == 1) return word(values);

    ivec2 tile = pixel / dimensions;
    int frame = tile.x + tile.y * grid.x;
    int left = 0;
    int right = count - 1;
    while (right - left > 1) {
        int middle = (left + right) / 2;
        if (knotTime(start, middle) <= frame) left = middle;
        else right = middle;
    }
    int first = knotTime(start, left);
    float time = float(frame - first) / float(knotTime(start, right) - first);
    return mix(word(values + left), word(values + right), time);
}

void main() {
    ivec4 sizeBytes = bytes(0);
    ivec2 dimensions = sizeBytes.rb + 256 * sizeBytes.ga;
    ivec4 settings = bytes(1);
    ivec2 grid = settings.rg;
    vec2 uv = fract(sheetUV) * vec2(dimensions * grid);
    vec4 color;
    if (settings.b != 0) {
        vec2 local = uv - 0.5;
        ivec2 pixel = ivec2(floor(local));
        vec2 weight = fract(local);
        color = mix(mix(sampleCurve(pixel, dimensions, grid),
                        sampleCurve(pixel + ivec2(1, 0), dimensions, grid), weight.x),
                    mix(sampleCurve(pixel + ivec2(0, 1), dimensions, grid),
                        sampleCurve(pixel + ivec2(1, 1), dimensions, grid), weight.x), weight.y);
    } else {
        color = sampleCurve(ivec2(floor(uv)), dimensions, grid);
    }
    color *= vertexColor;
    if (color.a < 0.1) discard;
    fragColor = color * ColorModulator;
}
