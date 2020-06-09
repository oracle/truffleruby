static struct {
    ID id_BEGIN;
    ID id_END;
    ID id_alias;
    ID id_alias_error;
    ID id_aref;
    ID id_aref_field;
    ID id_arg_ambiguous;
    ID id_arg_paren;
    ID id_args_add;
    ID id_args_add_block;
    ID id_args_add_star;
    ID id_args_new;
    ID id_array;
    ID id_assign;
    ID id_assign_error;
    ID id_assoc_new;
    ID id_assoc_splat;
    ID id_assoclist_from_args;
    ID id_bare_assoc_hash;
    ID id_begin;
    ID id_binary;
    ID id_block_var;
    ID id_blockarg;
    ID id_bodystmt;
    ID id_brace_block;
    ID id_break;
    ID id_call;
    ID id_case;
    ID id_class;
    ID id_class_name_error;
    ID id_command;
    ID id_command_call;
    ID id_const_path_field;
    ID id_const_path_ref;
    ID id_const_ref;
    ID id_def;
    ID id_defined;
    ID id_defs;
    ID id_do_block;
    ID id_dot2;
    ID id_dot3;
    ID id_dyna_symbol;
    ID id_else;
    ID id_elsif;
    ID id_ensure;
    ID id_excessed_comma;
    ID id_fcall;
    ID id_field;
    ID id_for;
    ID id_hash;
    ID id_heredoc_dedent;
    ID id_if;
    ID id_if_mod;
    ID id_ifop;
    ID id_kwrest_param;
    ID id_lambda;
    ID id_magic_comment;
    ID id_massign;
    ID id_method_add_arg;
    ID id_method_add_block;
    ID id_mlhs_add;
    ID id_mlhs_add_post;
    ID id_mlhs_add_star;
    ID id_mlhs_new;
    ID id_mlhs_paren;
    ID id_module;
    ID id_mrhs_add;
    ID id_mrhs_add_star;
    ID id_mrhs_new;
    ID id_mrhs_new_from_args;
    ID id_next;
    ID id_opassign;
    ID id_operator_ambiguous;
    ID id_param_error;
    ID id_params;
    ID id_paren;
    ID id_parse_error;
    ID id_program;
    ID id_qsymbols_add;
    ID id_qsymbols_new;
    ID id_qwords_add;
    ID id_qwords_new;
    ID id_redo;
    ID id_regexp_add;
    ID id_regexp_literal;
    ID id_regexp_new;
    ID id_rescue;
    ID id_rescue_mod;
    ID id_rest_param;
    ID id_retry;
    ID id_return;
    ID id_return0;
    ID id_sclass;
    ID id_stmts_add;
    ID id_stmts_new;
    ID id_string_add;
    ID id_string_concat;
    ID id_string_content;
    ID id_string_dvar;
    ID id_string_embexpr;
    ID id_string_literal;
    ID id_super;
    ID id_symbol;
    ID id_symbol_literal;
    ID id_symbols_add;
    ID id_symbols_new;
    ID id_top_const_field;
    ID id_top_const_ref;
    ID id_unary;
    ID id_undef;
    ID id_unless;
    ID id_unless_mod;
    ID id_until;
    ID id_until_mod;
    ID id_var_alias;
    ID id_var_field;
    ID id_var_ref;
    ID id_vcall;
    ID id_void_stmt;
    ID id_when;
    ID id_while;
    ID id_while_mod;
    ID id_word_add;
    ID id_word_new;
    ID id_words_add;
    ID id_words_new;
    ID id_xstring_add;
    ID id_xstring_literal;
    ID id_xstring_new;
    ID id_yield;
    ID id_yield0;
    ID id_zsuper;
} ripper_parser_ids;

