int test_native_callback(int (*callback)(void)) {
  return callback();
}
