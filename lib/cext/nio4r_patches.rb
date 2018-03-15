class Nio4RPatches < CommonPatches
  PATCHES = {
    gem: 'nio4r',
    patches: {
      'bytebuffer.c' => [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ],
      'monitor.c' => [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ],
      'selector.c' => [
        {
          match: /(static VALUE .*?)\s+= Qnil;/,
          replacement: '\1;'
        }
      ]
    },
  }
end