#define ripper_id_BEGIN ripper_parser_ids.id_BEGIN
#define ripper_id_END ripper_parser_ids.id_END
#define ripper_id_alias ripper_parser_ids.id_alias
#define ripper_id_alias_error ripper_parser_ids.id_alias_error
#define ripper_id_aref ripper_parser_ids.id_aref
#define ripper_id_aref_field ripper_parser_ids.id_aref_field
#define ripper_id_arg_ambiguous ripper_parser_ids.id_arg_ambiguous
#define ripper_id_arg_paren ripper_parser_ids.id_arg_paren
#define ripper_id_args_add ripper_parser_ids.id_args_add
#define ripper_id_args_add_block ripper_parser_ids.id_args_add_block
#define ripper_id_args_add_star ripper_parser_ids.id_args_add_star
#define ripper_id_args_new ripper_parser_ids.id_args_new
#define ripper_id_array ripper_parser_ids.id_array
#define ripper_id_assign ripper_parser_ids.id_assign
#define ripper_id_assign_error ripper_parser_ids.id_assign_error
#define ripper_id_assoc_new ripper_parser_ids.id_assoc_new
#define ripper_id_assoc_splat ripper_parser_ids.id_assoc_splat
#define ripper_id_assoclist_from_args ripper_parser_ids.id_assoclist_from_args
#define ripper_id_bare_assoc_hash ripper_parser_ids.id_bare_assoc_hash
#define ripper_id_begin ripper_parser_ids.id_begin
#define ripper_id_binary ripper_parser_ids.id_binary
#define ripper_id_block_var ripper_parser_ids.id_block_var
#define ripper_id_blockarg ripper_parser_ids.id_blockarg
#define ripper_id_bodystmt ripper_parser_ids.id_bodystmt
#define ripper_id_brace_block ripper_parser_ids.id_brace_block
#define ripper_id_break ripper_parser_ids.id_break
#define ripper_id_call ripper_parser_ids.id_call
#define ripper_id_case ripper_parser_ids.id_case
#define ripper_id_class ripper_parser_ids.id_class
#define ripper_id_class_name_error ripper_parser_ids.id_class_name_error
#define ripper_id_command ripper_parser_ids.id_command
#define ripper_id_command_call ripper_parser_ids.id_command_call
#define ripper_id_const_path_field ripper_parser_ids.id_const_path_field
#define ripper_id_const_path_ref ripper_parser_ids.id_const_path_ref
#define ripper_id_const_ref ripper_parser_ids.id_const_ref
#define ripper_id_def ripper_parser_ids.id_def
#define ripper_id_defined ripper_parser_ids.id_defined
#define ripper_id_defs ripper_parser_ids.id_defs
#define ripper_id_do_block ripper_parser_ids.id_do_block
#define ripper_id_dot2 ripper_parser_ids.id_dot2
#define ripper_id_dot3 ripper_parser_ids.id_dot3
#define ripper_id_dyna_symbol ripper_parser_ids.id_dyna_symbol
#define ripper_id_else ripper_parser_ids.id_else
#define ripper_id_elsif ripper_parser_ids.id_elsif
#define ripper_id_ensure ripper_parser_ids.id_ensure
#define ripper_id_excessed_comma ripper_parser_ids.id_excessed_comma
#define ripper_id_fcall ripper_parser_ids.id_fcall
#define ripper_id_field ripper_parser_ids.id_field
#define ripper_id_for ripper_parser_ids.id_for
#define ripper_id_hash ripper_parser_ids.id_hash
#define ripper_id_heredoc_dedent ripper_parser_ids.id_heredoc_dedent
#define ripper_id_if ripper_parser_ids.id_if
#define ripper_id_if_mod ripper_parser_ids.id_if_mod
#define ripper_id_ifop ripper_parser_ids.id_ifop
#define ripper_id_kwrest_param ripper_parser_ids.id_kwrest_param
#define ripper_id_lambda ripper_parser_ids.id_lambda
#define ripper_id_magic_comment ripper_parser_ids.id_magic_comment
#define ripper_id_massign ripper_parser_ids.id_massign
#define ripper_id_method_add_arg ripper_parser_ids.id_method_add_arg
#define ripper_id_method_add_block ripper_parser_ids.id_method_add_block
#define ripper_id_mlhs_add ripper_parser_ids.id_mlhs_add
#define ripper_id_mlhs_add_post ripper_parser_ids.id_mlhs_add_post
#define ripper_id_mlhs_add_star ripper_parser_ids.id_mlhs_add_star
#define ripper_id_mlhs_new ripper_parser_ids.id_mlhs_new
#define ripper_id_mlhs_paren ripper_parser_ids.id_mlhs_paren
#define ripper_id_module ripper_parser_ids.id_module
#define ripper_id_mrhs_add ripper_parser_ids.id_mrhs_add
#define ripper_id_mrhs_add_star ripper_parser_ids.id_mrhs_add_star
#define ripper_id_mrhs_new ripper_parser_ids.id_mrhs_new
#define ripper_id_mrhs_new_from_args ripper_parser_ids.id_mrhs_new_from_args
#define ripper_id_next ripper_parser_ids.id_next
#define ripper_id_opassign ripper_parser_ids.id_opassign
#define ripper_id_operator_ambiguous ripper_parser_ids.id_operator_ambiguous
#define ripper_id_param_error ripper_parser_ids.id_param_error
#define ripper_id_params ripper_parser_ids.id_params
#define ripper_id_paren ripper_parser_ids.id_paren
#define ripper_id_parse_error ripper_parser_ids.id_parse_error
#define ripper_id_program ripper_parser_ids.id_program
#define ripper_id_qsymbols_add ripper_parser_ids.id_qsymbols_add
#define ripper_id_qsymbols_new ripper_parser_ids.id_qsymbols_new
#define ripper_id_qwords_add ripper_parser_ids.id_qwords_add
#define ripper_id_qwords_new ripper_parser_ids.id_qwords_new
#define ripper_id_redo ripper_parser_ids.id_redo
#define ripper_id_regexp_add ripper_parser_ids.id_regexp_add
#define ripper_id_regexp_literal ripper_parser_ids.id_regexp_literal
#define ripper_id_regexp_new ripper_parser_ids.id_regexp_new
#define ripper_id_rescue ripper_parser_ids.id_rescue
#define ripper_id_rescue_mod ripper_parser_ids.id_rescue_mod
#define ripper_id_rest_param ripper_parser_ids.id_rest_param
#define ripper_id_retry ripper_parser_ids.id_retry
#define ripper_id_return ripper_parser_ids.id_return
#define ripper_id_return0 ripper_parser_ids.id_return0
#define ripper_id_sclass ripper_parser_ids.id_sclass
#define ripper_id_stmts_add ripper_parser_ids.id_stmts_add
#define ripper_id_stmts_new ripper_parser_ids.id_stmts_new
#define ripper_id_string_add ripper_parser_ids.id_string_add
#define ripper_id_string_concat ripper_parser_ids.id_string_concat
#define ripper_id_string_content ripper_parser_ids.id_string_content
#define ripper_id_string_dvar ripper_parser_ids.id_string_dvar
#define ripper_id_string_embexpr ripper_parser_ids.id_string_embexpr
#define ripper_id_string_literal ripper_parser_ids.id_string_literal
#define ripper_id_super ripper_parser_ids.id_super
#define ripper_id_symbol ripper_parser_ids.id_symbol
#define ripper_id_symbol_literal ripper_parser_ids.id_symbol_literal
#define ripper_id_symbols_add ripper_parser_ids.id_symbols_add
#define ripper_id_symbols_new ripper_parser_ids.id_symbols_new
#define ripper_id_top_const_field ripper_parser_ids.id_top_const_field
#define ripper_id_top_const_ref ripper_parser_ids.id_top_const_ref
#define ripper_id_unary ripper_parser_ids.id_unary
#define ripper_id_undef ripper_parser_ids.id_undef
#define ripper_id_unless ripper_parser_ids.id_unless
#define ripper_id_unless_mod ripper_parser_ids.id_unless_mod
#define ripper_id_until ripper_parser_ids.id_until
#define ripper_id_until_mod ripper_parser_ids.id_until_mod
#define ripper_id_var_alias ripper_parser_ids.id_var_alias
#define ripper_id_var_field ripper_parser_ids.id_var_field
#define ripper_id_var_ref ripper_parser_ids.id_var_ref
#define ripper_id_vcall ripper_parser_ids.id_vcall
#define ripper_id_void_stmt ripper_parser_ids.id_void_stmt
#define ripper_id_when ripper_parser_ids.id_when
#define ripper_id_while ripper_parser_ids.id_while
#define ripper_id_while_mod ripper_parser_ids.id_while_mod
#define ripper_id_word_add ripper_parser_ids.id_word_add
#define ripper_id_word_new ripper_parser_ids.id_word_new
#define ripper_id_words_add ripper_parser_ids.id_words_add
#define ripper_id_words_new ripper_parser_ids.id_words_new
#define ripper_id_xstring_add ripper_parser_ids.id_xstring_add
#define ripper_id_xstring_literal ripper_parser_ids.id_xstring_literal
#define ripper_id_xstring_new ripper_parser_ids.id_xstring_new
#define ripper_id_yield ripper_parser_ids.id_yield
#define ripper_id_yield0 ripper_parser_ids.id_yield0
#define ripper_id_zsuper ripper_parser_ids.id_zsuper

