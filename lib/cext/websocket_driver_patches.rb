class WebsocketDriverPatches < CommonPatches

  PATCHES = {
    gem: 'websocket-driver',
    patches: {
      'websocket_mask.c' => [
        {
          match: /(VALUE .*?)\s+= Qnil;/,
          replacement: '\1;'
        }
      ]
    },
  }
end
