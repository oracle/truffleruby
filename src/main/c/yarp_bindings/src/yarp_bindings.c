#include "prism.h"
#include "org_prism_Parser.h"

JNIEXPORT jbyteArray JNICALL Java_org_prism_Parser_parseAndSerialize(JNIEnv *env, jclass clazz, jbyteArray source) {
  jsize size = (*env)->GetArrayLength(env, source);
  // Null-terminate for safety, as parsers are prone to read further than the end
  jbyte* bytes = malloc(size + 4);
  (*env)->GetByteArrayRegion(env, source, 0, size, bytes);
  memset(bytes + size, 0, 4);

  pm_buffer_t buffer;
  pm_buffer_init(&buffer);

  pm_serialize_parse(&buffer, (uint8_t*) bytes, size, NULL);

  free(bytes);

  jbyteArray serialized = (*env)->NewByteArray(env, buffer.length);
  (*env)->SetByteArrayRegion(env, serialized, 0, buffer.length, (jbyte*) buffer.value);

  pm_buffer_free(&buffer);

  return serialized;
}
