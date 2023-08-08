#include "yarp.h"
#include "org_yarp_Parser.h"

JNIEXPORT jbyteArray JNICALL Java_org_yarp_Parser_parseAndSerialize(JNIEnv *env, jclass clazz, jbyteArray source) {
  jsize size = (*env)->GetArrayLength(env, source);
  // Null-terminate for safety, as parsers are prone to read further than the end
  jbyte* bytes = malloc(size + 4);
  (*env)->GetByteArrayRegion(env, source, 0, size, bytes);
  memset(bytes + size, 0, 4);

  yp_buffer_t buffer;
  yp_buffer_init(&buffer);

  yp_parse_serialize((char*) bytes, size, &buffer, NULL);

  free(bytes);

  jbyteArray serialized = (*env)->NewByteArray(env, buffer.length);
  (*env)->SetByteArrayRegion(env, serialized, 0, buffer.length, (jbyte*) buffer.value);

  yp_buffer_free(&buffer);

  return serialized;
}
