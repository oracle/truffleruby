#include "yarp.h"
#include "org_yarp_Parser.h"

JNIEXPORT jbyteArray JNICALL Java_org_yarp_Parser_parseAndSerialize(JNIEnv *env, jclass clazz, jbyteArray source) {
  jsize size = (*env)->GetArrayLength(env, source);
  jbyte* bytes = (*env)->GetByteArrayElements(env, source, NULL);

  yp_buffer_t buffer;
  yp_buffer_init(&buffer);

  yp_parse_serialize((char*) bytes, size, &buffer);

  (*env)->ReleaseByteArrayElements(env, source, bytes, JNI_ABORT);

  jbyteArray serialized = (*env)->NewByteArray(env, buffer.length);
  (*env)->SetByteArrayRegion(env, serialized, 0, buffer.length, (jbyte*) buffer.value);

  yp_buffer_free(&buffer);

  return serialized;
}
