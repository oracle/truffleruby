#ifndef PSD_NATIVE_FILE
#define PSD_NATIVE_FILE

VALUE psd_file_read_byte(VALUE self);
VALUE psd_file_read_bytes(VALUE self, int bytes);
VALUE psd_file(VALUE self);
int psd_file_tell(VALUE self);

#endif