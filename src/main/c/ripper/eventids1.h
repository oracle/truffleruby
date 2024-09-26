#ifndef RIPPER_EVENTIDS1
#define RIPPER_EVENTIDS1

void ripper_init_eventids1(void);
void ripper_init_eventids1_table(VALUE self);

struct ripper_parser_ids {
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
    ID id_args_forward;
    ID id_args_new;
    ID id_array;
    ID id_aryptn;
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
    ID id_fndptn;
    ID id_for;
    ID id_hash;
    ID id_heredoc_dedent;
    ID id_hshptn;
    ID id_if;
    ID id_if_mod;
    ID id_ifop;
    ID id_in;
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
    ID id_nokw_param;
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
};

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
#define ripper_id_args_forward ripper_parser_ids.id_args_forward
#define ripper_id_args_new ripper_parser_ids.id_args_new
#define ripper_id_array ripper_parser_ids.id_array
#define ripper_id_aryptn ripper_parser_ids.id_aryptn
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
#define ripper_id_fndptn ripper_parser_ids.id_fndptn
#define ripper_id_for ripper_parser_ids.id_for
#define ripper_id_hash ripper_parser_ids.id_hash
#define ripper_id_heredoc_dedent ripper_parser_ids.id_heredoc_dedent
#define ripper_id_hshptn ripper_parser_ids.id_hshptn
#define ripper_id_if ripper_parser_ids.id_if
#define ripper_id_if_mod ripper_parser_ids.id_if_mod
#define ripper_id_ifop ripper_parser_ids.id_ifop
#define ripper_id_in ripper_parser_ids.id_in
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
#define ripper_id_nokw_param ripper_parser_ids.id_nokw_param
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
#endif /* RIPPER_EVENTIDS1 */

