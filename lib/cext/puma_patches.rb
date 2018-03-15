class PumaPatches < CommonPatches

  PUMA_HTTP_PARSER_FREE = <<-EOF
void HttpParser_free(puma_parser* hp) {
  TRACE();

  if(hp) {
    rb_tr_release_handle(hp->request);
    rb_tr_release_handle(hp->body);
    xfree(hp);
  }
}
EOF

  PATCHES = {
    gem: 'puma_http11',
    patches: {
      'http11_parser.c' => [
        {
          match: /parser->(\w+) = Qnil;/,
          replacement: 'parser->\1 = rb_tr_handle_for_managed(Qnil);'
        }
      ],
      'puma_http11.c' => [
        # Handles for VALUEs in a static global struct array
        {
          match: /(define\b.*?), Qnil \}/,
          replacement: '\1, NULL }'
        },
        {
          match: /cf->value = (.*?);/,
          replacement: 'cf->value = rb_tr_handle_for_managed(\1);'
        },
        {
          match: /return found \? found->value : Qnil;/,
          replacement: 'return found ? rb_tr_managed_from_handle(found->value) : Qnil;'
        },
        {
          match: /return cf->value;/,
          replacement: 'return rb_tr_managed_from_handle(cf->value);'
        },
        # Handles for puma_parser->request and puma_parser->body
        {
          match: /void HttpParser_free\(void \*data\) {.*?}.*?}/m,
          replacement: PUMA_HTTP_PARSER_FREE
        },
        {
          match: /\(hp->request\b/,
          replacement: '(rb_tr_managed_from_handle(hp->request)'
        },
        {
          match: /(\w+)->request = (.+?);/,
          replacement: '\1->request = rb_tr_handle_for_managed(\2);'
        },
        {
          match: /return http->body;/,
          replacement: 'return rb_tr_managed_from_handle(http->body);'
        },
        {
          match: /(\w+)->body = (.+?);/,
          replacement: '\1->body = rb_tr_handle_for_managed(\2);'
        }
      ]
    }
  }
end
