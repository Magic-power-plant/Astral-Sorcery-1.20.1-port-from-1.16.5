void main() {
    vec2 sheet = fract(sheetUV) * Effect.xy;
    vec2 cell = floor(sheet);
    vec2 position = 2.0 * fract(sheet) - 1.0;
    float age = (cell.x + cell.y * Effect.x) / (Effect.x * Effect.y);
    float angle = atan(position.y, position.x + 1e-10);
    vec4 paint = polarMaterial(length(position), angle, direction(age * TAU), int(Effect.w)) * tint;
    if (paint.a < 0.1) discard;
    fragColor = paint * ColorModulator;
}