static void
ripper_init_eventids1(void)
{
#define set_id1(name) ripper_id_##name = rb_intern_const("on_"#name)
    set_id1(BEGIN);
    set_id1(END);
    set_id1(alias);
    set_id1(alias_error);
    set_id1(aref);
    set_id1(aref_field);
    set_id1(arg_ambiguous);
    set_id1(arg_paren);
    set_id1(args_add);
    set_id1(args_add_block);
    set_id1(args_add_star);
    set_id1(args_new);
    set_id1(array);
    set_id1(assign);
    set_id1(assign_error);
    set_id1(assoc_new);
    set_id1(assoc_splat);
    set_id1(assoclist_from_args);
    set_id1(bare_assoc_hash);
    set_id1(begin);
    set_id1(binary);
    set_id1(block_var);
    set_id1(blockarg);
    set_id1(bodystmt);
    set_id1(brace_block);
    set_id1(break);
    set_id1(call);
    set_id1(case);
    set_id1(class);
    set_id1(class_name_error);
    set_id1(command);
    set_id1(command_call);
    set_id1(const_path_field);
    set_id1(const_path_ref);
    set_id1(const_ref);
    set_id1(def);
    set_id1(defined);
    set_id1(defs);
    set_id1(do_block);
    set_id1(dot2);
    set_id1(dot3);
    set_id1(dyna_symbol);
    set_id1(else);
    set_id1(elsif);
    set_id1(ensure);
    set_id1(excessed_comma);
    set_id1(fcall);
    set_id1(field);
    set_id1(for);
    set_id1(hash);
    set_id1(heredoc_dedent);
    set_id1(if);
    set_id1(if_mod);
    set_id1(ifop);
    set_id1(kwrest_param);
    set_id1(lambda);
    set_id1(magic_comment);
    set_id1(massign);
    set_id1(method_add_arg);
    set_id1(method_add_block);
    set_id1(mlhs_add);
    set_id1(mlhs_add_post);
    set_id1(mlhs_add_star);
    set_id1(mlhs_new);
    set_id1(mlhs_paren);
    set_id1(module);
    set_id1(mrhs_add);
    set_id1(mrhs_add_star);
    set_id1(mrhs_new);
    set_id1(mrhs_new_from_args);
    set_id1(next);
    set_id1(opassign);
    set_id1(operator_ambiguous);
    set_id1(param_error);
    set_id1(params);
    set_id1(paren);
    set_id1(parse_error);
    set_id1(program);
    set_id1(qsymbols_add);
    set_id1(qsymbols_new);
    set_id1(qwords_add);
    set_id1(qwords_new);
    set_id1(redo);
    set_id1(regexp_add);
    set_id1(regexp_literal);
    set_id1(regexp_new);
    set_id1(rescue);
    set_id1(rescue_mod);
    set_id1(rest_param);
    set_id1(retry);
    set_id1(return);
    set_id1(return0);
    set_id1(sclass);
    set_id1(stmts_add);
    set_id1(stmts_new);
    set_id1(string_add);
    set_id1(string_concat);
    set_id1(string_content);
    set_id1(string_dvar);
    set_id1(string_embexpr);
    set_id1(string_literal);
    set_id1(super);
    set_id1(symbol);
    set_id1(symbol_literal);
    set_id1(symbols_add);
    set_id1(symbols_new);
    set_id1(top_const_field);
    set_id1(top_const_ref);
    set_id1(unary);
    set_id1(undef);
    set_id1(unless);
    set_id1(unless_mod);
    set_id1(until);
    set_id1(until_mod);
    set_id1(var_alias);
    set_id1(var_field);
    set_id1(var_ref);
    set_id1(vcall);
    set_id1(void_stmt);
    set_id1(when);
    set_id1(while);
    set_id1(while_mod);
    set_id1(word_add);
    set_id1(word_new);
    set_id1(words_add);
    set_id1(words_new);
    set_id1(xstring_add);
    set_id1(xstring_literal);
    set_id1(xstring_new);
    set_id1(yield);
    set_id1(yield0);
    set_id1(zsuper);
}

