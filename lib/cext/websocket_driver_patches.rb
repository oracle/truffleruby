class WebsocketDriverPatches < CommonPatches

  PATCHES = {
    'websocket_mask.c' => {
      gem: 'websocket-driver',
      patches: [
        {
          match: /(VALUE .*?)\s+= Qnil;/,
          replacement: '\1;'
        }
      ]
    },
  }
  
end
