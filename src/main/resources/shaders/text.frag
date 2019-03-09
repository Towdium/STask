#version 130

in vec2 tex_;
uniform sampler2D tex;

void main() {
	gl_FragColor =  vec4(1.0, 1.0, 1.0, texture(tex, tex_).a);
}