static void
ripper_init_eventids1_table(VALUE self)
{
    VALUE h = rb_hash_new();
    rb_define_const(self, "PARSER_EVENT_TABLE", h);
    rb_hash_aset(h, intern_sym("BEGIN"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("END"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("alias"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("alias_error"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("aref"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("aref_field"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("arg_ambiguous"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("arg_paren"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("args_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("args_add_block"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("args_add_star"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("args_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("array"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("assign"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("assign_error"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("assoc_new"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("assoc_splat"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("assoclist_from_args"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("bare_assoc_hash"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("begin"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("binary"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("block_var"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("blockarg"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("bodystmt"), INT2FIX(4));
    rb_hash_aset(h, intern_sym("brace_block"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("break"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("call"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("case"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("class"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("class_name_error"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("command"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("command_call"), INT2FIX(4));
    rb_hash_aset(h, intern_sym("const_path_field"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("const_path_ref"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("const_ref"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("def"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("defined"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("defs"), INT2FIX(5));
    rb_hash_aset(h, intern_sym("do_block"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("dot2"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("dot3"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("dyna_symbol"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("else"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("elsif"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("ensure"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("excessed_comma"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("fcall"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("field"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("for"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("hash"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("heredoc_dedent"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("if"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("if_mod"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("ifop"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("kwrest_param"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("lambda"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("magic_comment"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("massign"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("method_add_arg"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("method_add_block"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mlhs_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mlhs_add_post"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mlhs_add_star"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mlhs_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("mlhs_paren"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("module"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mrhs_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mrhs_add_star"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("mrhs_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("mrhs_new_from_args"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("next"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("opassign"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("operator_ambiguous"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("param_error"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("params"), INT2FIX(7));
    rb_hash_aset(h, intern_sym("paren"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("parse_error"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("program"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("qsymbols_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("qsymbols_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("qwords_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("qwords_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("redo"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("regexp_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("regexp_literal"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("regexp_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("rescue"), INT2FIX(4));
    rb_hash_aset(h, intern_sym("rescue_mod"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("rest_param"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("retry"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("return"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("return0"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("sclass"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("stmts_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("stmts_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("string_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("string_concat"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("string_content"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("string_dvar"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("string_embexpr"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("string_literal"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("super"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("symbol"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("symbol_literal"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("symbols_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("symbols_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("top_const_field"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("top_const_ref"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("unary"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("undef"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("unless"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("unless_mod"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("until"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("until_mod"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("var_alias"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("var_field"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("var_ref"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("vcall"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("void_stmt"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("when"), INT2FIX(3));
    rb_hash_aset(h, intern_sym("while"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("while_mod"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("word_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("word_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("words_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("words_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("xstring_add"), INT2FIX(2));
    rb_hash_aset(h, intern_sym("xstring_literal"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("xstring_new"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("yield"), INT2FIX(1));
    rb_hash_aset(h, intern_sym("yield0"), INT2FIX(0));
    rb_hash_aset(h, intern_sym("zsuper"), INT2FIX(0));
}
