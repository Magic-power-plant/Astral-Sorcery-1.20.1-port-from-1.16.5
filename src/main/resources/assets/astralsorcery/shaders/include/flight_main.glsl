void main() {
    vec2 sheet = fract(sheetUV) * Effect.xy;
    vec2 cell = floor(sheet);
    vec2 position = 2.0 * fract(sheet) - 1.0;
    float tick = cell.x + cell.y * Effect.x;
    vec4 paint = polarMaterial(position, tick) * tint;
    if (paint.a < 0.1) discard;
    fragColor = paint * ColorModulator;
}
