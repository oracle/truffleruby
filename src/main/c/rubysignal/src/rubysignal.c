/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
#include "org_truffleruby_signal_LibRubySignal.h"
#include <pthread.h>
#include <signal.h>

_Static_assert(sizeof(pthread_t) == sizeof(jlong), "Expected sizeof(pthread_t) == sizeof(jlong)");

static void empty_handler(int sig) {
}

JNIEXPORT jint JNICALL Java_org_truffleruby_signal_LibRubySignal_setupSIGVTALRMEmptySignalHandler(JNIEnv *env, jclass clazz) {
  struct sigaction action = {
    .sa_flags = 0, /* flags = 0 is intended as we want no SA_RESTART so we can interrupt blocking syscalls */
    .sa_handler = empty_handler,
  };
  return sigaction(SIGVTALRM, &action, NULL);
}

JNIEXPORT jlong JNICALL Java_org_truffleruby_signal_LibRubySignal_threadID(JNIEnv *env, jclass clazz) {
  pthread_t pthread_id = pthread_self();
  return (jlong) pthread_id;
}

JNIEXPORT jint JNICALL Java_org_truffleruby_signal_LibRubySignal_sendSIGVTALRMToThread(JNIEnv *env, jclass clazz, jlong threadID) {
  pthread_t pthread_id = (pthread_t) threadID;
  return pthread_kill(pthread_id, SIGVTALRM);
}
