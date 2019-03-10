#version 130

in vec2 tex_;
uniform int mode;
uniform vec4 color;
uniform sampler2D tex;

void main() {
    vec4 sampled;
    if (mode == 0) sampled = vec4(1, 1, 1, 1);
    else if (mode == 1) sampled = vec4(1, 1, 1, texture(tex, tex_).a);
    else if (mode == 2) sampled = texture(tex, tex_);
    gl_FragColor = color * sampled;
}
