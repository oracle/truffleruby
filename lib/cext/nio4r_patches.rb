class Nio4RPatches < CommonPatches
  PATCHES = {
    'bytebuffer.c' => {
      gem: 'nio4r',
      patches: [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ]
    },
    'monitor.c' => {
      gem: 'nio4r',
      patches: [
        {
          match: /(static VALUE .*?) = Qnil;/,
          replacement: '\1;'
        }
      ]
    },
    'selector.c' => {
      gem: 'nio4r',
      patches: [
        {
          match: /(static VALUE .*?)\s+= Qnil;/,
          replacement: '\1;'
        }
      ]
    },
  }
end
