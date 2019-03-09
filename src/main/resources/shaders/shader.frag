#version 130

in vec2 tex_;
in vec4 debug_;
uniform bool alpha;
uniform vec4 color;
uniform sampler2D tex;

void main() {
    vec4 sampled = texture(tex, tex_);
    vec4 masked = alpha ? vec4(1, 1, 1, sampled.a) : sampled;
    gl_FragColor = color * masked;
}
