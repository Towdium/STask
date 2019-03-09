#version 130

uniform mat4 mat;
in vec2 pos;
in vec2 tex;
out vec2 tex_;

void main() {
	gl_Position = mat * vec4(pos, 0, 1);
	tex_ = tex;
}
