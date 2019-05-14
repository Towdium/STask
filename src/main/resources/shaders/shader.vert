#version 130

#define MAX_CLIP_PLANES 6
uniform mat4 proj;
uniform mat4 mat;
uniform vec4 clip[6];
in vec2 pos;
in vec2 tex;
out vec2 tex_;

void main() {
    vec4 space = mat * vec4(pos, 0, 1);
	gl_Position = proj * space;

    gl_ClipDistance[0] = dot(clip[0], space);
    gl_ClipDistance[1] = dot(clip[1], space);
    gl_ClipDistance[2] = dot(clip[2], space);
    gl_ClipDistance[3] = dot(clip[3], space);
    gl_ClipDistance[4] = dot(clip[4], space);
    gl_ClipDistance[5] = dot(clip[5], space);

	tex_ = tex;
}
