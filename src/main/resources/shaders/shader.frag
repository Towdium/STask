#version 130

in vec2 tex_;
uniform sampler2D tex;

void main() {
	gl_FragColor = texture(tex, tex_);
}
