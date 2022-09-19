let g:rdb_bps = {}

function SET_BP()
  let signed = sign_getplaced(bufname(), {'lnum': line('.')})
  if empty(signed[0]['signs'])
    call sign_place(0, '', 'signBP', bufname(), {'lnum': line('.')})
  else
    "echo signed[0]['signs']
    call sign_unplace('', {'buffer': bufname(), 'id': signed[0]['signs'][0]['id']})
  endif
endfunction

function UPDATE_BPS()
  let signs = sign_getplaced(bufname())
  let key = expand('%:p')

  if empty(signs[0]['signs'])
    let removed = remove(g:rdb_bps, key)
  else
    let g:rdb_bps[key] = signs[0]['signs']
  endif
endfunction

function APPLY_BPS()
  let key = expand('%:p')
  if has_key(g:rdb_bps, key)
    for b in g:rdb_bps[key]
      call sign_place(0, '', 'signBP', bufname(), {'lnum': b['lnum']})
    endfor
  endif
endfunction

function WRITE_BPS()
  call writefile([json_encode(g:rdb_bps)], '.rdb_breakpoints.json')
endfunction

" load
try
  let json = readfile('.rdb_breakpoints.json')
  let g:rdb_bps = json_decode(json[0])
  " {"/full/path/to/file1": [{"lnum": 10}, ...], ...}
catch /Can't open/
  let g:rdb_bps = {}
catch /Invalid arguments for function json_decode/
  let g:rdb_bps = {}
endtry

sign define signBP text=BR

call APPLY_BPS()

autocmd BufReadPost * call APPLY_BPS()
autocmd BufUnload   * call UPDATE_BPS()
autocmd VimLeave    * call WRITE_BPS()

function! s:ruby_bp_settings() abort
  echomsg "Type <Space> to toggle break points and <q> to quit"

  if &readonly
    nnoremap <silent> <buffer> <Space> :call SET_BP()<CR>
    nnoremap <silent> <buffer> q :<C-u>quit<CR>
  endif
endfunction

" autocmd FileType ruby call s:ruby_bp_settings()
autocmd BufEnter *.rb call s:ruby_bp_settings()

call s:ruby_bp_settings()
